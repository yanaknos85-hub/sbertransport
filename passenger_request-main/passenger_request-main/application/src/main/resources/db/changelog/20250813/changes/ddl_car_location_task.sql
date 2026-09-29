create table if not exists request.car_location_task (
    id uuid primary key,
    request_id uuid not null,
    order_partner_id varchar(50),
    contractor_id uuid,
    created_at timestamp not null,
    active boolean not null,

    constraint fk_request_id foreign key (request_id) references request.request_for_taxi(id)
);

create index if not exists idx_car_location_task_request_id on request.car_location_task(request_id);
create index if not exists idx_car_location_task_order_partner_id on request.car_location_task(order_partner_id);

comment on table request.car_location_task is 'Таблица задач для получения местоположения водителя через интеграцию';
comment on column request.car_location_task.id is 'Идентификатор задачи';
comment on column request.car_location_task.request_id is 'Идентификатор запроса';
comment on column request.car_location_task.order_partner_id is 'Идентификатор заявки в системе контрагента';
comment on column request.car_location_task.contractor_id is 'Идентификатор контрагента';
comment on column request.car_location_task.created_at is 'Дата и время создания задачи (часовой пояс UTC)';
comment on column request.car_location_task.active is 'Флаг активности задачи (true - активна, false - завершена/отменена)';