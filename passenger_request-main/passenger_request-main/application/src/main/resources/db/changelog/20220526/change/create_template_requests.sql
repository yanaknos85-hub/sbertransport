create table request.template_requests
(
    id uuid not null
        constraint template_requests_pk
            primary key,
    template_id uuid
        constraint fk_template_template_id
        references request.template_for_cargo,
    request_id uuid,
    date_delivery timestamp,
    expected_cost double precision
);

comment on table request.template_requests is 'Таблица для генерации будущих заявок';
comment on column request.template_requests.template_id is 'Идентификатор шаблона';
comment on column request.template_requests.request_id is 'Идентификатор будущей заявки';
comment on column request.template_requests.date_delivery is 'Дата доставки по заявке';
comment on column request.template_requests.expected_cost is 'Сумма заявки';
