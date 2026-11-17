package Ontdekstation013.ClimateChecker.snapshot;

import static org.assertj.core.api.Assertions.assertThat;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.snapshot.OverviewCalculator.StationInput;
import Ontdekstation013.ClimateChecker.snapshot.OverviewCalculator.WijkInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Regels van de geconsolideerde snapshot, getest op een kleine fixture zonder database. */
class OverviewCalculatorTest {

    private static final Instant NOW = Instant.parse("2026-03-01T12:00:00Z");
    private static final Duration RECENCY = Duration.ofHours(2);

    // Drie vierkante wijken naast elkaar op lon 5.00 tot 5.30, lat 51.50 tot 51.60.
    private static final WijkInput CENTRUM = square(1, "Centrum", 5.00f);
    private static final WijkInput DONGEWIJK = square(2, "Dongewijk", 5.10f);
    private static final WijkInput NOORD = square(3, "Noord", 5.20f);
    private static final List<WijkInput> WIJKEN = List.of(CENTRUM, DONGEWIJK, NOORD);

    private static WijkInput square(long id, String name, float lonLow) {
        return new WijkInput(id, name, new float[][] {
                {51.50f, lonLow}, {51.60f, lonLow}, {51.60f, lonLow + 0.09f}, {51.50f, lonLow + 0.09f}});
    }

    private static StationInput station(long id, String name) {
        return new StationInput(id, name, true, null, null, false, false, false, false);
    }

    private static Measurement m(long stationId, Duration ago, float lon, Float temp, Float pm25) {
        Measurement measurement = new Measurement(NOW.minus(ago), 51.55f, lon, temp, 50f, pm25, null);
        Station station = new Station();
        station.setStationid(stationId);
        measurement.setStation(station);
        return measurement;
    }

    private static Overview calculate(List<StationInput> stations, List<Measurement> measurements) {
        return calculate(stations, measurements, Map.of());
    }

    private static Overview calculate(List<StationInput> stations, List<Measurement> measurements,
                                      Map<Long, Instant> lastTimestamps) {
        return OverviewCalculator.calculate(NOW, RECENCY, WIJKEN, stations, measurements, lastTimestamps,
                new Overview.Ingest(NOW.toString(), NOW.toString(), null, 0, false), NOW);
    }

    private static Overview.Wijk wijk(Overview overview, String name) {
        return overview.wijken().stream().filter(w -> w.name().equals(name)).findFirst().orElseThrow();
    }

    @Test
    void wijkAveragesUseTheCurrentValuesOfItsStations() {
        Overview overview = calculate(List.of(station(1, "A"), station(2, "B")), List.of(
                m(1, Duration.ofMinutes(10), 5.02f, 10f, 4f),
                m(2, Duration.ofMinutes(20), 5.03f, 12f, null)));

        Overview.Wijk centrum = wijk(overview, "Centrum");
        assertThat(centrum.avgTemp()).isEqualTo(11f);
        assertThat(centrum.avgPm25()).isEqualTo(4f);
        assertThat(centrum.stationCount()).isEqualTo(2);
        assertThat(overview.city().avgTemp()).isEqualTo(11f);
    }

    @Test
    void dongewijkWithNullInTheLatestHourAndOnlyAFiveHourOldValueIsNull() {
        Overview overview = calculate(List.of(station(1, "A")), List.of(
                m(1, Duration.ofHours(5), 5.12f, 14f, 6f),
                m(1, Duration.ofMinutes(30), 5.12f, null, null)));

        Overview.Wijk dongewijk = wijk(overview, "Dongewijk");
        assertThat(dongewijk.avgTemp()).isNull();
        assertThat(dongewijk.avgPm25()).isNull();
        // Er is wel een recente meting, dus het station telt mee, alleen zonder waarde.
        assertThat(dongewijk.stationCount()).isEqualTo(1);
        // De oude waarde zit nog wel in de reeks, want die toont de geschiedenis.
        assertThat(dongewijk.series().stream().map(Overview.SeriesPoint::avgTemp)).contains(14f);
        assertThat(overview.city().wijkenWithData()).isZero();
        assertThat(overview.city().avgTemp()).isNull();
    }

    @Test
    void aValueJustInsideTheRecencyWindowCountsAndJustOutsideDoesNot() {
        Overview inside = calculate(List.of(station(1, "A")), List.of(m(1, Duration.ofMinutes(119), 5.02f, 10f, null)));
        Overview outside = calculate(List.of(station(1, "A")), List.of(m(1, Duration.ofMinutes(121), 5.02f, 10f, null)));

        assertThat(wijk(inside, "Centrum").avgTemp()).isEqualTo(10f);
        assertThat(wijk(outside, "Centrum").avgTemp()).isNull();
        assertThat(outside.heatmapPoints()).isEmpty();
    }

