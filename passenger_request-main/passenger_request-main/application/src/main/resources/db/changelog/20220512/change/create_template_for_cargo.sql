create table request.template_for_cargo
(
    id uuid not null
        constraint template_for_cargo_pk
            primary key,
    humanreadableid varchar(100),
    creation_time timestamp,
    begin_date date,
    end_date date,
    cron_expression varchar(100),
    template jsonb,
    active boolean
);

comment on table request.template_for_cargo is 'Таблица для шаблонов и расписания';
comment on column request.template_for_cargo.creation_time is 'Дата создания или изменения шаблона';
comment on column request.template_for_cargo.begin_date is 'Начало действия шаблона';
comment on column request.template_for_cargo.end_date is 'Окончание действия шаблона';
comment on column request.template_for_cargo.cron_expression is 'крон-выражение для расписания';
comment on column request.template_for_cargo.template is 'Шаблон для создания заявок';