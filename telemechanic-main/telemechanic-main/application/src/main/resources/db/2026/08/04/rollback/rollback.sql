-- Удаление колонки checks_started_time
alter table telemechanic.request
    drop column if exists checks_started_time;

-- Возвращение старого комментария для checks_finished_time
comment on column telemechanic.request.checks_finished_time is 'Дата и время окончания осмотра';