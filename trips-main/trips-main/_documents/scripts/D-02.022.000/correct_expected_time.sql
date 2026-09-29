create type migrations.request as ("humanReadableId" text, id uuid, "contractorId" uuid, "authorId" uuid, resolution text, "passengerId" uuid, "creationTime" text, "timeZone" text, "finishedTime" text, "transportType" text, "tripClass" text, "tariffId" uuid, coop boolean, suburb boolean, "desiredDate" text, status text, "passengerCount" int, "expectedCost" int8, "expectedTime" text, "expectedDistance" double precision, "commentForDriver" text, "rideId" uuid, "deadLine" text, "driverId" uuid, "vehicleId" uuid, author json, passenger json, "driverWaitingTime" text, "factDistance" double precision);
do
$$
    declare
        contractor_trip contractors.trips%rowtype;
        trip_trip trips.trips%rowtype;
        contractor_request migrations.request;
        trip_request migrations.request;
        corrected migrations.request;
        corrected_json json;
        corrected_request text;
        test text;
    begin
        for contractor_trip in select * from contractors.trips where requests::text ilike '%--T%' loop
                select * into trip_trip from trips.trips where id = contractor_trip.id;
                for contractor_request in select * from json_populate_recordset(null::migrations.request, contractor_trip.requests) loop
                    for trip_request in select * from json_populate_recordset(null::migrations.request, trip_trip.requests) loop
                        if trip_request.id = contractor_request.id then
                         select contractor_request."humanReadableId", contractor_request.id, contractor_request."contractorId", contractor_request."authorId", contractor_request.resolution, contractor_request."passengerId", contractor_request."creationTime", contractor_request."timeZone", contractor_request."finishedTime", contractor_request."transportType", contractor_request."tripClass", contractor_request."tariffId", contractor_request.coop, contractor_request.suburb, contractor_request."desiredDate", contractor_request.status, contractor_request."passengerCount", contractor_request."expectedCost", trip_request."expectedTime", contractor_request."expectedDistance", contractor_request."commentForDriver", contractor_request."rideId", contractor_request."deadLine", contractor_request."driverId", contractor_request."vehicleId", contractor_request.author, contractor_request.passenger, contractor_request."driverWaitingTime", contractor_request."factDistance" into corrected;
                         select row_to_json(corrected) into corrected_json;
                         select concat(corrected_request, ',', corrected_json::text) into corrected_request;
                        end if;
                    end loop;
                end loop;
                update contractors.trips
                set requests = test::json
                where id = contractor_trip.id;
                select '' into corrected_request;
            end loop;
    end;
$$;
drop type migrations.request;