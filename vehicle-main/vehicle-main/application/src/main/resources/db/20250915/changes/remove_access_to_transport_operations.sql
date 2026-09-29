DO
$do$
    BEGIN
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/vehicle/search/' and method = 'POST') AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/vehicle/search/' and method = 'POST') and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/brand/all/' and method = 'POST')
                AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/brand/all/' and method = 'POST')
            and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/model/all/' and method = 'POST')
                AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/model/all/' and method = 'POST')
            and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/vehicle/all/' and method = 'POST')
                AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/vehicle/all/' and method = 'POST')
            and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/vehicle/' and method = 'POST')
                AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/vehicle/' and method = 'POST')
            and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/vehicle/{vehicleId}/' and method = 'GET')
                AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/vehicle/{vehicleId}/' and method = 'GET')
            and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/vehicle/{vehicleId}/' and method = 'DELETE')
                AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/vehicle/{vehicleId}/' and method = 'DELETE')
            and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
        IF EXISTS(
                SELECT 1
                FROM vehicle.roles vr
                WHERE vr.url_id = (select id from vehicle.urls where url = '/transport/deactivate/{transportId}/' and method = 'PATCH')
                AND vr.role = 'ROLE_ADMIN_DATA_MASTER'
            ) THEN
            delete
            from vehicle.roles vr
            where vr.url_id = (select id from vehicle.urls where url = '/transport/deactivate/{transportId}/' and method = 'PATCH')
            and vr.role = 'ROLE_ADMIN_DATA_MASTER';
        END IF;
    END
$do$;