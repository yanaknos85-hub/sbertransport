insert into trips_cargo.trips (
    select *
    from trips.trips
    where type = 'CARGO'
) on conflict do nothing;

update trips_cargo.trips set status = t.status,
                                        contractor_id = t.contractor_id,
                                        waypoints = t.waypoints,
                                        start_time = t.start_time,
                                        end_time = t.end_time,
                                        digit_id = t.digit_id,
                                        requests = t.requests,
                                        fact_distance = t.fact_distance,
                                        driver_id = t.driver_id,
                                        vehicle_id = t.vehicle_id,
                                        type = t.type,
                                        additional = t.additional from trips.trips as t where trips_cargo.trips.id = t.id;

insert into trips_cargo.contractors (
    select *
    from trips.contractors
) on conflict do nothing;

update trips_cargo.contractors set digit_id = c.digit_id from trips.contractors as c where trips_cargo.contractors.id = c.id;

delete from trips.trips where id in (select id
                                     from trips.trips
                                     where type = 'CARGO');