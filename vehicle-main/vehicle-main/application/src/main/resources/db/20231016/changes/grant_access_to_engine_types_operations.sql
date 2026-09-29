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
EXECUTE 'call migrations.fill_roles(''vehicle'', ''POST /engine-type'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''POST /engine-type'', ''ROLE_ADMIN_DATA_MASTER'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''POST /engine-type/all'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''POST /engine-type/all'', ''ROLE_ADMIN_DATA_MASTER'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''GET /engine-type/{typeId}'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''GET /engine-type/{typeId}'', ''ROLE_ADMIN_DATA_MASTER'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''PUT /engine-type/{typeId}'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''PUT /engine-type/{typeId}'', ''ROLE_ADMIN_DATA_MASTER'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''DELETE /engine-type/{typeId}'', ''ROLE_ADMIN_CORP_CLIENT'', true)';
EXECUTE 'call migrations.fill_roles(''vehicle'', ''DELETE /engine-type/{typeId}'', ''ROLE_ADMIN_DATA_MASTER'', true)';
END IF;
END
$do$;


