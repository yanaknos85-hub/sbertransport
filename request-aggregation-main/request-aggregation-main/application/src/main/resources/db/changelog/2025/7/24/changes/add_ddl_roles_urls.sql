create table if not exists request_aggregation.urls
(
    id      uuid not null
        constraint users_urls_pkey
            primary key,
    url     text not null,
    pattern text not null,
    method  text not null
);

comment
    on table request_aggregation.urls is 'Ссылка';

comment
    on column request_aggregation.urls.id is 'Идентификатор записи ссылки';

comment
    on column request_aggregation.urls.url is 'Адрес ссылки';

comment
    on column request_aggregation.urls.pattern is 'Паттерн ссылки';

comment
    on column request_aggregation.urls.method is 'Наименование REST метода';

create table if not exists request_aggregation.roles
(
    role   text not null,
    url_id uuid not null
        constraint role_urls_fkey
            references request_aggregation.urls,
    constraint user_roles_ukey
        unique (url_id, role)
);

comment
    on table request_aggregation.roles is 'Связка роль-ссылка';

comment
    on column request_aggregation.roles.role is 'Наименование роли';

comment
    on column request_aggregation.roles.url_id is 'Идентификатор записи о ссылке';