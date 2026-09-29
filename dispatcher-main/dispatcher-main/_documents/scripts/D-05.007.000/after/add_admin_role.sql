call migrations.fill_roles('dispatcher', 'GET /{contractorId}/vehicle-shift/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('trips', 'POST /self/dispatcher/vehicle/busyness/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('trips_cargo', 'POST /self/dispatcher/vehicle/busyness/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('dispatcher', 'POST /{contractorId}/vehicle-shift/status/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);