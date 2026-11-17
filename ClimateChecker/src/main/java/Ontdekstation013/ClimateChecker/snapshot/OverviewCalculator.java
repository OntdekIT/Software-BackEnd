package Ontdekstation013.ClimateChecker.snapshot;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.utility.GpsTriangulation;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Rekent de {@link Overview} uit ruwe invoer. Bewust een pure functie zonder database of klok, zodat
 * de regels met een fixture te testen zijn.
 *
 * <p>Regels: een waarde is actueel als de meting hoogstens {@code recency} oud is. Per station en per
 * grootheid telt de nieuwste niet-lege waarde binnen die termijn; een oudere waarde wordt nooit
 * gebruikt. Een wijk zonder actuele waarde krijgt null. Gelijke stand bij warmste/koudste wijk wordt
 * beslist op naam (alfabetisch, hoofdletterongevoelig), daarna op id.
 */
public final class OverviewCalculator {
    public static final int SERIES_HOURS = 24;

    public record WijkInput(long id, String name, float[][] polygon) {
    }

    public record StationInput(long id, String name, Boolean isActive, Float lastKnownLatitude,
                               Float lastKnownLongitude, boolean tempError, boolean humError,
                               boolean stofError, boolean locError) {
    }

    private record Current(float value, Instant at) {
    }

    private record StationState(StationInput input, List<Measurement> measurements, Measurement latest,
                                Current temp, Current pm25, Float lat, Float lon) {
        boolean recent(Instant now, Duration recency) {
            return latest != null && !latest.getTimestamp().isBefore(now.minus(recency));
        }
    }

    private OverviewCalculator() {
    }

    public static Overview calculate(Instant now, Duration recency, List<WijkInput> wijken,
                                     List<StationInput> stations, List<Measurement> measurements24h,
                                     Map<Long, Instant> lastTimestamps, Overview.Ingest ingest, Instant dataAsOf) {
        Map<Long, List<Measurement>> byStation = new HashMap<>();
        for (Measurement m : measurements24h) {
            if (m.getStation() != null && !m.getTimestamp().isAfter(now)) {
                byStation.computeIfAbsent(m.getStation().getStationid(), k -> new ArrayList<>()).add(m);
            }
        }

        List<StationState> states = new ArrayList<>();
        for (StationInput input : stations) {
            states.add(stateOf(input, byStation.getOrDefault(input.id(), List.of()), now, recency));
        }

        Instant anchor = now.truncatedTo(ChronoUnit.MINUTES);
        Map<Long, Long> wijkOfStation = assignWijken(states, wijken);

        List<Overview.Wijk> wijkResults = new ArrayList<>();
        for (WijkInput wijk : wijken) {
            List<StationState> inWijk = states.stream()
                    .filter(s -> Objects.equals(wijkOfStation.get(s.input().id()), wijk.id())).toList();
            wijkResults.add(buildWijk(wijk, inWijk, anchor, now, recency, lastTimestamps));
        }

        List<Overview.Station> stationResults = states.stream()
                .map(s -> buildStation(s, now, recency, lastTimestamps)).toList();

        return new Overview(Overview.SCHEMA_VERSION, now.toString(), dataAsOf == null ? null : dataAsOf.toString(),
                recency.toMinutes(), ingest, buildCity(wijkResults), wijkResults, stationResults,
                buildNetwork(states, anchor, now, recency), buildHeatPoints(states, now));
    }

    private static StationState stateOf(StationInput input, List<Measurement> measurements, Instant now, Duration recency) {
        List<Measurement> sorted = measurements.stream().sorted(Comparator.comparing(Measurement::getTimestamp)).toList();
        Measurement latest = sorted.isEmpty() ? null : sorted.get(sorted.size() - 1);
        Instant oldestAllowed = now.minus(recency);
        Current temp = latestValue(sorted, oldestAllowed, Measurement::getTemperature);
        Current pm25 = latestValue(sorted, oldestAllowed, Measurement::getPm25);
        Float lat = latest != null ? Float.valueOf(latest.getLatitude()) : input.lastKnownLatitude();
        Float lon = latest != null ? Float.valueOf(latest.getLongitude()) : input.lastKnownLongitude();
        return new StationState(input, sorted, latest, temp, pm25, finite(lat), finite(lon));
    }

