insert into trips.autopark (id, contractor_id, routing_id, active) select id, contractor_id, routing_id, active from dispatcher.autopark
on conflict do nothing;

update trips.dispatcher set autopark_id  = dd.autopark_id
from dispatcher.dispatcher as dd where trips.dispatcher.id = dd.id;

update trips.driver set autopark_id  = ddr.autopark_id
from dispatcher.driver as ddr where trips.driver.id = ddr.id;

update trips.vehicle set autopark_id  = v.autopark_id
from dispatcher.vehicle as v where trips.vehicle.id = v.id;