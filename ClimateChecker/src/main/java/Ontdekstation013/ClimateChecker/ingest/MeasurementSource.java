package Ontdekstation013.ClimateChecker.ingest;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import java.time.Instant;
import java.util.List;

/** Bron van ruwe metingen. De ingest-worker is de enige aanroeper (de enige route naar meetjestad.net). */
public interface MeasurementSource {
    List<Measurement> fetch(Instant start, Instant end);
}
