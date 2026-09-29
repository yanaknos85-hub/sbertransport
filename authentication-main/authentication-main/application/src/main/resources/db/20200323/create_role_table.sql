CREATE TABLE authentication.role
(
    code        varchar(255) PRIMARY KEY,
    name        varchar(255) NOT NULL UNIQUE,
    description text,
    "default"   boolean DEFAULT FALSE
);