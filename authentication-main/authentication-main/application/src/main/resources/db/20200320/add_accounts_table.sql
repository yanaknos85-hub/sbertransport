CREATE TABLE authentication.account
(
    id    uuid PRIMARY KEY,
    login varchar(255) NOT NULL UNIQUE,
    hash  varchar(255) NOT NULL
);