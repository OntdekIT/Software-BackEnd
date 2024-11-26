-- Rename workshopcode table to workshop
RENAME TABLE workshopcode TO workshop;

-- Add foreign key constraint to user table for workshop_code
ALTER TABLE user ADD COLUMN workshop_code BIGINT(20) NULL;
ALTER TABLE user ADD CONSTRAINT user_workshop_FK FOREIGN KEY (workshop_code) REFERENCES workshop(code) ON DELETE SET NULL ON UPDATE SET NULL;
