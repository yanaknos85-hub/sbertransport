alter table reports.shared_ride
    drop constraint if exists shared_ride_passengers_check;
alter table reports.shared_ride add constraint shared_ride_passengers_check
    check (passengers >= 0);