
create table if not exists tariff.contract_organizations (
    contract_id uuid not null,
    organization_id uuid not null,
    primary key (contract_id, organization_id),
    foreign key (contract_id) references tariff.contract (id)
        on update cascade on delete cascade,
    foreign key (organization_id) references tariff.organization (id)
);
COMMENT ON TABLE tariff.contract_organizations is 'Таблица связей контракт-организация';

alter table tariff.contract
    add column contract_number    varchar,
    add column include_vat        boolean,
    add column vat_value          int4;

comment on column tariff.contract.contract_number is 'Номер контракта';
comment on column tariff.contract.include_vat is 'Флаг наличия НДС';
comment on column tariff.contract.vat_value is 'НДС значение';

alter table tariff.contract
    drop column organization_id;

