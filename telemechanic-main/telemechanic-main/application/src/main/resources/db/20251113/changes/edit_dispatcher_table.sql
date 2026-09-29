alter table telemechanic.dispatcher
    add column if not exists contractor_id uuid;
alter table telemechanic.dispatcher
    add column if not exists autopark_id uuid;

comment on column telemechanic.dispatcher.contractor_id is 'Идентификатор автопарка';
comment on column telemechanic.dispatcher.autopark_id is 'Идентификатор филиала';