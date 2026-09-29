-- Плановая стоимость
update trips_cargo.trips set expected_cost = r.cost
from request_cargo.routelist as r
where trips.id = r.id
  and trips.expected_cost is null;

-- Плановая дистанция
update trips_cargo.trips set expected_distance = r.distance
from request_cargo.routelist as r
where trips.id = r.id
  and trips.expected_distance is null;