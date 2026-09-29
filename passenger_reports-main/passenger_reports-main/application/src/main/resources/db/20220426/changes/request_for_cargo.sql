create table reports.request_for_cargo
(
    id                     uuid                 not null
        constraint reports_for_cargo_pkey
            primary key,
    humanreadableid        varchar(100)
        constraint reports_for_cargo_humanreadableid_key
            unique,
    sender_id              uuid
        constraint fk_reports_for_cargo_sender
            references reports.employee,
    recipient_id           uuid
        constraint fk_reports_for_cargo_recipient
            references reports.employee,
    sender_organization    varchar(128),
    recipient_organization varchar(128),
    transport_type         varchar(255)         ,
    desired_date           timestamp            ,
    length                 double precision,
    width                  double precision,
    height                 double precision,
    volume                 double precision,
    weight                 double precision,
    occupied_places_count  integer,
    active                 boolean default true not null,
    comment                varchar(500)
);

comment on column reports.request_for_cargo.id is 'ID';
comment on column reports.request_for_cargo.sender_id is 'отправитель';
comment on column reports.request_for_cargo.recipient_id is 'получатель';
comment on column reports.request_for_cargo.sender_organization is 'организация отправитель';
comment on column reports.request_for_cargo.recipient_organization is 'организация получатель';
comment on column reports.request_for_cargo.transport_type is 'тип транспорта (тип доставки)';
comment on column reports.request_for_cargo.desired_date is 'дата и время отправления';
comment on column reports.request_for_cargo.length is 'общая длина груза';
comment on column reports.request_for_cargo.width is 'общая ширина груза';
comment on column reports.request_for_cargo.height is 'общая высота груза';
comment on column reports.request_for_cargo.volume is 'общий объем груза';
comment on column reports.request_for_cargo.weight is 'общий вес груза';
comment on column reports.request_for_cargo.occupied_places_count is 'колличество мест';
comment on column reports.request_for_cargo.active is 'Флаг активности';
comment on column reports.request_for_cargo.comment is 'Комментарии';

create table reports.cargo_detail
(
    id                    uuid not null
        constraint cargo_detail_pkey
            primary key,
    request_for_cargo_id  uuid not null
        constraint fk_cargo_detail_request_for_cargo
            references reports.request_for_cargo,
    position              integer,
    fragile               boolean,
    need_package          boolean,
    package_id            uuid,
    package_count         integer,
    cargo_name            varchar(255),
    cargo_type            varchar(50),
    cargo_category        varchar(50)
);

comment on column reports.cargo_detail.id is 'ID';
comment on column reports.cargo_detail.request_for_cargo_id is 'идентификатор заявки на грузоперевозку';
comment on column reports.cargo_detail.position is 'порядковый номер';
comment on column reports.cargo_detail.fragile is 'характер груза (хрупкий)';
comment on column reports.cargo_detail.need_package is 'требуется упаковка';
comment on column reports.cargo_detail.package_id is 'идентификатор упаковки';
comment on column reports.cargo_detail.package_count is 'колличество упаковок';
comment on column reports.cargo_detail.cargo_name is 'наименование груза';
comment on column reports.cargo_detail.cargo_type is 'тип груза';
comment on column reports.cargo_detail.cargo_category is 'категория груза';





