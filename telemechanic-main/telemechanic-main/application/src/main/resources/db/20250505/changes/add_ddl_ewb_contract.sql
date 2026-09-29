create table telemechanic.ewb_contract
(
    contract_id                     uuid
        constraint ewb_contract_pk primary key,
    organization_id      uuid        not null
        constraint ewb_contract_organization_id_fk
            references telemechanic.organization (id),
    inspection_type                 varchar(50) not null,
    edf_operator_id                 varchar(10) not null,
    edf_code                        varchar(50) not null,
    organization_medical_license_id uuid
        constraint ewb_contract_organization_medical_license_id_fk
            references telemechanic.organization_medical_license (id),
    active          boolean not null
);

comment on table telemechanic.ewb_contract is 'Договор ЭПЛ';

comment on column telemechanic.ewb_contract.contract_id is 'Идентификатор записи о договоре';

comment on column telemechanic.ewb_contract.organization_id is 'Идентификатор записи об организации контрагента';

comment on column telemechanic.ewb_contract.inspection_type is 'Вид осмотра';

comment on column telemechanic.ewb_contract.edf_operator_id is 'Идентификатор записи об операторе ЭДО';

comment on column telemechanic.ewb_contract.edf_code is 'Код участника';

comment on column telemechanic.ewb_contract.organization_medical_license_id is 'Идентификатор записи об медицинской лицензии организации';

comment on column telemechanic.ewb_contract.active is 'Флаг активности';

create index if not exists ewb_contract_edf_operator_id_index
    on telemechanic.ewb_contract (edf_operator_id);

create index if not exists ewb_contract_organization_medical_license_id_index
    on telemechanic.ewb_contract (organization_medical_license_id);