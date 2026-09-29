insert into trips_cargo.autopark (id, contractor_id, routing_id, active) select id, contractor_id, routing_id, active
from dispatcher.autopark
on conflict do nothing;

update trips_cargo.dispatcher set autopark_id  = dd.autopark_id
from dispatcher.dispatcher as dd where trips_cargo.dispatcher.id = dd.id;

update trips_cargo.driver set autopark_id  = ddr.autopark_id
from dispatcher.driver as ddr where trips_cargo.driver.id = ddr.id;

update trips_cargo.vehicle set autopark_id  = v.autopark_id
from dispatcher.vehicle as v where trips_cargo.vehicle.id = v.id;