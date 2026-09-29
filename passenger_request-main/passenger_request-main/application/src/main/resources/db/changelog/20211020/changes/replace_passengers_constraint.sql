alter table request.shared_ride
    drop constraint if exists shared_ride_passengers_check;
alter table request.shared_ride add constraint shared_ride_passengers_check
            check (passengers >= 0);