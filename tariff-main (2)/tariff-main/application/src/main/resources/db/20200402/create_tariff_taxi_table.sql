CREATE TABLE tariff.tariff_taxi
(
    id                uuid PRIMARY KEY,
    name              varchar(255)     NOT NULL UNIQUE,
    region            varchar(255)     NOT NULL,
    price_per_mile    double precision NOT NULL,
    trip_class_id     uuid             NOT NULL,
    minute_price      double precision NOT NULL,
    waiting_price     double precision NOT NULL,
    free_waiting_time bigint           NOT NULL,
    submission_price  double precision NOT NULL,
    min_mileage       int              NOT NULL,
    min_time          bigint           NOT NULL
)