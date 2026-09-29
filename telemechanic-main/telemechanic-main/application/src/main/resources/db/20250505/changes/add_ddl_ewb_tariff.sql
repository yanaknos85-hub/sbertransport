create table telemechanic.ewb_tariff
(
    tariff_id                   uuid
        constraint ewb_tariff_pk primary key,
    contract_id                 uuid
        constraint ewb_tariiff_contract_id_fk
            references telemechanic.ewb_contract (contract_id),
    organization_id uuid not null
        constraint ewb_tariff_organization_id_fk
            references telemechanic.organization (id),
    department_id   uuid not null
        constraint ewb_tariff_department_id_fk
            references telemechanic.department (id),
    active          boolean not null
);

comment on table telemechanic.ewb_tariff is 'Тариф ЭПЛ';

comment on column telemechanic.ewb_tariff.tariff_id is 'Идентификатор записи о тарифе';

comment on column telemechanic.ewb_tariff.contract_id is 'Идентификатор записи о договоре';

comment on column telemechanic.ewb_tariff.organization_id is 'Идентификатор записи об организации контрагента';

comment on column telemechanic.ewb_tariff.department_id is 'Идентификатор записи о подразделении контрагента';

comment on column telemechanic.ewb_tariff.active is 'Флаг активности';

create index if not exists ewb_tariff_organization_id_index
    on telemechanic.ewb_tariff (organization_id);

create index if not exists ewb_tariff_department_id_index
    on telemechanic.ewb_tariff (department_id);