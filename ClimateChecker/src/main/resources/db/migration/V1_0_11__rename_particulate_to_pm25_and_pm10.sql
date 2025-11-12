ALTER TABLE measurement RENAME COLUMN particulate TO pm25;
ALTER TABLE measurement ADD COLUMN pm10 FLOAT;