    @Test
    void anOlderNonNullValueWithinTheWindowIsUsedWhenTheLatestIsNull() {
        Overview overview = calculate(List.of(station(1, "A")), List.of(
                m(1, Duration.ofMinutes(90), 5.02f, 9f, null),
                m(1, Duration.ofMinutes(5), 5.02f, null, null)));

        assertThat(wijk(overview, "Centrum").avgTemp()).isEqualTo(9f);
        assertThat(overview.heatmapPoints().get(0).ageMinutes()).isEqualTo(90);
    }

    @Test
    void warmestAndColdestTieBreakOnNameCaseInsensitively() {
        Overview overview = calculate(List.of(station(1, "A"), station(2, "B"), station(3, "C")), List.of(
                m(1, Duration.ofMinutes(5), 5.02f, 15f, null),     // Centrum
                m(2, Duration.ofMinutes(5), 5.12f, 15f, null),     // Dongewijk
                m(3, Duration.ofMinutes(5), 5.22f, 15f, null)));   // Noord

        assertThat(overview.city().warmest().name()).isEqualTo("Centrum");
        assertThat(overview.city().coldest().name()).isEqualTo("Centrum");
    }

    @Test
    void warmestColdestAndBiggestChangeAreFoundAcrossWijken() {
        Overview overview = calculate(List.of(station(1, "A"), station(2, "B")), List.of(
                m(1, Duration.ofHours(20), 5.02f, 8f, null),
                m(1, Duration.ofMinutes(5), 5.02f, 20f, null),     // Centrum: +12
                m(2, Duration.ofHours(20), 5.12f, 10f, null),
                m(2, Duration.ofMinutes(5), 5.12f, 9f, null)));    // Dongewijk: -1

        assertThat(overview.city().warmest().name()).isEqualTo("Centrum");
        assertThat(overview.city().coldest().name()).isEqualTo("Dongewijk");
        assertThat(overview.city().avgTemp()).isEqualTo(14.5f);
        assertThat(overview.city().biggestChange().name()).isEqualTo("Centrum");
        assertThat(overview.city().biggestChange().delta()).isEqualTo(12f);
    }

    @Test
    void equalBiggestChangesAreDecidedByName() {
        Overview overview = calculate(List.of(station(1, "A"), station(2, "B")), List.of(
                m(1, Duration.ofHours(20), 5.02f, 10f, null),
                m(1, Duration.ofMinutes(5), 5.02f, 12f, null),
                m(2, Duration.ofHours(20), 5.12f, 20f, null),
                m(2, Duration.ofMinutes(5), 5.12f, 22f, null)));

        assertThat(overview.city().biggestChange().name()).isEqualTo("Centrum");
    }

    @Test
    void stationsExplainWhyTheyHaveNoCurrentValue() {
        Overview overview = calculate(
                List.of(station(1, "ok"), station(2, "sensorfout"), station(3, "laat"), station(4, "uitval")),
                List.of(
                        m(1, Duration.ofMinutes(5), 5.02f, 10f, null),
                        m(2, Duration.ofMinutes(5), 5.03f, null, null),
                        m(3, Duration.ofHours(5), 5.04f, 10f, null)),
                Map.of(4L, NOW.minus(Duration.ofDays(9))));

        Map<Long, Overview.Station> byId = new java.util.HashMap<>();
        overview.stations().forEach(s -> byId.put(s.id(), s));
        assertThat(byId.get(1L).status()).isEqualTo("ok");
        assertThat(byId.get(1L).reason()).isNull();
        assertThat(byId.get(2L).reason()).isEqualTo("sensorfout");
        assertThat(byId.get(3L).reason()).isEqualTo("late_meting");
        assertThat(byId.get(4L).reason()).isEqualTo("uitval");
        // Ook een offline station houdt zijn laatste meettijdstip.
        assertThat(byId.get(4L).lastMeasurementAt()).isEqualTo(NOW.minus(Duration.ofDays(9)).toString());
        assertThat(byId.get(3L).lastMeasurementAt()).isEqualTo(NOW.minus(Duration.ofHours(5)).toString());
        assertThat(overview.network().stationsTotal()).isEqualTo(4);
        assertThat(overview.network().active()).isEqualTo(2);
        assertThat(overview.network().offline()).isEqualTo(2);
    }

