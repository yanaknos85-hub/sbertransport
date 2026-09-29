ALTER TABLE only tariff.tariff alter COLUMN season_start set DEFAULT '2000-01-01'::date;
ALTER TABLE only tariff.tariff alter COLUMN season_end set DEFAULT '2000-12-31'::date;

