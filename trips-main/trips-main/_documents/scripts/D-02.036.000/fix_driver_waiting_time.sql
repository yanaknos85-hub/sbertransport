-- Необходимо применить если сервис не поднимается из-за того, что driverWaitingTime в столбце additional не целочисленный
do
$$

    declare
        trip trips.trips%rowtype;
        waitingTimeText text;
        waitingTimeInt integer;
    begin
        for trip in select * from trips.trips loop
                select trip.additional::json->'driverWaitingTime' into waitingTimeText;
                if waitingTimeText is not null and waitingTimeText ilike '%.%' then
                    raise notice '%', waitingTimeText;
                    waitingTimeInt := round(waitingTimeText::double precision);
                    raise notice '%', waitingTimeInt;
                    trip.additional := json_build_object('passengerCount', trip.additional::json->'passengerCount',
                                                         'taxiClass', trip.additional::json->'taxiClass',
                                                         'driverWaitingTime', waitingTimeInt);
                    raise notice '%', trip.additional;
                    update trips.trips set additional = trip.additional where id = trip.id;
                end if;
            end loop;
    end
$$;