update dispatcher.driver dd set autopark_id = null
from dispatcher.contractor dc where dd.contractor_id = dc.id
and dc.is_internal = true;

update trips.driver td set autopark_id = null
from dispatcher.driver dd where td.id = dd.id
and dd.autopark_id is null;

update trips_cargo.driver tcd set autopark_id = null
from dispatcher.driver dd where tcd.id = dd.id
and dd.autopark_id is null;