ALTER TABLE station DROP FOREIGN KEY FKjpfw49q2hwosivlesvqxjy6qo;
ALTER TABLE station ADD CONSTRAINT station_user_FK FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE SET NULL ON UPDATE SET NULL;
