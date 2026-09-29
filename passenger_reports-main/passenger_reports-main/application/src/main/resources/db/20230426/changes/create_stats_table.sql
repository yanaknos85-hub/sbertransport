create table reports.stats
(
    id                       uuid                  not null         constraint reports_stats_pkey             primary key,
    organization_id          uuid,
    year                     integer,
    month                    integer,
    service_type             varchar(255),
    transport_type           varchar(255),
    total_executed           bigint,
    total_canceled           bigint,
    total_not_executed       bigint,
    total_sum                bigint,
    ---sla_norm                 integer,
    ---sla_fact                 integer,
    sla_without_violation    bigint,
    sla_with_violation       bigint,
    ---csi_norm                 integer,
    ---csi_fact                 integer,
    ---csi_evaluated            integer,
    csi_star_positive         bigint,
    csi_star_negative         bigint,
    creation_time            timestamp                not null
);

comment on column reports.stats.id is 'ID';
comment on column reports.stats.organization_id is 'организация';
comment on column reports.stats.year is 'год';
comment on column reports.stats.month is 'месяц';
comment on column reports.stats.service_type is 'тип обслуживания';
comment on column reports.stats.transport_type is 'тип транспорта';
comment on column reports.stats.total_executed is 'Выполненные успешно заявки количество';
comment on column reports.stats.total_canceled is 'Отмененные заявки количество';
comment on column reports.stats.total_not_executed is 'Не выполенные заявки с промежуточными статусами количество';
comment on column reports.stats.total_sum is 'Сумма по всем заявкам кроме заявок со статусом canceled в рублях';
---comment on column reports.stats.sla_norm is 'Фиксированное значение sla';
---comment on column reports.stats.sla_fact is 'Фактическое значение sla процент';
comment on column reports.stats.sla_without_violation is 'Заявки без нарушений контрольных сроков количество';
comment on column reports.stats.sla_with_violation is 'Заявки с нарушениями контрольных сроков количество';
---comment on column reports.stats.csi_norm is 'Фиксированное значение csi';
---comment on column reports.stats.csi_fact is 'Фактическое значение CSI процент';
---comment on column reports.stats.csi_evaluated is 'Заявки с оценками количество';
comment on column reports.stats.csi_star_positive is 'Заявки с 4-5 звезд количество';
comment on column reports.stats.csi_star_negative is 'Заявки с 1-3 звезд количество';


create table reports.cargo_detail_stats
(
    id                    uuid not null          constraint cargo_detail_stats_pkey              primary key,
    stats_id  uuid not null          constraint fk_cargo_detail_stats_stats             references reports.stats,
    position              integer,
    fragile               boolean,
    need_package          boolean,
    package_id            uuid,
    package_count         integer,
    cargo_name            varchar(255),
    cargo_type            varchar(50),
    cargo_category        varchar(50)
);

comment on column reports.cargo_detail_stats.id is 'ID';
comment on column reports.cargo_detail_stats.stats_id is 'идентификатор заявки на грузоперевозку';
comment on column reports.cargo_detail_stats.position is 'порядковый номер';
comment on column reports.cargo_detail_stats.fragile is 'характер груза (хрупкий)';
comment on column reports.cargo_detail_stats.need_package is 'требуется упаковка';
comment on column reports.cargo_detail_stats.package_id is 'идентификатор упаковки';
comment on column reports.cargo_detail_stats.package_count is 'колличество упаковок';
comment on column reports.cargo_detail_stats.cargo_name is 'наименование груза';
comment on column reports.cargo_detail_stats.cargo_type is 'тип груза';
comment on column reports.cargo_detail_stats.cargo_category is 'категория груза';



