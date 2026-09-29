create type migrations.request as
(
    "humanReadableId"   text,
    id                  uuid,
    "contractorId"      uuid,
    "authorId"          uuid,
    "passengerId"       uuid,
    "creationTime"      text,
    "timeZone"          text,
    "tariffId"          uuid,
    coop                boolean,
    suburb              boolean,
    "desiredDate"       text,
    status              text,
    "passengerCount"    int,
    "comment"  text,
    "rideId"            uuid,
    author              json,
    passenger           json,
    "driverWaitingTime" text,
    "factDistance"      double precision,
    expected            json,
    rideId              uuid,
    "taxiClass"         text,
    waypoints           json,
    "requestOptions"    text
);
do
$$
    declare
        contractor_trip    contractors.trips;
        contractor_request migrations.request;
        corrected          migrations.request;
        corrected_json     json;
        corrected_request  text;
        test text;
    begin
        for contractor_trip in select * from contractors.trips where type = 'PASSENGER'
            loop
                for contractor_request in select * from json_populate_recordset(null::migrations.request,contractor_trip.requests) loop
                        select contractor_request."humanReadableId", contractor_request.id, contractor_request."contractorId", contractor_request."authorId", contractor_request."passengerId", contractor_request."creationTime", contractor_request."timeZone", contractor_request."tariffId", contractor_request.coop, contractor_request.suburb, contractor_request."desiredDate", contractor_request.status, contractor_request."passengerCount", contractor_request."comment", contractor_request."rideId", contractor_request.author, contractor_request.passenger, null, contractor_request."factDistance", contractor_request.expected, contractor_request."rideId", contractor_request."taxiClass", contractor_request.waypoints, contractor_request."requestOptions" into corrected;
                        select row_to_json(corrected) into corrected_json;
                        select concat(corrected_request, ',', corrected_json::text) into corrected_request;
                    end loop;
                select concat('[', substr(corrected_request, 2), ']') into test;
                update contractors.trips
                set requests = test::json
                where id = contractor_trip.id;
                select '' into corrected_request;
            end loop;
    end;
$$;
drop type migrations.request;
