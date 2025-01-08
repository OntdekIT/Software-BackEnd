CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    user_role VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS workshop (
    code BIGINT PRIMARY KEY,
    expiration_date DATETIME,
    creation_date DATETIME
);

CREATE TABLE IF NOT EXISTS station (
    stationid BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    database_tag VARCHAR(255),
    is_public BOOLEAN,
    registration_code BIGINT,
    user_id BIGINT,
    is_active BOOLEAN
    );

INSERT INTO workshop (code, expiration_date, creation_date) VALUES (123456, '2025-12-31 13:44:22.000', '2024-11-12 15:54:13.000');

INSERT INTO station (name, database_tag, is_public, registration_code, user_id, is_active) VALUES ('Test', 'MJS', 1, 123, null, null);