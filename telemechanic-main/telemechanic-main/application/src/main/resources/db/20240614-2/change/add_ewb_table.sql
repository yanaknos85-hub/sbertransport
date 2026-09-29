create table if not exists telemechanic.ewb
(
    id                    uuid primary key,
    author_id             uuid        not null,
    medic_id              uuid,
    request_id            uuid        not null,
    file_name             varchar(32) not null,
    creation_time         timestamp   not null,
    status                varchar(32) not null,
    human_readable_id     varchar(36) not null,
    ewb_uuid              varchar(36) not null,
    start_time            timestamp,
    finish_time           timestamp,
    organization_id       uuid        not null,
    transport_id          uuid        not null,
    driver_id             uuid        not null,
    driver_license_id     uuid        not null,
    telemech_out_id       uuid,
    telemech_decision_out timestamp,
    telemech_in_id        uuid,
    telemech_decision_in  timestamp,
    attorney_out_id       uuid
);

comment on table telemechanic.ewb is 'ЭПЛ';
comment on column telemechanic.ewb.id is 'Идентификатор записи ЭПЛ';
comment on column telemechanic.ewb.author_id is 'Идентификатор создателя ЭПЛ';
comment on column telemechanic.ewb.medic_id is 'Идентификатор медика';
comment on column telemechanic.ewb.request_id is 'Идентификатор заявки проверок телемеханика';
comment on column telemechanic.ewb.file_name is 'Идентификатор файла (название файла)';
comment on column telemechanic.ewb.creation_time is 'Дата и время создания ЭПЛ';
comment on column telemechanic.ewb.status is 'Статус ЭПЛ';
comment on column telemechanic.ewb.human_readable_id is 'Номер путевого листа';
comment on column telemechanic.ewb.ewb_uuid is 'Уникальный идентификатор документа путевого листа';
comment on column telemechanic.ewb.start_time is 'Дата путевого листа';
comment on column telemechanic.ewb.finish_time is 'Дата окончания срока использования путевого листа';
comment on column telemechanic.ewb.organization_id is 'Идентификатор организации';
comment on column telemechanic.ewb.transport_id is 'Идентификатор автомобиля';
comment on column telemechanic.ewb.driver_id is 'Идентификатор водителя';
comment on column telemechanic.ewb.driver_license_id is 'Идентификатор водительских прав';
comment on column telemechanic.ewb.telemech_out_id is 'Идентификатор телемеханика, который выпустил водителя на линию';
comment on column telemechanic.ewb.telemech_decision_out is 'Дата и время принятия решения о выпуске на линию';
comment on column telemechanic.ewb.telemech_in_id is 'Идентификатор телемеханика, который закрыл путевой лист';
comment on column telemechanic.ewb.telemech_decision_in is 'Дата и время принятия автомобиля в гараж';
comment on column telemechanic.ewb.attorney_out_id is 'Идентификатор МЧД телемеханика, который выпустил водителя на линию';