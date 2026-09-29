create or replace function migrations.date(source json) returns text language plpgsql
as
$$
declare
    result text;
    stringForArray text;
begin
    if source::text != '[]' then
        select translate(source::text, '"[]"', '') into stringForArray;
        --raise notice '%', stringForArray;
        result = concat('"', (regexp_split_to_array(stringForArray, ','))[1], '-', LPAD((regexp_split_to_array(stringForArray, ','))[2], 2, '0'), '-', LPAD((regexp_split_to_array(stringForArray, ','))[3], 2, '0'), 'T', LPAD(coalesce((regexp_split_to_array(stringForArray, ','))[4], '0'), 2, '0'), ':', LPAD(coalesce((regexp_split_to_array(stringForArray, ','))[5], '0'), 2, '0'), ':', LPAD(coalesce((regexp_split_to_array(stringForArray, ','))[6], '0'), 2, '0'), '.', LPAD(coalesce((regexp_split_to_array(stringForArray, ','))[7], '0'), 3, '0'), '"');
    else
        result = null;
    end if;
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
    "finishedTime" varchar,
    "transferTime" varchar,
    "shipmentTime" varchar,
    "timeWorkStart" varchar,
    "sourceLoaders" integer,
    "destinationLoaders" integer,
    "timeWorkFinish" varchar
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
    begin
        for trip in select * from trips_cargo.trips where requests::text ilike '%creationTime":"[%' loop
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
                            request."finishedTime",
                            request."transferTime",
                            request."shipmentTime",
                            request."timeWorkStart",
                            request."sourceLoaders",
                            request."destinationLoaders",
                            request."timeWorkFinish"
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
    end;
$$;
drop type migrations.request;
drop type migrations.corrected_request;
drop function migrations.date;