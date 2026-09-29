DO
$do$
    declare
        vehicleId            telemechanic.transport.id%TYPE;
        rec_transport_update record;
        rec_transport_save   record;
    BEGIN
        alter table telemechanic.request
            drop constraint request_transport_id_fk;
        for rec_transport_update in (select * from vehicle.transport where state_number in (select state_number from telemechanic.transport))
            loop
                select id from telemechanic.transport where state_number = rec_transport_update.state_number into vehicleId;
                update telemechanic.transport v
                set id     = rec_transport_update.id,
                    brand  = rec_transport_update.brand_by_passport,
                    model  = rec_transport_update.model_by_passport,
                    status = rec_transport_update.status
                where v.state_number = rec_transport_update.state_number;
                update telemechanic.request r set transport_id = rec_transport_update.id where r.transport_id = vehicleId;
            end loop;
        for rec_transport_save in (select * from vehicle.transport where state_number not in (select state_number from telemechanic.transport))
            loop
                insert into telemechanic.transport (id, state_number, brand, model, status)
                values (rec_transport_save.id,
                        rec_transport_save.state_number,
                        rec_transport_save.brand_by_passport,
                        rec_transport_save.model_by_passport,
                        rec_transport_save.status);
            end loop;
        alter table telemechanic.request
            add constraint request_transport_id_fk
                foreign key (transport_id) references telemechanic.transport;
    END
$do$;