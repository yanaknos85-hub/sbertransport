delete from telemechanic.roles
where url_id in (
    select u.id
    from telemechanic.urls u
    where u.url = '/transportation-types/'
       or u.url = '/transportation-types/{typeId}/transportation-subtypes/'
);

delete from telemechanic.urls
where url = '/transportation-types/'
   or url = '/transportation-types/{typeId}/transportation-subtypes/';
