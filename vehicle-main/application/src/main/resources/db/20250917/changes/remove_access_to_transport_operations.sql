DO
$do$
    BEGIN
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/telematics/all/' and method = 'POST') AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/telematics/all/' and method = 'POST') and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/type/all/' and method = 'POST')
                AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/type/all/' and method = 'POST')
            and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/subtype/all/' and method = 'POST')
                AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/subtype/all/' and method = 'POST')
            and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/transport/{transportId}/' and method = 'GET')
                AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/transport/{transportId}/' and method = 'GET')
            and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
    END
$do$;