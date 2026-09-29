alter table tariff.tariff
    add column contractor_id uuid;
comment on column tariff.tariff.contractor_id is 'ид контрагента';