insert into notifications_contractor.contractor (id, name)
select id, name
from dispatcher.contractor
ON CONFLICT(id) do update set name = excluded.name;

insert into notifications_contractor.dispatcher (id, phone, phone_confirmed, email, contractor_id)
select id, replace(phone, ' ', '') as phone, phone_confirmed, email, contractor_id
from dispatcher.dispatcher
ON CONFLICT(id) do update set phone           = excluded.phone,
                              phone_confirmed = excluded.phone_confirmed,
                              email           = excluded.email,
                              contractor_id   = excluded.contractor_id;

insert into notifications.contacts (id, email, phone, phone_confirmed)
select id, email, phone, phone_confirmed
from notifications_contractor.dispatcher
union all
select id, email, phone, phone_confirmed
from notifications_corporate.employee
union all
select id, email, phone, phone_confirmed
from notifications_request.driver
on conflict (id) do nothing;

update notifications_corporate.employee t
set email = s.email,
    phone = s.mobile_phone,
    phone_confirmed = s.phone_confirmed
from corporate.employee s
where t.id = s.id;

update notifications.contacts t
set email = s.email,
    phone = s.mobile_phone,
    phone_confirmed = s.phone_confirmed
from corporate.employee s
where t.id = s.id;