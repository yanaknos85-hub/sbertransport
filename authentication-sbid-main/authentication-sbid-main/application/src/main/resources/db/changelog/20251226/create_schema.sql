create schema authorization_sbid;
comment on schema authorization_sbid is 'Авторизация через Sber business ID';

create table authorization_sbid.sessions
(
    id      uuid not null
        constraint users_urls_pkey
            primary key,
    url     text not null,
    token text not null,
    nounce  text not null,
    state  text not null
);