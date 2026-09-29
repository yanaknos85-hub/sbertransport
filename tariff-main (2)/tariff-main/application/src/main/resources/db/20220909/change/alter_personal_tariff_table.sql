alter table tariff.tariff add column trust_idx  float8 default 0;
comment on column tariff.tariff.trust_idx is 'Индекс затрат на страхование, руб./км';