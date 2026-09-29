create table if not exists vehicle.driving_license
(
    id          uuid primary key    not null,
    driver_id   uuid
        references vehicle.employee not null,
    series      varchar(40)         not null,
    number      int                 not null,
    issue_date  date                not null,
    expiry_date date                not null,
    previous_id uuid
);

comment on table vehicle.driving_license is 'Водительские права';
comment on column vehicle.driving_license.id is 'Идентификатор водительских прав';
comment on column vehicle.driving_license.driver_id is 'Идентификатор водителя';
comment on column vehicle.driving_license.series is 'Серия водительских прав';
comment on column vehicle.driving_license.number is 'Номер водительских прав';
comment on column vehicle.driving_license.issue_date is 'Дата выдачи водительских прав';
comment on column vehicle.driving_license.expiry_date is 'Дата истечения водительских прав';
comment on column vehicle.driving_license.previous_id is 'Идентификатор предыдущего водительских прав';