delete
from telemechanic.roles r
where r.url_id in (select u.id from telemechanic.urls u where u.url = '/telemedicine/search/' and u.method = 'POST')
  and r.role not in ('ROLE_MEDIC', 'ROLE_DISPATCHER_SUPPORT_SERVICE');

delete
from telemechanic.roles r
where r.url_id in (select u.id from telemechanic.urls u where u.url = '/medical-license/' and u.method = 'GET')
  and r.role not in ('ROLE_MEDIC', 'ROLE_DISPATCHER_SUPPORT_SERVICE');

delete
from telemechanic.roles r
where r.url_id in (select u.id from telemechanic.urls u where u.url = '/monitoring/' and u.method = 'POST')
  and r.role not in ('ROLE_TELEMECHANIC', 'ROLE_TELEMECHANIC_ORGANIZATION', 'ROLE_DISPATCHER_SUPPORT_SERVICE');