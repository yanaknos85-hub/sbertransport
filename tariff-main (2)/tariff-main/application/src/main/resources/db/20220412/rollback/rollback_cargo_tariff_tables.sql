alter table tariff.cargo_tariff_courier drop column if exists prev_value;
alter table tariff.cargo_tariff_region drop column if exists prev_value;

drop index if exists tariff.cargo_courier_tid_zone_pfr_pto_idx;
drop index if exists tariff.cargo_region_tid_pfr_pto_idx;

