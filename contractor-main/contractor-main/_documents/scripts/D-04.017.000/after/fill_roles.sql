call migrations.fill_roles('contractors', 'POST /internal-auto-park/branches/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('contractors', 'POST /internal-auto-park/branches/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('contractors', 'POST /internal-auto-park/branches/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('contractors', 'PUT /internal-auto-park/branches/{externalId}/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('contractors', 'PUT /internal-auto-park/branches/{externalId}/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('contractors', 'PUT /internal-auto-park/branches/{externalId}/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('contractors', 'DELETE /internal-auto-park/branches/{externalId}/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('contractors', 'DELETE /internal-auto-park/branches/{externalId}/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('contractors', 'DELETE /internal-auto-park/branches/{externalId}/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('contractors', 'GET /internal-auto-park/branches/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('contractors', 'GET /internal-auto-park/branches/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('contractors', 'GET /internal-auto-park/branches/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('contractors', 'GET /internal-auto-park/staff/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('contractors', 'GET /internal-auto-park/staff/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('contractors', 'GET /internal-auto-park/staff/', 'ROLE_DISPATCHER_CONTRACTOR', true);