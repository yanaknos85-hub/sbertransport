-- Плановая длительность
update trips.trips set expected_time = srmshr.ride_time
from srm.srm_shared_ride as srmshr
where trips.id = srmshr.id
  and trips.expected_time is null
  and trips.requests::text ilike '%"coop":true%'
  and json_array_length(trips.requests) > 1
  and srmshr.ride_time is not null;

update trips.trips set expected_time = replace((((requests->0->'expected')::json -> 'time')::text), '.000000000', '')::bigint
where trips.expected_time is null
  and trips.requests::text ilike '%"coop":true%'
  and json_array_length(trips.requests) = 1
  and (((requests->0->'expected')::json -> 'time')::text) not like '%P%'
  and (((requests->0->'expected')::json ->> 'time')::text) is not null;

update trips.trips set expected_time = replace((((requests->0->'expected')::json -> 'time')::text), '.000000000', '')::bigint
where trips.expected_time is null
  and trips.requests::text ilike '%"coop":false%'
  and (((requests->0->'expected')::json -> 'time')::text) not like '%P%'
  and (((requests->0->'expected')::json ->> 'time')::text) is not null;