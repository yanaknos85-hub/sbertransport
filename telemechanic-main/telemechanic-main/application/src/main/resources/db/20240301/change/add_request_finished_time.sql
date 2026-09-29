alter table telemechanic.request
    add column checks_finished_time timestamp;

comment on column telemechanic.request.checks_finished_time is 'Дата и время окончания осмотра';
