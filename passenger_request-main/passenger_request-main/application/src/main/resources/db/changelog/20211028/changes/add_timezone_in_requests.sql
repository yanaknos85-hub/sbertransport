ALTER TABLE request.request
    ADD COLUMN time_zone varchar(100);

ALTER TABLE request.request_for_carsharing
    ADD COLUMN time_zone varchar(100);

ALTER TABLE request.request_for_personal
    ADD COLUMN time_zone varchar(100);

ALTER TABLE request.request_for_public
    ADD COLUMN time_zone varchar(100);

ALTER TABLE request.request_for_taxi
    ADD COLUMN time_zone varchar(100);

ALTER TABLE request_audit.request_for_public
    ADD COLUMN time_zone varchar(100);

comment on column request.request.time_zone is 'Таймзона созданной заявки';
comment on column request.request_for_carsharing.time_zone is 'Таймзона созданной заявки';
comment on column request.request_for_personal.time_zone is 'Таймзона созданной заявки';
comment on column request.request_for_public.time_zone is 'Таймзона созданной заявки';
comment on column request.request_for_taxi.time_zone is 'Таймзона созданной заявки';
comment on column request_audit.request_for_public.time_zone is 'Таймзона созданной заявки';
