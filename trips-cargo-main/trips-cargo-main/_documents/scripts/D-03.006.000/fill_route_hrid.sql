update trips_cargo.trips
set route_human_readable_id = r.humanreadableid
from request_cargo.routelist as r
where route_human_readable_id is null
  and trips.id = r.id