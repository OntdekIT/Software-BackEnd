package Ontdekstation013.ClimateChecker.features.measurement;

import Ontdekstation013.ClimateChecker.features.measurement.endpoint.MeasurementDto;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.utility.DayMeasurementResponse;
import Ontdekstation013.ClimateChecker.utility.MeasurementLogic;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

