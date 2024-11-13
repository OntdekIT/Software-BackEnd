-- Rename workshopcode table to workshop
RENAME TABLE workshopcode TO workshop;

-- Add foreign key constraint to user table for workshop_code
ALTER TABLE user ADD workshop_code varchar(100) NULL;
ALTER TABLE user ADD CONSTRAINT user_workshop_FK FOREIGN KEY (workshop_code) REFERENCES workshop(code) ON DELETE SET NULL;
