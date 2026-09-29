create type migrations.cargo_request as
(
    id uuid,
    "humanReadableId" text,
    author json,
    sender json,
    recipient json,
    "creationTime" varchar,
    "approvalDate" varchar,
    "desiredDate" varchar,
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
        cargo_request migrations.cargo_request;
        corrected migrations.cargo_request;
        corrected_json json;
        corrected_request text;
        test text;
    begin
        for trip in select * from trips_cargo.trips where requests::text like '%phone%' loop
                for cargo_request in select * from json_populate_recordset(null::migrations.cargo_request,trip.requests) loop
                            select cargo_request.id,
                                   cargo_request."humanReadableId",
                                   json_build_object(
                                           'id', cargo_request.author->'id',
                                           'lastName',cargo_request.author->'lastName',
                                           'firstName',cargo_request.author->'firstName',
                                           'patronymic',cargo_request.author->'patronymic',
                                           'mobilePhone',cargo_request.author->'phone',
                                           'email',cargo_request.author->'email',
                                           'organization',cargo_request.author->'organization'),
                                   json_build_object(
                                           'id', cargo_request.sender->'id',
                                           'lastName',cargo_request.sender->'lastName',
                                           'firstName',cargo_request.sender->'firstName',
                                           'patronymic',cargo_request.sender->'patronymic',
                                           'mobilePhone',cargo_request.sender->'phone',
                                           'email',cargo_request.sender->'email',
                                           'organization',cargo_request.sender->'organization'),
                                   json_build_object(
                                           'id', cargo_request.recipient->'id',
                                           'lastName',cargo_request.recipient->'lastName',
                                           'firstName',cargo_request.recipient->'firstName',
                                           'patronymic',cargo_request.recipient->'patronymic',
                                           'mobilePhone',cargo_request.recipient->'phone',
                                           'email',cargo_request.recipient->'email',
                                           'organization',cargo_request.recipient->'organization'),
                                   cargo_request."creationTime",
                                   cargo_request."approvalDate",
                                   cargo_request."desiredDate",
                                   cargo_request."timeZone",
                                   cargo_request."transportType",
                                   json_build_object(
                                           'id', cargo_request."approvedBy"->'id',
                                           'lastName',cargo_request."approvedBy"->'lastName',
                                           'firstName',cargo_request."approvedBy"->'firstName',
                                           'patronymic',cargo_request."approvedBy"->'patronymic',
                                           'mobilePhone',cargo_request."approvedBy"->'phone',
                                           'email',cargo_request."approvedBy"->'email',
                                           'organization',cargo_request."approvedBy"->'organization'),
                                   cargo_request."tariffId",
                                   cargo_request.status,
                                   cargo_request."statusCode",
                                   cargo_request."approvalState",
                                   cargo_request."expected",
                                   cargo_request."requestOptions",
                                   cargo_request.active,
                                   cargo_request."length",
                                   cargo_request.width,
                                   cargo_request.height,
                                   cargo_request.volume,
                                   cargo_request.weight,
                                   cargo_request."occupiedPlacesCount",
                                   cargo_request."cargoData",
                                   cargo_request."comment",
                                   cargo_request."finishedTime",
                                   cargo_request."transferTime",
                                   cargo_request."shipmentTime",
                                   cargo_request."timeWorkStart",
                                   cargo_request."sourceLoaders",
                                   cargo_request."destinationLoaders",
                                   cargo_request."timeWorkFinish"
                            into corrected;
                            select row_to_json(corrected) into corrected_json;
                            select concat(corrected_request, ',', corrected_json::text) into corrected_request;
                    end loop;
                select concat('[', substr(corrected_request, 2), ']') into test;
                update trips_cargo.trips
                set requests = test::json
                where id = trip.id;
                select '' into corrected_request;
            end loop;
    end
$$;

drop type migrations.cargo_request;