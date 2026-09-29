alter table tariff.cargo_tariff_courier
    add prev_value double precision;

alter table tariff.cargo_tariff_region
    add prev_value double precision;

create index cargo_courier_tid_zone_pfr_pto_idx on tariff.cargo_tariff_courier(tariff_id, zone, point_from, point_to);
create index cargo_region_tid_pfr_pto_idx on tariff.cargo_tariff_region(tariff_id, point_from, point_to);

comment on column tariff.cargo_tariff_courier.prev_value is 'значение тарифа из предыдущего интервала в рамках тарифной зоны';
comment on column tariff.cargo_tariff_region.prev_value is 'значение тарифа из предыдущего интервала в рамках пунктов отправления и получения';

update tariff.cargo_tariff_courier ctc
set prev_value = coalesce((select c.value
                           from tariff.cargo_tariff_courier c
                                    join tariff.cargo_intervals ci on ctc.interval_id = ci.id
                                    join tariff.cargo_intervals ci2 on c.interval_id = ci2.id
                           where c.tariff_id = ctc.tariff_id
                             and c.zone = ctc.zone
                             and c.point_from = ctc.point_from
                             and c.point_to = ctc.point_to
                             and ci2.cargo_to = ci.cargo_from
                           limit 1), ctc.value);

update tariff.cargo_tariff_region ctc
set prev_value = coalesce((select c.value
                           from tariff.cargo_tariff_region c
                                    join tariff.cargo_intervals ci on ctc.interval_id = ci.id
                                    join tariff.cargo_intervals ci2 on c.interval_id = ci2.id
                           where c.tariff_id = ctc.tariff_id
                             and c.point_from = ctc.point_from
                             and c.point_to = ctc.point_to
                             and ci2.cargo_to = ci.cargo_from
                           limit 1), ctc.value);

alter table tariff.cargo_tariff_courier alter column prev_value set not null;
alter table tariff.cargo_tariff_region alter column prev_value set not null;