alter table telemechanic.request
    add column if not exists checks_started_time timestamp;

comment on column telemechanic.request.checks_started_time is 'Дата и время начала прохождения проверок';

-- Обновление комментария для checks_finished_time
comment on column telemechanic.request.checks_finished_time is 'Дата и время завершения прохождения проверок';