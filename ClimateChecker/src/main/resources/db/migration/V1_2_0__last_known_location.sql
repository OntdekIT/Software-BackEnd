-- Laatst bekende GPS-positie per station. Metingen zonder geldige positie
-- krijgen deze positie in plaats van weggegooid te worden.
ALTER TABLE station
    ADD COLUMN last_known_latitude  FLOAT NULL,
    ADD COLUMN last_known_longitude FLOAT NULL;

-- Markeert metingen waarvan de positie is overgenomen van het station.
ALTER TABLE measurement
    ADD COLUMN location_inherited BIT(1) NOT NULL DEFAULT b'0';