    private static Current latestValue(List<Measurement> sortedAscending, Instant oldestAllowed,
                                       Function<Measurement, Float> getter) {
        for (int i = sortedAscending.size() - 1; i >= 0; i--) {
            Measurement m = sortedAscending.get(i);
            if (m.getTimestamp().isBefore(oldestAllowed)) {
                return null;
            }
            Float value = finite(getter.apply(m));
            if (value != null) {
                return new Current(value, m.getTimestamp());
            }
        }
        return null;
    }

    private static Map<Long, Long> assignWijken(List<StationState> states, List<WijkInput> wijken) {
        Map<Long, Long> result = new HashMap<>();
        for (StationState state : states) {
            if (state.lat() == null || state.lon() == null) {
                continue;
            }
            float[] point = {state.lat(), state.lon()};
            for (WijkInput wijk : wijken) {
                if (GpsTriangulation.pointInPolygon(wijk.polygon(), point)) {
                    result.put(state.input().id(), wijk.id());
                    break;
                }
            }
        }
        return result;
    }

    private static Overview.Wijk buildWijk(WijkInput wijk, List<StationState> stations, Instant anchor, Instant now,
                                           Duration recency, Map<Long, Instant> lastTimestamps) {
        Float avgTemp = mean(stations.stream().map(s -> s.temp() == null ? null : s.temp().value()).toList());
        Float avgPm25 = mean(stations.stream().map(s -> s.pm25() == null ? null : s.pm25().value()).toList());
        int stationCount = (int) stations.stream().filter(s -> s.recent(now, recency)).count();
        Instant last = stations.stream().map(s -> lastMeasurementAt(s, lastTimestamps)).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);

