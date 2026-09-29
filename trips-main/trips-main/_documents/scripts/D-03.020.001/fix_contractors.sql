update trips.contractors
set autoassign      = dc.autoassign,
    digit_id        = dc.digit_id
from dispatcher.contractor as dc
where trips.contractors.id = dc.id;