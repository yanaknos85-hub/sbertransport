do
$$
    declare
        trip request.taxi_trip%rowtype;
    BEGIN
        for trip in select * from request.taxi_trip loop
                if not exists(select id from request.request_for_taxi where taxi_trip_id = trip.id) then
                    delete from request.taxi_trip where id = trip.id;
                end if;
            end loop;
    end;
$$