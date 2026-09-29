alter table tariff.tariff
    add column service_type varchar(255) not null default 'EMPLOYEE_TRANSPORTATION';
comment on column tariff.tariff.service_type is 'Вид транспортной услуги';