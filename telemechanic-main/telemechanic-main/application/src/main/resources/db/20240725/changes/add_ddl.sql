create table if not exists telemechanic.medical_license
(
    id          uuid
        constraint medical_license_pk
            primary key,
    medic_id    uuid
        constraint medic_employee_id_fk
            references telemechanic.employee not null,
    series      varchar(20)             not null,
    number      int                     not null,
    issue_date  date                    not null,
    expiry_date date                    not null
);

comment on table telemechanic.medical_license is 'Медицинская лицензия';

comment on column telemechanic.medical_license.id is 'Идентификатор записи';

comment on column telemechanic.medical_license.medic_id is 'Идентификатор медика';

comment on column telemechanic.medical_license.series is 'Серия лицензии';

comment on column telemechanic.medical_license.number is 'Номер лицензии';

comment on column telemechanic.medical_license.issue_date is 'Дата выдачи лицензии';

comment on column telemechanic.medical_license.expiry_date is 'Дата окончания срока действия лицензии';