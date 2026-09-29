ALTER TABLE reports.request
    ADD COLUMN time_zone varchar(100);

comment on column reports.request.time_zone is 'Таймзона созданной заявки';