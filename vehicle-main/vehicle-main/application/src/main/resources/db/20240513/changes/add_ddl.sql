create table vehicle.odometer_value
(
    id              uuid
        constraint odometer_value_pk
            primary key,
    value     int       not null,
    transport_id    uuid
        constraint odometer_value_transport_id_fk
            references vehicle.transport,
    year            int  not null,
    month           smallint  not null,
    creation_date   timestamp not null,
    creator_user_id uuid      not null
        constraint odometer_value_employee_id_fk
            references vehicle.employee
);

comment on table vehicle.odometer_value is 'Показания одометра';

comment on column vehicle.odometer_value.id is 'Идентификатор записи';

comment on column vehicle.odometer_value.value is 'Показатель (в километрах)';

comment on column vehicle.odometer_value.transport_id is 'Ссылка на идентификатор транспортного средства';

comment on column vehicle.odometer_value.year is 'Год внесения показателей';

comment on column vehicle.odometer_value.month is 'Месяц внесения показателей';

comment on column vehicle.odometer_value.creation_date is 'Дата и время создания записи';

comment on column vehicle.odometer_value.creator_user_id is 'Идентификатор записи с таблицы corporate.user сотрудника, создавшего запись';

create index odometer_value_creator_index
    on vehicle.odometer_value (creator_user_id);

create index odometer_value_transport_id_index
    on vehicle.odometer_value (transport_id);

alter table vehicle.odometer_value
    add constraint odometer_value_pk_2
        unique (transport_id, year, month);