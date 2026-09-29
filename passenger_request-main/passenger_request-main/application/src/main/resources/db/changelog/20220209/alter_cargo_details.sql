alter table request.cargo_detail drop column cargo_type_id;
alter table request.cargo_detail drop column cargo_type_other;
alter table request.cargo_detail drop column cargo_nomenclature_id;
alter table request.cargo_detail drop column cargo_nomenclature_other;

alter table request.cargo_detail add column cargo_name varchar(255);
alter table request.cargo_detail add column cargo_type varchar(50);
alter table request.cargo_detail add column cargo_category varchar(50);

comment on column request.cargo_detail.cargo_name is 'наименование груза';
comment on column request.cargo_detail.cargo_type is 'тип груза';
comment on column request.cargo_detail.cargo_category is 'категория груза';