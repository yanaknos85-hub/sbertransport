call migrations.fill_roles('vehicle', 'POST /brand/all/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('vehicle', 'POST /brand/all/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('vehicle', 'POST /brand/all/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('vehicle', 'POST /model/all/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('vehicle', 'POST /model/all/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('vehicle', 'POST /model/all/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('vehicle', 'GET /transport/{transportId}/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('vehicle', 'GET /transport/{transportId}/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('vehicle', 'GET /transport/{transportId}/', 'ROLE_DISPATCHER_CONTRACTOR', true);