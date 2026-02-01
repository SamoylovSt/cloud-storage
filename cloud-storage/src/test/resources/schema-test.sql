CREATE TABLE IF NOT EXISTS users
(
    id
    BIGINT
    GENERATED
    ALWAYS AS
    IDENTITY
    PRIMARY
    KEY,
    name
    VARCHAR
(
    30
) NOT NULL UNIQUE,
    password VARCHAR
(
    100
) NOT NULL
    );