--  Auto-generated SQL script #202501071119
INSERT INTO ontdekstation013.`user` (`role`, first_name, last_name, email, password)
VALUES (1, 'Test', 'Account', 'ontdekstation.test@gmail.com',
        '$argon2id$v=19$m=65536,t=4,p=8$gD/Masqa2eGqzPndNCWk9A$awOhYzNNIsZYPJ7HlvjTn6GcYmQwU7JeO/rmDALDeh0');
UPDATE ontdekstation013.`user`
SET last_name='Account'
WHERE user_id = 22;
