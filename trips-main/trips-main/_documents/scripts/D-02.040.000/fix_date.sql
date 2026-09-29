create or replace function migrations.date(source json) returns text language plpgsql
as
$$
declare
    result text;
    stringForArray text;
begin
    if source::text != '[]' then
        select translate(source::text, '"[]"', '') into stringForArray;
        raise notice '%', stringForArray;
        result = concat('"', (regexp_split_to_array(stringForArray, ','))[1], '-', LPAD((regexp_split_to_array(stringForArray, ','))[2], 2, '0'), '-', LPAD((regexp_split_to_array(stringForArray, ','))[3], 2, '0'), 'T', LPAD(coalesce((regexp_split_to_array(stringForArray, ','))[4], '0'), 2, '0'), ':', LPAD(coalesce((regexp_split_to_array(stringForArray, ','))[5], '0'), 2, '0'), ':', LPAD(coalesce((regexp_split_to_array(stringForArray, ','))[6], '0'), 2, '0'), '.', LPAD(coalesce((regexp_split_to_array(stringForArray, ','))[7], '0'), 3, '0'), '"');
    else
        result = null;
    end if;
    return result;
end
$$;
create type migrations.request as ("humanReadableId" text, id uuid, "contractorId" uuid, "authorId" uuid, resolution text, "passengerId" uuid, "creationTime" json, "timeZone" text, "finishedTime" json, "transportType" text, "tripClass" text, "tariffId" uuid, coop boolean, suburb boolean, "desiredDate" json, status text, "passengerCount" int, "expectedCost" int8, "expectedTime" json, "expectedDistance" double precision, "commentForDriver" text, "rideId" uuid, "deadLine" text, "driverId" uuid, "vehicleId" uuid, author json, passenger json, "driverWaitingTime" text, "factDistance" double precision);
create type migrations.corrected_request as ("humanReadableId" text, id uuid, "contractorId" uuid, "authorId" uuid, resolution text, "passengerId" uuid, "creationTime" text, "timeZone" text, "finishedTime" text, "transportType" text, "tripClass" text, "tariffId" uuid, coop boolean, suburb boolean, "desiredDate" text, status text, "passengerCount" int, "expectedCost" int8, "expectedTime" text, "expectedDistance" double precision, "commentForDriver" text, "rideId" uuid, "deadLine" text, "driverId" uuid, "vehicleId" uuid, author json, passenger json, "driverWaitingTime" text, "factDistance" double precision);
do
$$
    declare
        trip trips.trips%rowtype;
        request migrations.request;
        corrected migrations.request;
        corrected_json json;
        corrected_request text;
        test text;
    begin
        for trip in select * from trips.trips where requests::text ilike '%creationTime":"[%' loop
                for request in select * from json_populate_recordset(null::migrations.request, trip.requests) loop
                        select request."humanReadableId", request.id, request."contractorId", request."authorId", request.resolution, request."passengerId", migrations.date(request."creationTime"), request."timeZone", migrations.date(request."finishedTime"), request."transportType", request."tripClass", request."tariffId", request.coop, request.suburb, migrations.date(request."desiredDate"), request.status, request."passengerCount", request."expectedCost", migrations.date(request."expectedTime"), request."expectedDistance", request."commentForDriver", request."rideId", request."deadLine", request."driverId", request."vehicleId", request.author, request.passenger, request."driverWaitingTime", request."factDistance" into corrected;
                        select row_to_json(corrected) into corrected_json;
                        select concat(corrected_request, ',', corrected_json::text) into corrected_request;
                    end loop;
                select concat('[', substr(corrected_request, 2), ']') into test;
                update trips.trips
                set requests = test::json
                where id = trip.id;
                select '' into corrected_request;
            end loop;
    end;
$$;
drop type migrations.request;
drop type migrations.corrected_request;
drop function migrations.date;