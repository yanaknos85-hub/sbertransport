DO
$do$
    BEGIN
        IF EXISTS(
                SELECT routine_schema,
                       routine_name,
                       routine_type
                FROM information_schema.routines
                WHERE routine_name = 'fill_roles'
                  and routine_schema = 'migrations'
                  and routine_type = 'PROCEDURE'
            ) THEN
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /request/on-the-line/'', ''ROLE_DRIVER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /request/active/'', ''ROLE_DRIVER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /ewb/request/'', ''ROLE_DRIVER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /ewb/close/'', ''ROLE_DRIVER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /request/close/{requestId}/'', ''ROLE_DRIVER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /request/{requestId}/'', ''ROLE_DRIVER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /request/{requestId}/cancel/'', ''ROLE_DRIVER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /request/create/'', ''ROLE_DRIVER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /telemedicine/{ewbId}/'', ''ROLE_DRIVER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /request/{requestId}/{checkType}/check/'', ''ROLE_DRIVER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''POST /ewb/odometer-out/'', ''ROLE_DRIVER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''PATCH /request/{requestId}/status/'', ''ROLE_DRIVER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /ewb/{ewbId}/qr/'', ''ROLE_DRIVER_CONTRACTOR'', true)';
            EXECUTE 'call migrations.fill_roles(''telemechanic'', ''GET /ewb/detailed/'', ''ROLE_DRIVER_CONTRACTOR'', true)';
        END IF;
    END
$do$;