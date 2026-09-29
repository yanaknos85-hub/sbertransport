alter table telemechanic.driver
    add column if not exists contractor_id uuid;
alter table telemechanic.driver
    add column if not exists autopark_id uuid;

comment on column telemechanic.driver.contractor_id is 'Идентификатор контрагента';
comment on column telemechanic.driver.autopark_id is 'Идентификатор автопрка';