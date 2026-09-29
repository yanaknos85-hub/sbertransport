call migrations.fill_roles('contractors', 'POST /', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('contractors', 'GET /{contractorId}/dispatcher', 'ROLE_ENGINEER_CORP_CLIENT', true);
call migrations.fill_roles('contractors', 'GET /{contractorId}/dispatcher/{dispatcherId}', 'ROLE_ENGINEER_CORP_CLIENT', true);