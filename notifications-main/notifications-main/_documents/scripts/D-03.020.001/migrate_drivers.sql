insert into notifications_request.driver
(id, contractor_id, email, first_name, last_name, patronymic, phone, rating, autopark_id)
select id,
       contractor_id,
       email,
       first_name,
       last_name,
       patronymic,
       replace(replace(contact_phone_number, '(', ''), ')', ''),
       rating,
       null
from dispatcher.driver
on conflict do nothing;

update notifications_request.driver
set contractor_id               = dd.contractor_id,
    email                       = dd.email,
    first_name                  = dd.first_name,
    last_name                   = dd.last_name,
    patronymic                  = dd.patronymic,
    phone                       = replace(replace(dd.contact_phone_number, '(', ''), ')', ''),
    rating                      = dd.rating,
    autopark_id                 = null
from dispatcher.driver as dd
where notifications_request.driver.id = dd.id;