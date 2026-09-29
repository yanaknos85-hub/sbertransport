create table request.dispatcher
(
    id                          uuid                 not null
        constraint dispatcher_pkey
            primary key,
    humanreadableid   varchar(128)         not null
            constraint dispatcher_humanreadableid_key
                unique,
    user_id                     uuid,
    contractor_id               uuid
);

comment on column request.dispatcher.user_id is 'Пользователь';
comment on column request.dispatcher.contractor_id is 'Контрагент';