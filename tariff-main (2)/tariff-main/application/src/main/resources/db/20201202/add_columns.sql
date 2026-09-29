alter table tariff.tariff
    add column if not exists active         boolean   not null default true,
    drop column if exists name;
comment on column tariff.tariff.active is 'Флаг активности, false - удален';






