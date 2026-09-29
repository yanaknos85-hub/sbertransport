call migrations.fill_roles('vehicle', 'POST /transport/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('vehicle', 'POST /transport/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('vehicle', 'POST /transport/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('vehicle', 'PATCH /transport/{tracnsportId}/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('vehicle', 'PATCH /transport/{tracnsportId}/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('vehicle', 'PATCH /transport/{tracnsportId}/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('vehicle', 'POST /transport/search/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('vehicle', 'POST /transport/search/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('vehicle', 'POST /transport/search/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('vehicle', 'PATCH /transport/deactivate/{transportId}/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('vehicle', 'PATCH /transport/deactivate/{transportId}/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('vehicle', 'PATCH /transport/deactivate/{transportId}/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('vehicle', 'GET /vehicle/brand/all/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('vehicle', 'GET /vehicle/brand/all/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('vehicle', 'GET /vehicle/brand/all/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('vehicle', 'GET /vehicle/model/all/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('vehicle', 'GET /vehicle/model/all/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('vehicle', 'GET /vehicle/model/all/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('vehicle', 'GET /vehicle/telematics/all/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('vehicle', 'GET /vehicle/telematics/all/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('vehicle', 'GET /vehicle/telematics/all/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('vehicle', 'GET /vehicle/type/all/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('vehicle', 'GET /vehicle/type/all/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('vehicle', 'GET /vehicle/type/all/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('vehicle', 'GET /vehicle/subtype/all/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('vehicle', 'GET /vehicle/subtype/all/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('vehicle', 'GET /vehicle/subtype/all/', 'ROLE_DISPATCHER_CONTRACTOR', true);