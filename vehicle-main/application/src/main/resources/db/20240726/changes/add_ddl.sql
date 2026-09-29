create table if not exists vehicle.odometer_history
(
    id              uuid
        constraint odometer_history_pk
            primary key,
    creation_time   timestamp                     not null,
    transport_id    uuid
        constraint odometer_history_transport_id_fk
            references vehicle.transport          not null,
    creator_user_id uuid
        constraint odometer_history_employee_user_id_fk
            references vehicle.employee (user_id) not null,
    value           int                           not null
);

comment on table vehicle.odometer_history is 'История изменения показаний одометра';

comment on column vehicle.odometer_history.id is 'Идентификатор записи';

comment on column vehicle.odometer_history.creation_time is 'Дата и время добавления записи';

comment on column vehicle.odometer_history.transport_id is 'Идентификатор автомобиля';

comment on column vehicle.odometer_history.creator_user_id is 'Идентификатор сортудника изменившего позказания одометра';

comment on column vehicle.odometer_history.value is 'Показания одометра';
