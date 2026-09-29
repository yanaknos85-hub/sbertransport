call migrations.fill_roles('contractors', 'POST /internal-auto-park/staff/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('contractors', 'POST /internal-auto-park/staff/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('contractors', 'POST /internal-auto-park/staff/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('dispatcher', 'POST /{contractorId}/dispatcher/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('dispatcher', 'POST /{contractorId}/drivers/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);