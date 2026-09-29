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
        for trip in select * from trips.trips where requests::text like '%"phone"%' loop
                for requestForMigrate in select * from json_populate_recordset(null::migrations.request,trip.requests) loop
                        select requestForMigrate.id,
                               requestForMigrate."authorId",
                               requestForMigrate."humanReadableId",
                               json_build_object(
                                       'id', requestForMigrate.author->'id',
                                       'lastName',requestForMigrate.author->'lastName',
                                       'firstName',requestForMigrate.author->'firstName',
                                       'patronymic',requestForMigrate.author->'patronymic',
                                       'mobilePhone',requestForMigrate.author->'phone',
                                       'email',requestForMigrate.author->'email',
                                       'organization',requestForMigrate.author->'organization'),
                               requestForMigrate."passengerId",
                               json_build_object(
                                       'id', requestForMigrate.passenger->'id',
                                       'lastName',requestForMigrate.passenger->'lastName',
                                       'firstName',requestForMigrate.passenger->'firstName',
                                       'patronymic',requestForMigrate.passenger->'patronymic',
                                       'mobilePhone',requestForMigrate.passenger->'phone',
                                       'email',requestForMigrate.passenger->'email',
                                       'organization',requestForMigrate.passenger->'organization'),
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
                --raise notice '%', test;
                select '' into corrected_request;
            end loop;
    end
$$;

drop type migrations.request;