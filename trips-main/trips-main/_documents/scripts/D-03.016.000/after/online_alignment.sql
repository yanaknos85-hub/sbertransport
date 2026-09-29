update trips.driver
set online = false
where shift_id is null;

update trips_cargo.driver
set online = false
where shift_id is null;

update dispatcher.driver
set online = td.online
from trips.driver as td
where dispatcher.driver.id = td.id
  and (driver_speciality = 'PASSENGER'
    or driver_speciality = 'BOTH');

update dispatcher.driver
set online = tcd.online
from trips_cargo.driver as tcd
where dispatcher.driver.id = tcd.id
  and (driver_speciality = 'CARGO')