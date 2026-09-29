
create type migrations.request as ("humanReadableId" text, id uuid, "contractorId" uuid, "authorId" uuid, resolution text, "passengerId" uuid, "creationTime" text, "timeZone" text, "finishedTime" text, "transportType" text, "tripClass" text, "tariffId" uuid, "coopTrip" boolean, suburb boolean, "desiredDate" text, status text, "passengerCount" int, "expectedCost" int8, "expectedTime" text, "expectedDistance" double precision, "commentForDriver" text, "rideId" uuid, "deadLine" text, "driverId" uuid, "vehicleId" uuid, author json, passenger json, "driverWaitingTime" text, "factDistance" double precision);
create type migrations.corrected_request as ("humanReadableId" text, id uuid, "contractorId" uuid, "authorId" uuid, resolution text, "passengerId" uuid, "creationTime" text, "timeZone" text, "finishedTime" text, "transportType" text, "tripClass" text, "tariffId" uuid, coop boolean, suburb boolean, "desiredDate" text, status text, "passengerCount" int, "expectedCost" int8, "expectedTime" text, "expectedDistance" double precision, "commentForDriver" text, "rideId" uuid, "deadLine" text, "driverId" uuid, "vehicleId" uuid, author json, passenger json, "driverWaitingTime" text, "factDistance" double precision);
do
$$
    declare
        trip_trip trips.trips%rowtype;
        trip_request migrations.request;
        corrected migrations.corrected_request;
        corrected_json json;
        corrected_request text;
        test text;
    begin
        for trip_trip in select * from trips.trips where requests::text ilike '%coopTrip%' loop
                for trip_request in select * from json_populate_recordset(null::migrations.request, trip_trip.requests) loop
                        select trip_request."humanReadableId", trip_request.id, trip_request."contractorId", trip_request."authorId", trip_request.resolution, trip_request."passengerId", trip_request."creationTime", trip_request."timeZone", trip_request."finishedTime", trip_request."transportType", trip_request."tripClass", trip_request."tariffId", trip_request."coopTrip", trip_request.suburb, trip_request."desiredDate", trip_request.status, trip_request."passengerCount", trip_request."expectedCost", trip_request."expectedTime", trip_request."expectedDistance", trip_request."commentForDriver", trip_request."rideId", trip_request."deadLine", trip_request."driverId", trip_request."vehicleId", trip_request.author, trip_request.passenger, trip_request."driverWaitingTime", trip_request."factDistance" into corrected;
                        select row_to_json(corrected) into corrected_json;
                        select concat(corrected_request, ',', corrected_json::text) into corrected_request;
                    end loop;
                select concat('[', substr(corrected_request, 2), ']') into test;
                raise notice '%', test;
                update trips.trips
                set requests = test::json
                where id = trip_trip.id;
                select '' into corrected_request;
            end loop;
    end;
$$;
drop type migrations.request;
drop type migrations.corrected_request;

delete from contractors.trip_history where trip_history.trip_id in (select id from contractors.trips where requests::text = '[]' or requests is null);
delete from contractors.trips where requests::text = '[]' or requests is null;