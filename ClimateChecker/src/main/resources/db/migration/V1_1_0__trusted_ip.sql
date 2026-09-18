-- Vertrouwde IP-adressen per gebruiker. Als een gebruiker vanaf een vertrouwd
-- IP inlogt, mag de mail-verificatiestap (tweede factor) worden overgeslagen.
-- Het IP wordt niet in leesbare vorm opgeslagen maar als HMAC-hash (met een
-- server-side pepper), zodat de waarde nutteloos is als de database lekt.
CREATE TABLE trusted_ip (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    ip_hash    VARCHAR(128) NOT NULL,
    created_at DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_trusted_ip_user FOREIGN KEY (user_id) REFERENCES user (user_id) ON DELETE CASCADE,
    CONSTRAINT uq_trusted_ip_user_hash UNIQUE (user_id, ip_hash)
);
