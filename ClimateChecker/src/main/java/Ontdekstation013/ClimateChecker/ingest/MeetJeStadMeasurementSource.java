package Ontdekstation013.ClimateChecker.ingest;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetJeStadMeasurementSource implements MeasurementSource {
    private final MeetJeStadService meetJeStadService;

    @Override
    public List<Measurement> fetch(Instant start, Instant end) {
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = start;
        params.EndDate = end;
        // Alles opslaan; het uitschieterfilter gebeurt bij het lezen, zoals voorheen.
        params.includeFaultyMeasurements = true;
        return meetJeStadService.getMeasurements(params);
    }
}
