alter table tariff.tariff add column if not exists work_group varchar;

comment on column tariff.tariff.work_group is 'Рабочая группа';