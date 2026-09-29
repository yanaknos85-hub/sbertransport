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
        for trip in select * from trips_cargo.trips where requests::text ilike '%"timeZone":"Asia/Yekaterinburg"%'loop
                for request in select * from json_populate_recordset(null::migrations.request, trip.requests) loop
                        select
                            request.id ,
                            request."humanReadableId" ,
                            request.author ,
                            request.sender ,
                            request.recipient ,
                            request."creationTime" ,
                            request."approvalDate",
                            request."desiredDate",
                            '+5',
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