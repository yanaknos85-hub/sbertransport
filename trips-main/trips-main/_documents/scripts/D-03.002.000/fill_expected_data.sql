-- Плановая стоимость
update trips.trips set expected_cost = srmshr.ride_cost
from srm.srm_shared_ride as srmshr
where trips.id = srmshr.id
  and trips.expected_cost is null
  and trips.requests::text ilike '%"coop":true%'
  and json_array_length(trips.requests) > 1;

update trips.trips set expected_cost = replace(((requests->0->'expected')::json -> 'cost')::text, '.0', '')::bigint
where trips.expected_cost is null
  and trips.requests::text ilike '%"coop":true%'
  and json_array_length(trips.requests) = 1
  and (((requests->0->'expected')::json -> 'cost')::text) not like '%E%';

update trips.trips set expected_cost = replace(((requests->0->'expected')::json -> 'cost')::text, '.0', '')::bigint
where trips.expected_cost is null
  and trips.requests::text ilike '%"coop":false%'
  and (((requests->0->'expected')::json -> 'cost')::text) not like '%E%';

-- Плановая дистанция
update trips.trips set expected_distance = srmshr.ride_distance
from srm.srm_shared_ride as srmshr
where trips.id = srmshr.id
  and trips.expected_distance is null
  and trips.requests::text ilike '%"coop":true%'
  and json_array_length(trips.requests) > 1;

update trips.trips set expected_distance = (((requests->0->'expected')::json -> 'distance') :: text) :: double precision
where trips.expected_distance is null
  and trips.requests::text ilike '%"coop":true%'
  and json_array_length(trips.requests) = 1;

update trips.trips set expected_distance = (((requests->0->'expected')::json -> 'distance') :: text) :: double precision
where trips.expected_distance is null
  and trips.requests::text ilike '%"coop":false%';