alter table telemechanic.ewb
    alter column request_id drop not null;
alter table telemechanic.ewb
    alter column start_time set not null;
alter table telemechanic.ewb
    alter column finish_time set not null;