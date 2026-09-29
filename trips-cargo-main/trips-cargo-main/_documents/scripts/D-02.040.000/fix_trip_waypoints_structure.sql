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
        trip trips_cargo.trips%rowtype;
        waypoint migrations.waypoint;
        corrected migrations.waypoint;
        corrected_json json;
        corrected_waypoint text;
        test text;
    begin
        for trip in select * from trips_cargo.trips where waypoints::text ilike '%"structure"%' loop
                for waypoint in select * from json_populate_recordset(null::migrations.waypoint,trip.waypoints) loop
                        select waypoint.id, waypoint.latitude, waypoint.longitude, waypoint."orderingIndex", waypoint.country, waypoint.region, waypoint.city, waypoint.street, waypoint.house, waypoint.building, waypoint."waitingTime" into corrected;
                        select row_to_json(corrected) into corrected_json;
                        select concat(corrected_waypoint, ',', corrected_json::text) into corrected_waypoint;
                    end loop;
                select concat('[', substr(corrected_waypoint, 2), ']') into test;
                update trips_cargo.trips
                set waypoints = test::json
                where id = trip.id;
                select '' into corrected_waypoint;
            end loop;
    end
$$;

drop type migrations.waypoint;