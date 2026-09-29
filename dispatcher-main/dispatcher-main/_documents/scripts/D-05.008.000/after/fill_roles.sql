call migrations.fill_roles('dispatcher', 'GET /shifts/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('dispatcher', 'GET /shifts/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('dispatcher', 'POST /shifts/ewb/first-title/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('dispatcher', 'POST /shifts/ewb/first-title/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('dispatcher', 'POST /shifts/ewb/sign/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('dispatcher', 'POST /shifts/ewb/sign/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('dispatcher', 'PATCH /{contractorId}/dispatcher/{dispatcherId}/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('dispatcher', 'PATCH /{contractorId}/dispatcher/{dispatcherId}/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('dispatcher', 'PATCH /{contractorId}/drivers/{driverId}/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('dispatcher', 'PATCH /{contractorId}/drivers/{driverId}/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);