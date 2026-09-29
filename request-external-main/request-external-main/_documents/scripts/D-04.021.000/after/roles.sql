call migrations.fill_roles('external_request', 'POST /report', 'ROLE_EMPLOYEE_CORP_CLIENT', true);
call migrations.fill_roles('external_request', 'POST /report', 'ROLE_ADMIN_DATA_MASTER', true);

call migrations.fill_roles('external_request', 'GET /organizations/{organizationId}/departments/filter-registry', 'ROLE_EMPLOYEE_CORP_CLIENT', true);
call migrations.fill_roles('external_request', 'GET /organizations/{organizationId}/departments/filter-registry', 'ROLE_ADMIN_DATA_MASTER', true);