    @Test
    void wijkLastMeasurementAtIncludesOfflineStations() {
        Overview overview = calculate(List.of(
                        new StationInput(1, "offline", true, 51.55f, 5.02f, false, false, false, false)),
                List.of(), Map.of(1L, NOW.minus(Duration.ofDays(3))));

        Overview.Wijk centrum = wijk(overview, "Centrum");
        assertThat(centrum.avgTemp()).isNull();
        assertThat(centrum.lastMeasurementAt()).isEqualTo(NOW.minus(Duration.ofDays(3)).toString());
    }

    @Test
    void seriesHasTwentyFourHourlyPointsEndingNow() {
        Overview overview = calculate(List.of(station(1, "A")), List.of(
                m(1, Duration.ofMinutes(30), 5.02f, 10f, null),
                m(1, Duration.ofMinutes(90), 5.02f, 8f, null)));

        List<Overview.SeriesPoint> series = wijk(overview, "Centrum").series();
        assertThat(series).hasSize(24);
        assertThat(series.get(23).t()).isEqualTo(NOW.toString());
        assertThat(series.get(23).avgTemp()).isEqualTo(10f);
        assertThat(series.get(22).avgTemp()).isEqualTo(8f);
        assertThat(series.get(0).avgTemp()).isNull();
    }

    @Test
    void networkCoverageIsTheShareOfStationHoursWithData() {
        // Twee stations, 48 station-uren; een station heeft in 6 verschillende uren een meting.
        List<Measurement> measurements = new ArrayList<>();
        for (int hour = 0; hour < 6; hour++) {
            measurements.add(m(1, Duration.ofMinutes(30 + 60L * hour), 5.02f, 10f, null));
        }
        Overview overview = calculate(List.of(station(1, "A"), station(2, "B")), measurements);

        assertThat(overview.network().coveragePercent24h()).isEqualTo(12.5f);
    }

    @Test
    void heatmapPointsCarryValueAndAgeOfStationsWithACurrentValue() {
        Overview overview = calculate(List.of(station(1, "A"), station(2, "B")), List.of(
                m(1, Duration.ofMinutes(15), 5.02f, 10.456f, 3f),
                m(2, Duration.ofMinutes(15), 5.03f, null, 3f)));

        assertThat(overview.heatmapPoints()).hasSize(1);
        Overview.HeatPoint point = overview.heatmapPoints().get(0);
        assertThat(point.stationId()).isEqualTo(1);
        assertThat(point.temperature()).isEqualTo(10.46f);
        assertThat(point.ageMinutes()).isEqualTo(15);
    }

    @Test
    void nanAndInfiniteValuesNeverLeakIntoTheJson() throws Exception {
        Overview overview = calculate(List.of(station(1, "A"), station(2, "B")), List.of(
                m(1, Duration.ofMinutes(5), 5.02f, Float.NaN, Float.POSITIVE_INFINITY),
                m(2, Duration.ofMinutes(5), 5.12f, 10f, Float.NaN)));

        String json = new ObjectMapper().writeValueAsString(overview);

        assertThat(json).doesNotContain("NaN").doesNotContain("Infinity");
        assertThat(wijk(overview, "Centrum").avgTemp()).isNull();
        assertThat(wijk(overview, "Dongewijk").avgPm25()).isNull();
    }

    @Test
    void emptyInputGivesEmptyButCompleteStructure() {
        Overview overview = calculate(List.of(), List.of());

        assertThat(overview.stations()).isEmpty();
        assertThat(overview.heatmapPoints()).isEmpty();
        assertThat(overview.wijken()).hasSize(3);
        assertThat(overview.city().warmest()).isNull();
        assertThat(overview.network().coveragePercent24h()).isNull();
        assertThat(overview.schemaVersion()).isEqualTo(1);
    }

    @Test
    void timestampsAreIsoUtc() {
        Overview overview = calculate(List.of(station(1, "A")), List.of(m(1, Duration.ofMinutes(5), 5.02f, 10f, null)));

        assertThat(overview.generatedAt()).isEqualTo("2026-03-01T12:00:00Z");
        assertThat(overview.stations().get(0).latest().timestamp()).isEqualTo("2026-03-01T11:55:00Z");
    }

    @Test
    void volatileFieldsAreLeftOutOfTheEtagCopy() {
        Overview overview = calculate(List.of(), List.of());

        Overview stable = overview.withoutVolatileFields();

        assertThat(stable.generatedAt()).isNull();
        assertThat(stable.ingest().lastAttemptAt()).isNull();
        assertThat(stable.ingest().lastSuccessAt()).isEqualTo(overview.ingest().lastSuccessAt());
    }
}
