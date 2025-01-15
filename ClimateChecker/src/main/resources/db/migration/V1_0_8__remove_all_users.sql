ALTER TABLE ontdekstation013.token DROP FOREIGN KEY FKp2lrydh553poqd4jq5f2gxh5u;
ALTER TABLE ontdekstation013.token ADD CONSTRAINT token_user_FK FOREIGN KEY (user_id) REFERENCES ontdekstation013.`user`(user_id) ON DELETE CASCADE ON UPDATE CASCADE;

DELETE FROM user;