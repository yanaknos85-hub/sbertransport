create table tariff.cargo_tariff_courier_tmp as
select ctc.*, cz.point_from, cz.point_to
from tariff.cargo_cities_zone cz,
     tariff.cargo_tariff_courier ctc
where cz.tariff_id = ctc.tariff_id
  and cz.zone = ctc.zone;

truncate table tariff.cargo_tariff_courier;

alter table tariff.cargo_tariff_courier
    add column if not exists point_from uuid not null
        constraint cargo_cities_id_fkey1 references tariff.cargo_cities;

alter table tariff.cargo_tariff_courier
    add column if not exists point_to uuid not null
        constraint cargo_cities_id_fkey2 references tariff.cargo_cities;

comment on column tariff.cargo_tariff_courier.point_from is 'Город отправления груза';
comment on column tariff.cargo_tariff_courier.point_to is 'Город доставки груза';

insert into tariff.cargo_tariff_courier
select * from tariff.cargo_tariff_courier_tmp;

drop table tariff.cargo_tariff_courier_tmp;