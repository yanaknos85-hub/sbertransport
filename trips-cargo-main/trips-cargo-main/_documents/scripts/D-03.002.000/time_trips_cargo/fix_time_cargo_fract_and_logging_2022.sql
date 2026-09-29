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
create type migrations.request as
(
    id uuid,
    "humanReadableId" text,
    author json,
    sender json,
    recipient json,
    "creationTime" json,
    "approvalDate" json,
    "desiredDate" json,
    "timeZone" text,
    "transportType" text,
    "approvedBy" json,
    "tariffId" uuid,
    status text,
    "statusCode" integer,
    "approvalState" text,
    "expected" json,
    "requestOptions" json,
    active boolean,
    "length" double precision,
    width double precision,
    height double precision,
    volume double precision,
    weight double precision,
    "occupiedPlacesCount" integer,
    "cargoData" json,
    "comment" text,
    "finishedTime" json,
    "transferTime" json,
    "shipmentTime" json,
    "timeWorkStart" json,
    "sourceLoaders" integer,
    "destinationLoaders" integer,
    "timeWorkFinish" json
);

create type migrations.corrected_request as
(
    id uuid,
    "humanReadableId" text,
    author json,
    sender json,
    recipient json,
    "creationTime" text,
    "approvalDate" text,
    "desiredDate" text,
    "timeZone" text,
    "transportType" text,
    "approvedBy" json,
    "tariffId" uuid,
    status text,
    "statusCode" integer,
    "approvalState" text,
    "expected" json,
    "requestOptions" json,
    active boolean,
    "length" double precision,
    width double precision,
    height double precision,
    volume double precision,
    weight double precision,
    "occupiedPlacesCount" integer,
    "cargoData" json,
    "comment" text,
    "finishedTime" varchar,
    "transferTime" varchar,
    "shipmentTime" varchar,
    "timeWorkStart" varchar,
    "sourceLoaders" integer,
    "destinationLoaders" integer,
    "timeWorkFinish" varchar
);
do
$$
    declare
        trip trips_cargo.trips%rowtype;
        request migrations.request;
        corrected migrations.request;
        corrected_json json;
        corrected_request text;
        test text;
        year int;
        _month int;
    begin
        year := 2022;
        _month := 1;
        for _month in 1..12 loop
            for trip in select * from trips_cargo.trips where requests::text ilike '%"creationTime":"'||year||'-'||LPAD(_month::text, 2, '0')||'%' loop
                    for request in select * from json_populate_recordset(null::migrations.request, trip.requests) loop
                            select
                                request.id ,
                                request."humanReadableId" ,
                                request.author ,
                                request.sender ,
                                request.recipient ,
                                migrations.date(request."creationTime") ,
                                migrations.date(request."approvalDate"),
                                migrations.date(request."desiredDate") ,
                                request."timeZone" ,
                                request."transportType" ,
                                request."approvedBy" ,
                                request."tariffId" ,
                                request.status ,
                                request."statusCode" ,
                                request."approvalState" ,
                                request."expected" ,
                                request."requestOptions" ,
                                request.active ,
                                request. "length",
                                request.width,
                                request.height,
                                request.volume,
                                request.weight,
                                request."occupiedPlacesCount",
                                request."cargoData",
                                request."comment",
                                migrations.date(request."finishedTime"),
                                migrations.date(request."transferTime"),
                                migrations.date(request."shipmentTime"),
                                migrations.date(request."timeWorkStart"),
                                request."sourceLoaders",
                                request."destinationLoaders",
                                migrations.date(request."timeWorkFinish")
                            into corrected;
                            select row_to_json(corrected) into corrected_json;
                            select concat(corrected_request, ',', corrected_json::text) into corrected_request;
                        end loop;
                    select concat('[', substr(corrected_request, 2), ']') into test;
                    update trips_cargo.trips
                    set requests = test::json
                    where id = trip.id;
                    --raise notice '%', test;
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