alter table tariff.organization add column active boolean default true not null;
comment on column tariff.organization.active is 'Флаг активности(неудаленности)';


