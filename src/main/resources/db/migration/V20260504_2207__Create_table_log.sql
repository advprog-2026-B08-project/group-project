CREATE TABLE activity_log (
    id UUID primary key,
    action VARCHAR(255) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    actorRole VARCHAR(30) NOT NULL,
    target VARCHAR(100),
    description VARCHAR(500),
    timestamp TIMESTAMP NOT NULL
);