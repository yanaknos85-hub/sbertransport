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
    "taxiClass"         text,
    waypoints           json,
    "requestOptions"    text
);
do
$$
    declare
        contractor_trip    contractors.trips%rowtype;
        contractor_request migrations.request;
        bad_trip_id uuid;
    begin
        for contractor_trip in select * from contractors.trips where type = 'PASSENGER' loop
                for contractor_request in select * from json_populate_recordset(null::migrations.request,contractor_trip.requests) loop
                        if contractor_request.expected is null then
                            select contractor_trip.id into bad_trip_id;
                        end if;
                    end loop;
                if bad_trip_id is not null then
                    raise notice 'id - %', bad_trip_id;
                end if;
                select null into bad_trip_id;
            end loop;
    end;
$$;
drop type migrations.request;