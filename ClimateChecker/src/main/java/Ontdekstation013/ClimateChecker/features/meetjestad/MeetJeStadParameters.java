package Ontdekstation013.ClimateChecker.features.meetjestad;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MeetJeStadParameters {
    public Instant StartDate;
    public Instant EndDate;
    public List<Integer> StationIds = new ArrayList<>();
    public int Limit;
    public boolean includeFaultyMeasurements;
}
