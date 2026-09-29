create table request.request_for_import
(
    id uuid,
    count_all int,
    count int,
    date date,
    status varchar,
    description varchar,
    dto_json jsonb
);

comment on table request.request_for_import is 'Временная таблица для массовой загрузки';

create unique index request_for_import_id_uindex
    on request.request_for_import (id);

alter table request.request_for_import
    add constraint request_for_import_pk
        primary key (id);