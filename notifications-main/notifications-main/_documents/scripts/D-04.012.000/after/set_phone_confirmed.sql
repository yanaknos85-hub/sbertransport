update notifications_corporate.employee ne
set phone_confirmed = ce.phone_confirmed
from corporate.employee ce
where ne.id = ce.id;

update notifications_request.driver de
set phone_confirmed = dd.phone_confirmed
from dispatcher.driver dd
where de.id = dd.id;