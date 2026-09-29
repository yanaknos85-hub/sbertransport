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

create type migrations.incorrect_waypoint as
(
    id uuid,
    latitude double precision,
    longitude double precision,
    orderingindex integer,
    country text,
    region text,
    city text,
    street text,
    house text,
    building text,
    waitingtime text
);

create type migrations.waypoint as
(
    id uuid,
    latitude double precision,
    longitude double precision,
    "orderingIndex" integer,
    country text,
    region text,
    city text,
    street text,
    house text,
    building text,
    "waitingTime" text
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
        incorrect_waypoint migrations.incorrect_waypoint;
        corrected_waypoint migrations.waypoint;
        corrected_waypoint_json json;
        corrected_waypoint_text text;
        test_waypoint text;
    begin
        for trip in select * from trips.trips where requests::text like '%orderingindex%'loop
            --raise notice '%', trip.requests::text;
                for requestForMigrate in select * from json_populate_recordset(null::migrations.request,trip.requests) loop
                    for incorrect_waypoint in select * from json_populate_recordset(null::migrations.incorrect_waypoint, requestForMigrate.waypoints) loop
                        --raise notice '%', row_to_json(incorrect_waypoint);
                        select incorrect_waypoint.id,
                                   incorrect_waypoint.latitude,
                                   incorrect_waypoint.longitude,
                                   incorrect_waypoint.orderingindex,
                                   incorrect_waypoint.country,
                                   incorrect_waypoint.region,
                                   incorrect_waypoint.city,
                                   incorrect_waypoint.street,
                                   incorrect_waypoint.house,
                                   incorrect_waypoint.building,
                                   incorrect_waypoint.waitingtime
                            into corrected_waypoint;
                            --raise notice '%', row_to_json(corrected_waypoint);
                            select row_to_json(corrected_waypoint) into corrected_waypoint_json;
                            select concat(corrected_waypoint_text, ',', corrected_waypoint_json::text) into corrected_waypoint_text;
                        --raise notice '%', corrected_waypoint_text;
                    end loop;
                    select concat('[', substr(corrected_waypoint_text, 2), ']') into test_waypoint;
                    select '' into corrected_waypoint_text;
                    --raise notice '%', test_waypoint;
                    select requestForMigrate.id,
                               requestForMigrate."authorId",
                               requestForMigrate."humanReadableId",
                               requestForMigrate.author,
                               requestForMigrate."passengerId",
                               requestForMigrate.passenger,
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
                               requestForMigrate.tariff,
                               requestForMigrate."contractorId",
                               requestForMigrate.status,
                               requestForMigrate.comment,
                               test_waypoint::json,
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
drop type migrations.waypoint;
drop type migrations.incorrect_waypoint;