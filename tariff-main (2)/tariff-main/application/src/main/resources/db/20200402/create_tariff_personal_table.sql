CREATE TABLE tariff.tariff_personal
(
    id                   uuid PRIMARY KEY,
    name                 varchar(255)     NOT NULL UNIQUE,
    region               varchar(255)     NOT NULL,
    price_per_mile       double precision NOT NULL,
    reward_for_passenger double precision NOT NULL,
    season_coefficient   double precision NOT NULL,
    season_start         date             NOT NULL,
    season_end           date             NOT NULL
)