delete from telemechanic.roles
where url_id = (select id from telemechanic.urls where url = '/ewb/form-title/2/');

delete from telemechanic.urls where url = '/ewb/form-title/2/';