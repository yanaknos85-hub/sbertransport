alter table vehicle.transport
add column location_address varchar(255) default '-' not null;

alter table vehicle.transport
add column parking_address varchar(255) default '-' not null;

alter table vehicle.transport
add column comment varchar(255);

comment on column vehicle.transport.location_address is 'Адрес места базирования';
comment on column vehicle.transport.parking_address is 'Адрес стоянки автомобиля';
comment on column vehicle.transport.comment is 'Комментарий';

alter table vehicle.transport
    alter column location_address drop default;

alter table vehicle.transport
    alter column parking_address drop default;
