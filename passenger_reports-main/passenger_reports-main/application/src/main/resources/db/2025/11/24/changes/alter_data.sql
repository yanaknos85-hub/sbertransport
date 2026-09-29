DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                  WHERE table_schema = 'contractors'
                  and table_name = 'contractor'
            ) THEN
            insert into reports.contractor (id, name)
            select co.id, co.name
            from contractors.contractor co
            on conflict do nothing;
            update reports.contractor c set "name" = (select cc."name" from contractors.contractor cc where cc.id = c.id);
        END IF;
    END
$do$;

DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                  WHERE table_schema = 'reports'
                  and table_name = 'request'
            ) THEN
            update reports.request set request_status_code  = 204 where transport_type = 'TAXI' and "request_status" = 'TAXI_CANCELLED' and request_status_code = 0;
            update reports.request set public_compensation_document_exist = false where public_compensation_document_exist is null;
        END IF;
    END
$do$;

DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                  WHERE table_schema = 'request'
                  and table_name = 'request'
            ) THEN
            update request.request_for_taxi set status_code  = 204
            where transport_type = 'TAXI'
            and "request_status" = 'TAXI_CANCELLED'
            and status_code = 0;
        END IF;
    END
$do$;