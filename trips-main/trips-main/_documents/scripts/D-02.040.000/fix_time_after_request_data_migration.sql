create type migrations.request as
(
    id uuid,
    "authorId" uuid,
    "humanReadableId" text,
    author json,
    "passengerId" uuid,
    passenger json,
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
        for trip in select * from trips.trips loop
                for requestForMigrate in select * from json_populate_recordset(null::migrations.request,trip.requests) loop
                        select requestForMigrate.id,
                               requestForMigrate."authorId",
                               requestForMigrate."humanReadableId",
                               requestForMigrate.author,
                               requestForMigrate."passengerId",
                               requestForMigrate.passenger,
                               requestForMigrate."taxiClass",
                               requestForMigrate."passengerCount",
                               requestForMigrate.expected,
                               replace(requestForMigrate."creationTime", ' ', 'T'),
                               replace(requestForMigrate."desiredDate", ' ', 'T'),
                               requestForMigrate."rideId",
                               requestForMigrate.suburb,
                               requestForMigrate."timeZone",
                               requestForMigrate."requestOptions",
                               requestForMigrate."tariffId",
                               requestForMigrate.tariff,
                               requestForMigrate."contractorId",
                               requestForMigrate.status,
                               requestForMigrate.comment,
                               requestForMigrate.waypoints,
                               requestForMigrate."driverWaitingTime",
                               requestForMigrate."factDistance",
                               requestForMigrate."transportType",
                               requestForMigrate.coop
                        into corrected;
                        select row_to_json(corrected) into corrected_json;
                        select concat(corrected_request, ',', corrected_json::text) into corrected_request;
                    end loop;
                select concat('[', substr(corrected_request, 2), ']') into test;
                update trips.trips
                set requests = test::json
                where id = trip.id;
                select '' into corrected_request;
            end loop;
    end
$$;

drop type migrations.request;