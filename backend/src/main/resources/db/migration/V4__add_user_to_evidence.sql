ALTER TABLE evidence ADD COLUMN user_id BIGINT;

ALTER TABLE evidence
ADD CONSTRAINT fk_user
FOREIGN KEY (user_id) REFERENCES users(id);