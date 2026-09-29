create table request.request_integration_ext
(
    request_id          uuid not null
        constraint request_integration_ext_pkey
            primary key,
    contractor_name     text,
    contractor_rus_name text,
    integration_email   text,
    workgroup           text,
    integration_type    text
);

comment on column request.request_integration_ext.request_id is 'Идентификатор заявки';
comment on column request.request_integration_ext.contractor_name is 'Название контрагента латиницей (указывается при интеграции в имени файла)';
comment on column request.request_integration_ext.contractor_rus_name is 'Название контрагента кириллицей (указывается при интеграции в поле ИСПОЛНИТЕЛЬ)';
comment on column request.request_integration_ext.integration_email is 'Электронная почта, используемая для почтовой интеграции';
comment on column request.request_integration_ext.workgroup is 'Рабочая группа';
comment on column request.request_integration_ext.integration_type is 'Тип интеграционного взаимодействия';