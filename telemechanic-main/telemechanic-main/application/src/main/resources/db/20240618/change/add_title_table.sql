create table if not exists telemechanic.title
(
    id            uuid primary key,
    korus_id      uuid         not null,
    chain_id      uuid         not null,
    ewb_id        uuid         not null,
    doc_number    int          not null,
    file_name     varchar(255)  not null,
    creation_time timestamp    not null,
    send_time     timestamp    not null,
    accepted_time timestamp    not null,
    s3_name       varchar(255) not null
);

comment on table telemechanic.title is 'Титулы';
comment on column telemechanic.title.id is 'Идентификатор записи';
comment on column telemechanic.title.korus_id is 'Идентификатор в системе КОРУС';
comment on column telemechanic.title.chain_id is 'Идентификатор цепочки документов';
comment on column telemechanic.title.ewb_id is 'Идентификатор записи ЭПЛ';
comment on column telemechanic.title.doc_number is 'Номер титула';
comment on column telemechanic.title.file_name is 'Название файла';
comment on column telemechanic.title.creation_time is 'Дата и время создания титула';
comment on column telemechanic.title.send_time is 'Дата и время отправки титула';
comment on column telemechanic.title.accepted_time is 'Дата и время принятия архива КОРУСом';
comment on column telemechanic.title.s3_name is 'Название файла в s3';