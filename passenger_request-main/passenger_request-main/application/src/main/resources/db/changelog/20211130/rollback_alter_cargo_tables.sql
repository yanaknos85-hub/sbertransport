alter table request.request_for_cargo
    add column cargo_type_id uuid,
    add column cargo_type_other varchar(50),
    add column cargo_nomenclature_id uuid,
    add column cargo_nomenclature_other varchar(50),
    add column fragile boolean,
    add column need_package boolean,
    add column package_id uuid,
    add column package_count integer;

alter table request.cargo_detail
    drop column position,
    drop column cargo_type_id,
    drop column cargo_type_other,
    drop column cargo_nomenclature_id,
    drop column cargo_nomenclature_other,
    drop column occupied_places_count,
    drop column fragile,
    drop column need_package,
    drop column package_id,
    drop column package_count;