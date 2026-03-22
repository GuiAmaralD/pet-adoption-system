ALTER TABLE users ADD COLUMN role_new VARCHAR(255);

UPDATE users
SET role_new = CASE role
    WHEN 0 THEN 'ADMIN'
    WHEN 1 THEN 'USER'
    ELSE NULL
END;

ALTER TABLE users DROP COLUMN role;
ALTER TABLE users RENAME COLUMN role_new TO role;

ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'USER'));
