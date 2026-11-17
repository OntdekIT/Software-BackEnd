-- Index voor opzoeken van metingen per station binnen een tijdvenster.
CREATE INDEX idx_measurement_stationid_timestamp ON measurement (stationid, timestamp);

-- De kaartendpoints lezen alle metingen in een tijdvenster, over alle stations heen.
-- Daarvoor is een index met timestamp vooraan nodig; de index hierboven helpt dan niet.
CREATE INDEX idx_measurement_timestamp ON measurement (timestamp);
