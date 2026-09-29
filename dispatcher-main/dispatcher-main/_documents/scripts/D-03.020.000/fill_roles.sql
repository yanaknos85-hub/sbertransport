call migrations.fill_roles('dispatcher', 'GET /transport/', 'ROLE_EXTERNAL_REQUEST', true);
call migrations.fill_roles('contractors', 'GET /{contractorId}/transport/', 'ROLE_EMPLOYEE_CORP_CLIENT', true);

call migrations.fill_roles('dispatcher', 'GET /transport/{vehicleId}/trips/', 'ROLE_EXTERNAL_REQUEST', true);
call migrations.fill_roles('contractors', 'GET /{contractorId}/transport/{vehicleId}/trips/', 'ROLE_EMPLOYEE_CORP_CLIENT', true);