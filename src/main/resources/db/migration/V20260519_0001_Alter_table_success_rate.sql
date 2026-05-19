ALTER TABLE users
    ADD COLUMN successfully_sold INTEGER DEFAULT 0;

ALTER TABLE users
    ADD COLUMN tried_to_sell INTEGER DEFAULT 0;