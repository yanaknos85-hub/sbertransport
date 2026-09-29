call migrations.fill_roles('addresses', 'GET /self/addresses/', 'ROLE_EMPLOYEE_CORP_CLIENT', true);

call migrations.fill_roles('addresses', 'POST /self/addresses/favorite/', 'ROLE_EMPLOYEE_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'PUT /self/addresses/favorite/{id}/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'DELETE /self/addresses/favorite/{id}/', 'ROLE_EMPLOYEE_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /self/addresses/favorite/{id}/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /self/addresses/favorite/', 'ROLE_ENGINEER_CORP_CLIENT', true);

call migrations.fill_roles('addresses', 'GET /self/addresses/frequently/', 'ROLE_EMPLOYEE_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'DELETE /self/addresses/favorite/{addressId}/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'DELETE /self/addresses/frequently/{addressId}/', 'ROLE_ENGINEER_CORP_CLIENT', true);

call migrations.fill_roles('addresses', 'POST /self/addresses/meeting/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'POST /self/addresses/meeting/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'POST /self/addresses/meeting/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'PUT /self/addresses/meeting/{id}/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'PUT /self/addresses/meeting/{id}/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'PUT /self/addresses/meeting/{id}/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'DELETE /self/addresses/meeting/{id}/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'DELETE /self/addresses/meeting/{id}/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'DELETE /self/addresses/meeting/{id}/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /self/addresses/meeting/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'GET /self/addresses/meeting/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'GET /self/addresses/meeting/', 'ROLE_ENGINEER_CORP_CLIENT', true);

call migrations.fill_roles('addresses', 'GET /files/meetingAddress/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'GET /files/meetingAddress/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /files/meetingAddress/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'GET /files/meetingAddress/result/{fileName}/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'GET /files/meetingAddress/result/{fileName}/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /files/meetingAddress/result/{fileName}/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'GET /files/meetingAddress/empty/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'GET /files/meetingAddress/empty/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /files/meetingAddress/empty/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'GET /files/meetingAddress/{id}/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'GET /files/meetingAddress/{id}/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /files/meetingAddress/{id}/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'GET /files/meetingAddress/result/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'GET /files/meetingAddress/result/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'GET /files/meetingAddress/result/', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('addresses', 'POST /files/meetingAddress/', 'ROLE_MAINTENANCE_ENGINEER', true);
call migrations.fill_roles('addresses', 'POST /files/meetingAddress/', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('addresses', 'POST /files/meetingAddress/', 'ROLE_ADMIN_DATA_MASTER', true);