package Ontdekstation013.ClimateChecker.features.measurement;

import Ontdekstation013.ClimateChecker.cache.CacheNames;
import Ontdekstation013.ClimateChecker.cache.TimeBuckets;
import Ontdekstation013.ClimateChecker.features.measurement.endpoint.MeasurementDto;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.utility.DayMeasurementResponse;
import Ontdekstation013.ClimateChecker.utility.HourMeasurementResponse;
import Ontdekstation013.ClimateChecker.utility.HalfHourMeasurementResponse;
import Ontdekstation013.ClimateChecker.utility.MeasurementLogic;
import Ontdekstation013.ClimateChecker.utility.NetworkMeasurementResponse;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class  MeasurementService {
    private final MeetJeStadService meetJeStadService;
    private final LocalMeasurementReader localMeasurementReader;
    private final ZoneId applicationZone;

    /**
     * De dichtstbijzijnde meting per station op het gegeven tijdstip, uit de lokale database.
     * Het tijdstip wordt op de minuut naar beneden afgerond; de uitkomst wordt per minuut 60 s
     * gecachet (zie CacheConfig).
     */
    @Cacheable(cacheNames = CacheNames.STATIONS_WITH_MEASUREMENTS,
            key = "T(Ontdekstation013.ClimateChecker.cache.TimeBuckets).floorToMinute(#dateTime)")
    public List<MeasurementDto> getMeasurementsAtTime(Instant dateTime) {
        Instant at = TimeBuckets.floorToMinute(dateTime);
        int minuteMargin = meetJeStadService.getMinuteLimit();
        List<Measurement> allMeasurements = localMeasurementReader.getMeasurements(
                at.minus(Duration.ofMinutes(minuteMargin)), at, false);

        List<Measurement> closestMeasurements = MeasurementLogic.filterClosestMeasurements(allMeasurements, at);

        return closestMeasurements.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<MeasurementDto> GetDailyMeasurements(Instant dateTime) {
        int minuteMargin = 1440;
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = dateTime.minus(Duration.ofMinutes(minuteMargin));
        params.EndDate = dateTime;
        params.includeFaultyMeasurements = true;
        params.StationIds.clear();
        List<Measurement> allMeasurements = meetJeStadService.getMeasurements(params);

        return allMeasurements.stream()
                .map(this::convertToDTO)
                .toList();
    }


    public List<DayMeasurementResponse> getHistoricalMeasurements(int id, Instant startDate, Instant endDate) {
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = startDate;
        params.EndDate = endDate;
        params.StationIds.add(id);
        params.includeFaultyMeasurements = true;
        List<Measurement> measurements = meetJeStadService.getMeasurements(params);

        return MeasurementLogic.splitIntoDayMeasurements(measurements, applicationZone);
    }

    /**
     * Haalt de netwerkbrede statistiek op: per dag het minimum, het maximum, de
     * mediaan en het gemiddelde over álle meetstations binnen Tilburg.
     *
     * <p>Door geen StationIds mee te geven levert MeetJeStad de metingen van
     * alle stations; MeetJeStadService filtert die daarna op de gemeentegrens.
     *
     * <p>Let op: dit is een zware aanroep. Over een lange periode haalt hij de
     * metingen van het hele netwerk op bij meetjestad.net, zonder cache. Zodra
     * er een lokale kopie van de meetdata is, hoort deze query daarop te draaien.
     */
    public List<NetworkMeasurementResponse> getNetworkMeasurements(Instant startDate, Instant endDate) {
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = startDate;
        params.EndDate = endDate;
        params.StationIds.clear(); // leeg = alle stations
        params.includeFaultyMeasurements = false; // netwerkstatistiek zonder uitschieters van defecte sensoren

        List<Measurement> measurements = meetJeStadService.getMeasurements(params);
        return MeasurementLogic.splitIntoNetworkDayMeasurements(measurements, applicationZone);
    }

    public List<HourMeasurementResponse> getHourlyMeasurements(int id, Instant startDate, Instant endDate) {
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = startDate;
        params.EndDate = endDate;
        params.StationIds.add(id);
        params.includeFaultyMeasurements = true;

        List<Measurement> measurements = meetJeStadService.getMeasurements(params);
        return MeasurementLogic.splitIntoHourMeasurements(measurements, applicationZone);
    }

    public List<HalfHourMeasurementResponse> getHalfHourlyMeasurements(int id, Instant startDate, Instant endDate) {
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = startDate;
        params.EndDate = endDate;
        params.StationIds.add(id);
        params.includeFaultyMeasurements = true;

        List<Measurement> measurements = meetJeStadService.getMeasurements(params);
        return MeasurementLogic.splitIntoHalfHourMeasurements(measurements, applicationZone);
    }

    public byte[] getMeasurementsAsPDF(int id, Instant startDate, Instant endDate){
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = startDate;
        params.EndDate = endDate;
        params.StationIds.add(id);
        params.includeFaultyMeasurements = true;
        List<Measurement> measurements = meetJeStadService.getMeasurements(params);

        return generatePdf(measurements);
    }

    public byte[] generatePdf(List<Measurement> measurements) {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // A PDF without content fails on close ("The document has no pages"), which used to
            // turn a period without measurements into a 500.
            if (measurements.isEmpty()) {
                document.add(new Paragraph("Geen metingen in de gekozen periode."));
            }

            for (Measurement measurement : measurements) {
                String formattedMeasurement = formatMeasurement(measurement);
                Paragraph paragraph = new Paragraph(formattedMeasurement);
                document.add(paragraph);
            }

            document.close();
        } catch (DocumentException e) {
            log.error("PDF genereren mislukt", e);
        }

        return baos.toByteArray();
    }

    private String formatMeasurement(Measurement measurement) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestampStr = measurement.getTimestamp().atZone(applicationZone).format(formatter);
        return String.format("%d: %s - Temp: %.2f - Hum: %.2f - Lat/Long: %.6f / %.6f",
                measurement.getId(), timestampStr, measurement.getTemperature() != null ? measurement.getTemperature() : 0,
                measurement.getHumidity() != null ? measurement.getHumidity() : 0, measurement.getLatitude(), measurement.getLongitude());
    }

    private MeasurementDto convertToDTO(Measurement entity) {
        MeasurementDto dto = new MeasurementDto();
        dto.setId(Math.toIntExact(entity.getStation().getStationid()));
        dto.setLongitude(entity.getLongitude());
        dto.setLatitude(entity.getLatitude());
        dto.setLocationInherited(entity.isLocationInherited());
        dto.setTemperature(entity.getTemperature());
        dto.setHumidity(entity.getHumidity());
        dto.setPm10(entity.getPm10());
        dto.setPm25(entity.getPm25());

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("dd-MM-yyyy HH:mm:ss")
                .withZone(ZoneId.of("Europe/Amsterdam"));
        dto.setTimestamp(formatter.format(entity.getTimestamp()));

        return dto;
    }
}
