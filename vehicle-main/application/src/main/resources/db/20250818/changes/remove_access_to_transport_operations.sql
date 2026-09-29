DO
$do$
    BEGIN
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/transport/' and method = 'POST') AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/transport/' and method = 'POST') and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/transport/{transportId}/' and method = 'PATCH')
                AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/transport/{transportId}/' and method = 'PATCH')
            and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/transport/search/structure/' and method = 'POST')
                AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/transport/search/structure/' and method = 'POST')
            and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/transport/statenumber/' and method = 'POST')
                AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/transport/statenumber/' and method = 'POST')
            and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
    END
$do$;