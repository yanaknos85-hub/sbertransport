CREATE TABLE tariff.transport_type
(
    id      uuid PRIMARY KEY,
    name    varchar(255) NOT NULL,
    formula text
)