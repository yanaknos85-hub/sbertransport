alter table telemechanic.vehicle
    drop constraint state_number_vehicle;

alter table if exists telemechanic.vehicle
    rename to transport;

alter table telemechanic.request
    rename column vehicle_id to transport_id;

alter index telemechanic.request_vehicle_id_index rename to request_transport_id_index;

alter table telemechanic.request
    rename constraint request_vehicle_id_fk to request_transport_id_fk;

alter table telemechanic.transport
    add status varchar(20);

comment on column telemechanic.transport.status is 'Статус';

update telemechanic.transport set status = 'IN_USE';

alter table telemechanic.transport alter column status set not null;

