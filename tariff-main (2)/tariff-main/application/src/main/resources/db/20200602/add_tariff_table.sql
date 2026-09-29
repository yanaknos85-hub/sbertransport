CREATE TABLE tariff.tariff
(
    id                uuid PRIMARY KEY,
    name              varchar(255)     NOT NULL UNIQUE,
    region            varchar(255)     NOT NULL,
    price_per_mile    double precision NOT NULL,
    transport_type_id uuid CONSTRAINT tariff_transport_type_fk REFERENCES tariff.transport_type (id),
    price_details     json
)