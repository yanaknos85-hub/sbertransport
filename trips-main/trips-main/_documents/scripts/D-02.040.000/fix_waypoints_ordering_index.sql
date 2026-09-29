create type migrations.incorrect_waypoint as
(
    id uuid,
    latitude double precision,
    longitude double precision,
    orderingindex integer,
    country text,
    region text,
    city text,
    street text,
    house text,
    building text,
    waitingtime text
);

create type migrations.waypoint as
(
    id uuid,
    latitude double precision,
    longitude double precision,
    "orderingIndex" integer,
    country text,
    region text,
    city text,
    street text,
    house text,
    building text,
    "waitingTime" text
);

do
$$

    declare
        trip trips.trips%rowtype;
        incorrect_waypoint migrations.incorrect_waypoint;
        corrected migrations.waypoint;
        corrected_json json;
        corrected_waypoint text;
        test text;
    begin
        for trip in select * from trips.trips where waypoints::text like '%orderingindex%'loop
                for incorrect_waypoint in select * from json_populate_recordset(null::migrations.incorrect_waypoint,trip.waypoints) loop
                        select incorrect_waypoint.id,
                               incorrect_waypoint.latitude,
                               incorrect_waypoint.longitude,
                               incorrect_waypoint.orderingindex,
                               incorrect_waypoint.country,
                               incorrect_waypoint.region,
                               incorrect_waypoint.city,
                               incorrect_waypoint.street,
                               incorrect_waypoint.house,
                               incorrect_waypoint.building,
                               incorrect_waypoint.waitingtime
                        into corrected;
                        select row_to_json(corrected) into corrected_json;
                        select concat(corrected_waypoint, ',', corrected_json::text) into corrected_waypoint;
                end loop;
                select concat('[', substr(corrected_waypoint, 2), ']') into test;
                update trips.trips
                set waypoints = test::json
                where id = trip.id;
                select '' into corrected_waypoint;
        end loop;
    end
$$;

drop type migrations.waypoint;
drop type migrations.incorrect_waypoint;