update trips.contractors
set integration_type = c.integration_type
from contractors.contractor as c
where trips.contractors.id = c.id;

delete from trips.trip_history
where trip_id in (select id
                  from trips.trips
                  where contractor_id in
                        (select id from trips.contractors where contractors.integration_type <> 'DISPATCHER'));

delete
from trips.trips
where contractor_id in
      (select id from trips.contractors where integration_type <> 'DISPATCHER')