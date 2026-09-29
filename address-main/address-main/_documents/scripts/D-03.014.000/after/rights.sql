call migrations.fill_roles('addresses', 'GET /self/', 'ROLE_EMPLOYEE_CORP_CLIENT', true);

call migrations.fill_roles('addresses', 'GET /favorite/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /favorite/', 'ROLE_EMPLOYEE_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'POST /favorite/', 'ROLE_EMPLOYEE_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'PUT /favorite/{id}/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /favorite/{id}/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'DELETE /favorite/{id}/', 'ROLE_EMPLOYEE_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'DELETE /favorite/{id}/', 'ROLE_ENGINEER_CORP_CLIENT', true);

call migrations.fill_roles('addresses', 'GET /frequently/', 'ROLE_EMPLOYEE_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'DELETE /frequently/{id}/', 'ROLE_ENGINEER_CORP_CLIENT', true);

call migrations.fill_roles('addresses', 'POST /meeting/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'POST /meeting/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'POST /meeting/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /meeting/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'GET /meeting/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'GET /meeting/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'PUT /meeting/{id}/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'PUT /meeting/{id}/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'PUT /meeting/{id}/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'DELETE /meeting/{id}/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'DELETE /meeting/{id}/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'DELETE /meeting/{id}/', 'ROLE_ENGINEER_CORP_CLIENT', true);

call migrations.fill_roles('addresses', 'GET /files/meeting/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'GET /files/meeting/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /files/meeting/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'GET /files/meeting/result/{fileName}/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'GET /files/meeting/result/{fileName}/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /files/meeting/result/{fileName}/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'GET /files/meeting/empty/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'GET /files/meeting/empty/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /files/meeting/empty/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'GET /files/meeting/{id}/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'GET /files/meeting/{id}/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /files/meeting/{id}/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'GET /files/meeting/result/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'GET /files/meeting/result/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /files/meeting/result/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'POST /files/meeting/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'POST /files/meeting/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'POST /files/meeting/', 'ROLE_ADMIN_DATA_MASTER', true);