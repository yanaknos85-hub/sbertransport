call migrations.fill_roles('contractors', 'DELETE /internal-auto-park/staff/{externalId}/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('contractors', 'DELETE /internal-auto-park/staff/{externalId}/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('contractors', 'DELETE /internal-auto-park/staff/{externalId}/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('dispatcher', 'DELETE /{contractorId}/drivers/{driverId}/', 'ROLE_DISPATCHER_ROOM_ADMIN', true);
call migrations.fill_roles('dispatcher', 'DELETE /{contractorId}/drivers/{driverId}/', 'ROLE_MAIN_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('dispatcher', 'DELETE /{contractorId}/drivers/{driverId}/', 'ROLE_DISPATCHER_CONTRACTOR', true);