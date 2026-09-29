alter table request.cargo_detail drop column cargo_name;
alter table request.cargo_detail drop column cargo_type;
alter table request.cargo_detail drop column cargo_category;

alter table request.cargo_detail add column cargo_type_id uuid;
alter table request.cargo_detail add column cargo_type_other varchar(50);
alter table request.cargo_detail add column cargo_nomenclature_id uuid;
alter table request.cargo_detail add column cargo_nomenclature_other varchar(50);

comment on column request.cargo_detail.cargo_type_id is 'идентификатор типа груза';
comment on column request.cargo_detail.cargo_type_other is 'тип груза - другое';
comment on column request.cargo_detail.cargo_nomenclature_id is 'идентификатор номенклатуры груза';
comment on column request.cargo_detail.cargo_nomenclature_other is 'номенклатура груза - другое';