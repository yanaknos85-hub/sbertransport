call migrations.fill_roles('dispatcher', 'GET /files/shifts/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('dispatcher', 'GET /files/shifts/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('dispatcher', 'GET /files/shifts/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('dispatcher', 'POST /files/shifts/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('dispatcher', 'POST /files/shifts/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('dispatcher', 'POST /files/shifts/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('dispatcher', 'GET /files/shifts/result/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('dispatcher', 'GET /files/shifts/result/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('dispatcher', 'GET /files/shifts/result/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('dispatcher', 'GET /files/shifts/result/{fileName}/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('dispatcher', 'GET /files/shifts/result/{fileName}/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('dispatcher', 'GET /files/shifts/result/{fileName}/', 'ROLE_DISPATCHER_CONTRACTOR', true);