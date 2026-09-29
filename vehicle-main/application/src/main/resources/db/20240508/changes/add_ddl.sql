create table vehicle.fuel_consumption
(
    id              uuid
        constraint fuel_consumption_pk
            primary key,
    consumption     int       not null,
    transport_id    uuid
        constraint fuel_consumption_transport_id_fk
            references vehicle.transport,
    year            int  not null,
    month           smallint  not null,
    creation_date   timestamp not null,
    creator_user_id uuid      not null
        constraint fuel_consumption_employee_id_fk
            references vehicle.employee
);

comment on table vehicle.fuel_consumption is 'Расход топлива';

comment on column vehicle.fuel_consumption.id is 'Идентификатор записи';

comment on column vehicle.fuel_consumption.consumption is 'Расход (в литрах)';

comment on column vehicle.fuel_consumption.transport_id is 'Ссылка на идентификатор транспортного средства';

comment on column vehicle.fuel_consumption.year is 'Год внесения показателей';

comment on column vehicle.fuel_consumption.month is 'Месяц внесения показателей';

comment on column vehicle.fuel_consumption.creation_date is 'Дата и время создания записи';

comment on column vehicle.fuel_consumption.creator_user_id is 'Идентификатор записи с таблицы corporate.user сотрудника, создавшего запись';

create index fuel_consumption_creator_index
    on vehicle.fuel_consumption (creator_user_id);

create index fuel_consumption_transport_id_index
    on vehicle.fuel_consumption (transport_id);

alter table vehicle.fuel_consumption
    add constraint fuel_consumption_pk_2
        unique (transport_id, year, month);