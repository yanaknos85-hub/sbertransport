update tariff.tariff set contractor_id = null;
alter table tariff.tariff
    rename column contractor_id to contract_id;
comment on column tariff.tariff.contract_id is 'id контракта';
alter table tariff.contract
    alter column end_date drop not null;