        List<Overview.SeriesPoint> series = new ArrayList<>();
        for (int i = 0; i < SERIES_HOURS; i++) {
            Instant end = anchor.minus(Duration.ofHours(SERIES_HOURS - 1 - i));
            Instant start = end.minus(Duration.ofHours(1));
            List<Float> temps = new ArrayList<>();
            List<Float> pms = new ArrayList<>();
            for (StationState s : stations) {
                temps.add(bucketMean(s, start, end, Measurement::getTemperature));
                pms.add(bucketMean(s, start, end, Measurement::getPm25));
            }
            series.add(new Overview.SeriesPoint(end.toString(), mean(temps), mean(pms)));
        }
        return new Overview.Wijk(wijk.id(), wijk.name(), avgTemp, avgPm25, stationCount,
                last == null ? null : last.toString(), series);
    }

    // Gemiddelde van een station in een uur: (start, end].
    private static Float bucketMean(StationState state, Instant start, Instant end, Function<Measurement, Float> getter) {
        List<Float> values = new ArrayList<>();
        for (Measurement m : state.measurements()) {
            if (m.getTimestamp().isAfter(start) && !m.getTimestamp().isAfter(end)) {
                values.add(getter.apply(m));
            }
        }
        return mean(values);
    }

    private static Instant lastMeasurementAt(StationState state, Map<Long, Instant> lastTimestamps) {
        Instant stored = lastTimestamps.get(state.input().id());
        Instant inWindow = state.latest() == null ? null : state.latest().getTimestamp();
        if (stored == null) {
            return inWindow;
        }
        return inWindow != null && inWindow.isAfter(stored) ? inWindow : stored;
    }

    private static Overview.Station buildStation(StationState s, Instant now, Duration recency,
                                                 Map<Long, Instant> lastTimestamps) {
        Overview.Latest latest = s.latest() == null ? null : new Overview.Latest(
                s.latest().getTimestamp().toString(), round(finite(s.latest().getTemperature())),
                round(finite(s.latest().getHumidity())), round(finite(s.latest().getPm25())),
                round(finite(s.latest().getPm10())));
        boolean hasCurrent = s.temp() != null || s.pm25() != null;
        String reason = null;
        if (!hasCurrent) {
            if (s.latest() == null) {
                reason = "uitval";
            } else if (s.recent(now, recency)) {
                reason = "sensorfout";
            } else {
                reason = "late_meting";
            }
        }
        Instant last = lastMeasurementAt(s, lastTimestamps);
        StationInput in = s.input();
        return new Overview.Station(in.id(), in.name(), s.lat(), s.lon(), in.isActive(), latest,
                new Overview.Flags(in.tempError(), in.humError(), in.stofError(), in.locError()),
                hasCurrent ? "ok" : "no_current_value", reason, last == null ? null : last.toString());
    }

    private static Overview.City buildCity(List<Overview.Wijk> wijken) {
        List<Overview.Wijk> withTemp = wijken.stream().filter(w -> w.avgTemp() != null).toList();
        Float avgTemp = mean(withTemp.stream().map(Overview.Wijk::avgTemp).toList());
        Float avgPm25 = mean(wijken.stream().map(Overview.Wijk::avgPm25).toList());

        Comparator<Overview.Wijk> byName = Comparator.comparing((Overview.Wijk w) -> w.name() == null ? "" : w.name(),
                String.CASE_INSENSITIVE_ORDER).thenComparingLong(Overview.Wijk::id);
        Overview.Wijk warmest = withTemp.stream()
                .min(Comparator.comparing(Overview.Wijk::avgTemp, Comparator.reverseOrder()).thenComparing(byName)).orElse(null);
        Overview.Wijk coldest = withTemp.stream()
                .min(Comparator.comparing(Overview.Wijk::avgTemp).thenComparing(byName)).orElse(null);

        Overview.Change change = null;
        Float bestAbs = null;
        for (Overview.Wijk w : withTemp.stream().sorted(byName).toList()) {
            Float from = w.series().stream().map(Overview.SeriesPoint::avgTemp).filter(Objects::nonNull)
                    .findFirst().orElse(null);
            if (from == null) {
                continue;
            }
            float delta = w.avgTemp() - from;
            // Strikt groter: bij gelijkspel wint de eerste op naam, omdat we op naam gesorteerd lopen.
            if (bestAbs == null || Math.abs(delta) > bestAbs) {
                bestAbs = Math.abs(delta);
                change = new Overview.Change(w.id(), w.name(), round(from), w.avgTemp(), round(delta));
            }
        }
        return new Overview.City(avgTemp, avgPm25, withTemp.size(),
                warmest == null ? null : new Overview.WijkRef(warmest.id(), warmest.name(), warmest.avgTemp()),
                coldest == null ? null : new Overview.WijkRef(coldest.id(), coldest.name(), coldest.avgTemp()),
                change);
    }

    private static Overview.Network buildNetwork(List<StationState> states, Instant anchor, Instant now, Duration recency) {
        int total = states.size();
        int active = (int) states.stream().filter(s -> s.recent(now, recency)).count();
        int withErrors = (int) states.stream().filter(s -> s.input().tempError() || s.input().humError()
                || s.input().stofError() || s.input().locError()).count();

        Float coverage = null;
        if (total > 0) {
            Set<String> covered = new HashSet<>();
            for (StationState s : states) {
                for (Measurement m : s.measurements()) {
                    long hoursAgo = Duration.between(m.getTimestamp(), anchor).toMinutes() / 60;
                    if (hoursAgo >= 0 && hoursAgo < SERIES_HOURS) {
                        covered.add(s.input().id() + "@" + hoursAgo);
                    }
                }
            }
            coverage = round(100f * covered.size() / (total * SERIES_HOURS));
        }
        return new Overview.Network(total, active, total - active, withErrors, coverage);
    }

    private static List<Overview.HeatPoint> buildHeatPoints(List<StationState> states, Instant now) {
        Map<Long, Overview.HeatPoint> points = new LinkedHashMap<>();
        for (StationState s : states) {
            if (s.temp() == null || s.lat() == null || s.lon() == null) {
                continue;
            }
            long age = Math.max(0, Duration.between(s.temp().at(), now).toMinutes());
            points.put(s.input().id(), new Overview.HeatPoint(s.input().id(), s.lat(), s.lon(),
                    round(s.temp().value()), s.pm25() == null ? null : round(s.pm25().value()), age));
        }
        return new ArrayList<>(points.values());
    }

    /** Gemiddelde van de niet-lege, eindige waarden, of null als er geen zijn. Afgerond op 2 decimalen. */
    static Float mean(List<Float> values) {
        double sum = 0;
        int count = 0;
        for (Float v : values) {
            Float clean = finite(v);
            if (clean != null) {
                sum += clean;
                count++;
            }
        }
        return count == 0 ? null : round((float) (sum / count));
    }

    private static Float finite(Float value) {
        return value == null || value.isNaN() || value.isInfinite() ? null : value;
    }

    private static Float round(Float value) {
        return value == null ? null : Math.round(value * 100f) / 100f;
    }
}
