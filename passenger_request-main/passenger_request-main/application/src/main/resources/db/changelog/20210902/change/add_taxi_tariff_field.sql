alter table request.taxi_tariff add column if not exists contractor_id uuid;
comment on column request.taxi_tariff.contractor_id is 'Id контрагента';

