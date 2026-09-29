create table if not exists telemechanic.medic_contractor (
    id uuid primary key,
    full_name varchar(255) not null,
    personnel_number varchar(255) not null,
    organization varchar(255) not null,
    department varchar (255) not null,
    position varchar(255) not null,
    sign_key_number varchar(255) not null,
    sign_key_end_date date not null
);

create unique index if not exists medic_contractor_personnel_number_uindex on telemechanic.medic_contractor(personnel_number);

comment on table telemechanic.medic_contractor is 'Таблица медицинских сотрудников сторонних организаций';

comment on column telemechanic.medic_contractor.id is 'Идентификатор';
comment on column telemechanic.medic_contractor.full_name is 'ФИО';
comment on column telemechanic.medic_contractor.personnel_number is 'Табельный номер';
comment on column telemechanic.medic_contractor.organization is 'Организация';
comment on column telemechanic.medic_contractor.department is 'Подразделение';
comment on column telemechanic.medic_contractor.position is 'Должность';
comment on column telemechanic.medic_contractor.sign_key_number is 'Номер ключа электронной подписи';
comment on column telemechanic.medic_contractor.sign_key_end_date is 'Дата окончания действия ключа электронной подписи';