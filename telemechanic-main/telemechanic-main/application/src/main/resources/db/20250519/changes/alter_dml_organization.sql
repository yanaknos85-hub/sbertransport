DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'corporate'
                  and table_name = 'organization_group'
            ) THEN
            insert into telemechanic.organization_group (id, name, internal)
            select go.id, go.name, go.internal
            from corporate.organization_group go
            on conflict do nothing;
        END IF;
    END
$do$;

DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'corporate'
                  and table_name = 'organization'
            ) THEN
            insert into telemechanic.organization (id, digit_id, active, official_name)
            select co.id, co.digit_id, CASE WHEN co.status = 'ACTIVE' THEN true ELSE false END, co.official_name
            from corporate.organization co
            on conflict do nothing;
        END IF;
    END
$do$;

update telemechanic.organization set active = true where id in (select id from corporate.organization where status = 'ACTIVE');
update telemechanic.organization set active = false where id in (select id from corporate.organization where status = 'INACTIVE');

update telemechanic.organization o set msrn = (select co.msrn from corporate.organization co where co.id = o.id);
update telemechanic.organization o set tin = (select co.tin from corporate.organization co where co.id = o.id);
update telemechanic.organization o set organization_group_id = (select co.organization_group_id from corporate.organization co where co.id = o.id);