DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'corporate'
                  and table_name = 'contact'
            ) THEN
            insert into telemechanic.contact (id, type, value)
            select co.id, co.type, co.value
            from corporate.contact co
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
                  and table_name = 'organization_contacts'
            ) THEN
            insert into telemechanic.organization_contact (contact_id, organization_id)
            select co.contact_id, co.organization_id
            from corporate.organization_contacts co
            on conflict do nothing;
        END IF;
    END
$do$;