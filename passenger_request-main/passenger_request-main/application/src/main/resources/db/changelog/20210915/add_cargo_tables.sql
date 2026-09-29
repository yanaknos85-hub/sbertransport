create table request.request_for_cargo
(
    id                       uuid                 not null
        constraint request_for_cargo_pkey
            primary key,
    humanreadableid          varchar(100)         not null
        constraint request_for_cargo_humanreadableid_key
            unique,
    author_id                uuid                 not null
        constraint fk_request_for_cargo_author
            references request.employee,
    sender_id                uuid                 not null
        constraint fk_request_for_cargo_sender
            references request.employee,
    recipient_id             uuid                 not null
        constraint fk_request_for_cargo_recipient
            references request.employee,
    sender_organization      varchar(128),
    recipient_organization   varchar(128),
    transport_type           varchar(255)         not null,
    segments                 jsonb,
    desired_date             timestamp            not null,
    cargo_type_id            uuid,
    cargo_type_other         varchar(50),
    cargo_nomenclature_id    uuid,
    cargo_nomenclature_other varchar(50),
    fragile                  boolean,
    length                   double precision,
    width                    double precision,
    height                   double precision,
    volume                   double precision,
    weight                   double precision,
    occupied_places_count    integer,
    source_loaders           boolean,
    destination_loaders      boolean,
    need_package             boolean,
    package_id               uuid,
    package_count            integer,
    tariff_id                uuid,
    expected_cost            double precision,
    expected_distance        double precision,
    expected_time            bigint,
    creation_time            timestamp,
    request_options          jsonb,
    passenger_id             uuid,
    trip_purpose             uuid,
    request_status           varchar(255),
    status_code              integer,
    approval_state           varchar(255),
    approved_by_id           uuid
        constraint fk_request_for_cargo_approved
            references request.employee,
    approval_date            timestamp,
    active                   boolean default true not null,
    comment                  varchar(500)
);

comment on column request.request_for_cargo.id is 'ID';
comment on column request.request_for_cargo.author_id is 'автор заявки';
comment on column request.request_for_cargo.sender_id is 'отправитель';
comment on column request.request_for_cargo.recipient_id is 'получатель';
comment on column request.request_for_cargo.sender_organization is 'организация отправитель';
comment on column request.request_for_cargo.recipient_organization is 'организация получатель';
comment on column request.request_for_cargo.desired_date is 'дата и время отправления';
comment on column request.request_for_cargo.cargo_type_id is 'идентификатор типа груза';
comment on column request.request_for_cargo.cargo_type_other is 'тип груза - другое';
comment on column request.request_for_cargo.cargo_nomenclature_id is 'идентификатор номенклатуры груза';
comment on column request.request_for_cargo.cargo_nomenclature_other is 'номенклатура груза - другое';
comment on column request.request_for_cargo.fragile is 'характер груза (хрупкий)';
comment on column request.request_for_cargo.transport_type is 'тип транспорта (тип доставки)';
comment on column request.request_for_cargo.length is 'общая длина груза';
comment on column request.request_for_cargo.width is 'общая ширина груза';
comment on column request.request_for_cargo.height is 'общая высота груза';
comment on column request.request_for_cargo.volume is 'общий объем груза';
comment on column request.request_for_cargo.weight is 'общий вес груза';
comment on column request.request_for_cargo.occupied_places_count is 'колличество мест';
comment on column request.request_for_cargo.source_loaders is 'грузчики в пункте погрузки';
comment on column request.request_for_cargo.destination_loaders is 'грузчики в пункте доставки';
comment on column request.request_for_cargo.need_package is 'требуется упаковка';
comment on column request.request_for_cargo.package_id is 'идентификатор упаковки';
comment on column request.request_for_cargo.package_count is 'колличество упаковок';
comment on column request.request_for_cargo.tariff_id is 'идентификатор тарифа';
comment on column request.request_for_cargo.active is 'Флаг активности';
comment on column request.request_for_cargo.comment is 'Комментарии';

create table request.request_for_cargo_history
(
    id                   uuid         not null
        constraint request_for_cargo_history_pkey
            primary key,
    change_date          timestamp    not null,
    code                 integer,
    comment              varchar(255),
    request_status       varchar(255) not null,
    request_for_cargo_id uuid         not null
        constraint fk_request_history_request_for_cargo
            references request.request_for_cargo,
    initiator_id         uuid         not null
        constraint fk_personal_history_employee
            references request.employee
);

create table request.cargo_detail
(
    id                   uuid not null
        constraint cargo_detail_pkey
            primary key,
    request_for_cargo_id uuid not null
        constraint fk_cargo_detail_request_for_cargo
            references request.request_for_cargo,
    length               double precision,
    width                double precision,
    height               double precision,
    volume               double precision,
    weight               double precision
);

comment on column request.cargo_detail.id is 'ID';
comment on column request.cargo_detail.request_for_cargo_id is 'идентификатор заявки на грузоперевозку';
comment on column request.cargo_detail.length is 'длина груза';
comment on column request.cargo_detail.width is 'ширина груза';
comment on column request.cargo_detail.height is 'высота груза';
comment on column request.cargo_detail.volume is 'объем груза';
comment on column request.cargo_detail.weight is 'вес груза';