create or replace function migrations.date(source json) returns text language plpgsql
as
$$
declare
    result text;
    string text;
begin
    if source::text not like '%+%' and source::text not like '%Z%' then
        select translate(source::text, '""', '') into string;
        result = concat('"',string, '+00:00','"');
    else
        result = source::text;
    end if;
    --raise notice '%', result;
    return result;
end
$$;
create type migrations.request as (
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
                                      "creationTime" json,
                                      "desiredDate" json,
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
                                      coop boolean);
create type migrations.corrected_request as (
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
                                                coop boolean);
do
$$
    declare
        _month int;
        year int;
        trip trips.trips%rowtype;
        request migrations.request;
        corrected migrations.request;
        corrected_json json;
        corrected_request text;
        test text;
    begin
        year := 2024;
        _month := 1;
            for _month in 1..12 loop
                for trip in select * from trips.trips join contractors.contractor on contractor.id = trips.contractor_id where requests::text ilike '%"creationTime":"'||year||'-'||LPAD(_month::text, 2, '0')||'%' and contractor.integration_type = 'DISPATCHER' loop
                        for request in select * from json_populate_recordset(null::migrations.request, trip.requests) loop
                                select request.id,
                                       request."authorId",
                                       request."humanReadableId",
                                       request.author,
                                       request."passengerId",
                                       request.passenger,
                                       request."organizationId",
                                       request."taxiClass",
                                       request."passengerCount",
                                       request.expected,
                                       migrations.date(request."creationTime"),
                                       migrations.date(request."desiredDate"),
                                       request."rideId",
                                       request.suburb,
                                       request."timeZone",
                                       request."requestOptions",
                                       request."tariffId",
                                       request.tariff,
                                       request."contractorId",
                                       request.status,
                                       request.comment,
                                       request.waypoints,
                                       request."driverWaitingTime",
                                       request."factDistance",
                                       request."transportType",
                                       request."sharedRideOwner",
                                       request.coop into corrected;
                                select row_to_json(corrected) into corrected_json;
                                select concat(corrected_request, ',', corrected_json::text) into corrected_request;
                            end loop;
                        select concat('[', substr(corrected_request, 2), ']') into test;
                        update trips.trips
                        set requests = test::json
                        where id = trip.id;
            --                 raise notice '%', test;
                        select '' into corrected_request;
                    end loop;
                raise notice 'Год %, месяц % - готово (%)', year, lpad(_month::text, 2, '0'), now();
                _month := _month + 1;
            end loop;
    end;
$$;
drop type migrations.request;
drop type migrations.corrected_request;
drop function migrations.date;