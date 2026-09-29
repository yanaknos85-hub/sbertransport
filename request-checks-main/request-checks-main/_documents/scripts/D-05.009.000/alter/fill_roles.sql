call migrations.fill_roles('request_checks', 'POST /multipoint/limit', 'ROLE_EMPLOYEE_CORP_CLIENT', true);
call migrations.fill_roles('request_checks', 'POST /multipoint/limit', 'ROLE_ADMIN_DATA_MASTER', true);

call migrations.fill_roles('request_checks', 'POST /duration', 'ROLE_EMPLOYEE_CORP_CLIENT', true);
call migrations.fill_roles('request_checks', 'POST /duration', 'ROLE_ADMIN_DATA_MASTER', true);