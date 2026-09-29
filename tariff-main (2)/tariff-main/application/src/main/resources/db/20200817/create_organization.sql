create table if not exists tariff.organization
(
    id       uuid    not null
        constraint organization_pkey
            primary key,
    digit_id numeric not null
        constraint ux_organization_1
            unique
);
COMMENT ON TABLE tariff.organization is 'Справочник организаций';
comment on column tariff.organization.id is 'Уникальный идентификатор';
comment on column tariff.organization.digit_id is 'Уникальный идентификатор (числовой ID)';

