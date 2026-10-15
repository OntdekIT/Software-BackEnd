package Ontdekstation013.ClimateChecker.features.measurement;

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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class  MeasurementService {
    private final MeetJeStadService meetJeStadService;

    public List<MeasurementDto> getMeasurementsAtTime(Instant dateTime) {
        int minuteMargin = meetJeStadService.getMinuteLimit();
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = dateTime.minus(Duration.ofMinutes(minuteMargin));
        params.EndDate = dateTime;
        params.includeFaultyMeasurements = false;
        List<Measurement> allMeasurements = meetJeStadService.getMeasurements(params);

        List<Measurement> closestMeasurements = MeasurementLogic.filterClosestMeasurements(allMeasurements, dateTime);

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

        return MeasurementLogic.splitIntoDayMeasurements(measurements);
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
        return MeasurementLogic.splitIntoNetworkDayMeasurements(measurements);
    }

    public List<HourMeasurementResponse> getHourlyMeasurements(int id, Instant startDate, Instant endDate) {
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = startDate;
        params.EndDate = endDate;
        params.StationIds.add(id);
        params.includeFaultyMeasurements = true;

        List<Measurement> measurements = meetJeStadService.getMeasurements(params);
        return MeasurementLogic.splitIntoHourMeasurements(measurements);
    }

    public List<HalfHourMeasurementResponse> getHalfHourlyMeasurements(int id, Instant startDate, Instant endDate) {
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = startDate;
        params.EndDate = endDate;
        params.StationIds.add(id);
        params.includeFaultyMeasurements = true;

        List<Measurement> measurements = meetJeStadService.getMeasurements(params);
        return MeasurementLogic.splitIntoHalfHourMeasurements(measurements);
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

            for (Measurement measurement : measurements) {
                String formattedMeasurement = formatMeasurement(measurement);
                Paragraph paragraph = new Paragraph(formattedMeasurement);
                document.add(paragraph);
            }

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    private String formatMeasurement(Measurement measurement) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestampStr = measurement.getTimestamp().atZone(ZoneId.systemDefault()).format(formatter);
        return String.format("%d: %s - Temp: %.2f - Hum: %.2f - Lat/Long: %.6f / %.6f",
                measurement.getId(), timestampStr, measurement.getTemperature() != null ? measurement.getTemperature() : 0,
                measurement.getHumidity() != null ? measurement.getHumidity() : 0, measurement.getLatitude(), measurement.getLongitude());
    }

    private MeasurementDto convertToDTO(Measurement entity) {
        MeasurementDto dto = new MeasurementDto();
        dto.setId(Math.toIntExact(entity.getStation().getStationid()));
        dto.setLongitude(entity.getLongitude());
        dto.setLatitude(entity.getLatitude());
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
