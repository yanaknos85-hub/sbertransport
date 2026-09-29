alter table tariff.tariff
    rename column contract_id to contractor_id;
comment on column tariff.tariff.contractor_id is 'id подрядчика';
