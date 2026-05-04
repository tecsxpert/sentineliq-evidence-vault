ALTER TABLE users ADD COLUMN email VARCHAR(255);
ALTER TABLE users ADD COLUMN phone_number VARCHAR(255);

UPDATE users
SET email = username
WHERE email IS NULL AND username LIKE '%@%';

UPDATE users
SET email = CONCAT(username, '@example.local')
WHERE email IS NULL;

UPDATE users
SET phone_number = '9999999999'
WHERE phone_number IS NULL;

CREATE UNIQUE INDEX idx_users_email ON users(email);
