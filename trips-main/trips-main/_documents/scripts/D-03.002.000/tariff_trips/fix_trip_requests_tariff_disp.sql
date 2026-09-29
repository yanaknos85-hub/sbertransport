create type migrations.request as
(
    id uuid,
    "authorId" uuid,
    "humanReadableId" text,
    author json,
    "passengerId" uuid,
    passenger json,
    "organizationId" uuid,
    "taxiClass" text,
    "passengerCount" text,
    expected json,
    "creationTime" varchar,
    "desiredDate" varchar,
    "rideId" uuid,
    suburb boolean,
    "timeZone" varchar,
    "requestOptions" varchar,
    "tariffId" uuid,
    tariff json,
    "contractorId" uuid,
    status text,
    "comment" text,
    waypoints json,
    "driverWaitingTime" varchar,
    "factDistance" double precision,
    "transportType" text,
    "sharedRideOwner" boolean,
    coop boolean
);

do
$$

    declare
        trip trips.trips%rowtype;
        requestForMigrate migrations.request;
        corrected migrations.request;
        corrected_json json;
        corrected_request text;
        test text;
    begin
        for trip in select * from trips.trips join contractors.contractor on contractor.id = trips.contractor_id where (requests -> 0 ->> 'tariff')::text is not null and contractor.integration_type = 'DISPATCHER' loop
                for requestForMigrate in select * from json_populate_recordset(null::migrations.request,trip.requests) loop
                    --                         raise notice '=====================================================';
--                         raise notice '%', trip.id;
                        select requestForMigrate.id,
                               requestForMigrate."authorId",
                               requestForMigrate."humanReadableId",
                               requestForMigrate.author,
                               requestForMigrate."passengerId",
                               requestForMigrate.passenger,
                               requestForMigrate."organizationId",
                               requestForMigrate."taxiClass",
                               requestForMigrate."passengerCount",
                               requestForMigrate.expected,
                               requestForMigrate."creationTime",
                               requestForMigrate."desiredDate",
                               requestForMigrate."rideId",
                               requestForMigrate.suburb,
                               requestForMigrate."timeZone",
                               requestForMigrate."requestOptions",
                               requestForMigrate."tariffId",
                               replace(substr((requestForMigrate.tariff)::text, 2, length((requestForMigrate.tariff)::text) - 2), '\', '')::json,
                               requestForMigrate."contractorId",
                               requestForMigrate.status,
                               requestForMigrate.comment,
                               requestForMigrate.waypoints,
                               requestForMigrate."driverWaitingTime",
                               requestForMigrate."factDistance",
                               requestForMigrate."transportType",
                               requestForMigrate."sharedRideOwner",
                               requestForMigrate.coop
                        into corrected;
                        select row_to_json(corrected) into corrected_json;
                        select concat(corrected_request, ',', corrected_json::text) into corrected_request;
                    end loop;
                select concat('[', substr(corrected_request, 2), ']') into test;
                --                 raise notice '______________________________________';
--                 raise notice '%', test;
                update trips.trips
                set requests = test::json
                where id = trip.id;
                select '' into corrected_request;
            end loop;
    end
$$;

drop type migrations.request;