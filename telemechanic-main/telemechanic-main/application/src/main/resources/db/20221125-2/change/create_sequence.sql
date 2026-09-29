

create table telemechanic.company_sq
(
    id         uuid                                   not null
        primary key,
    prefix     varchar(2)                             not null,
    orgdigitid numeric                                not null,
    sq         numeric                                not null,
    dt_insert  timestamp with time zone default now() not null,
    dt_modify  timestamp with time zone default now() not null
);

comment
    on table telemechanic.company_sq is 'Таблица для формирования последовательностей для компаний';

comment
    on column telemechanic.company_sq.id is 'Уникальный идентификатор (первичный ключ)';

comment
    on column telemechanic.company_sq.prefix is 'Кодовое обозначение типа сущности';

comment
    on column telemechanic.company_sq.orgdigitid is 'Уникальный идентификатор (числовой)';

comment
    on column telemechanic.company_sq.sq is 'Порядковый номер (в рамках клиента)';

comment
    on column telemechanic.company_sq.dt_insert is 'Дата время вставки записи';

comment
    on column telemechanic.company_sq.dt_modify is 'Дата время модификации записи';

create unique index ux_company_sq_1
    on telemechanic.company_sq (prefix, orgdigitid, sq);

create unique index ux_company_sq_2
    on telemechanic.company_sq (prefix, orgdigitid);