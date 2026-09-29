delete from telemechanic.roles
where url_id = (
    select u.id
    from telemechanic.urls u
    where u.url = '/ewb/form-title/1/'
        and u.method = 'POST'
);

delete from telemechanic.urls
where url = '/ewb/form-title/1/'
    and method = 'POST';