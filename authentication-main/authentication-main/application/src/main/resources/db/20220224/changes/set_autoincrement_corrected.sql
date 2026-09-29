alter table authentication_aud.revinfo
    add column newId int;

update authentication_aud.revinfo
set newId = id
where id is not null;

alter table authentication_aud.revinfo
    drop column id;

alter table authentication_aud.revinfo
    rename column newId to id;

alter table authentication_aud.revinfo
    alter column id set default nextval('authentication_aud.hibernate_sequence');
alter table authentication_aud.revinfo
    alter column id set not null;