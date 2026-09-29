call migrations.fill_roles('contractors', 'GET /files/{type}/empty', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('contractors', 'GET /files/{type}/empty', 'ROLE_ADMIN_CORP_CLIENT', true);call migrations.fill_roles('contractors', 'GET /files/{type}/empty', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('contractors', 'POST /files/{type}', 'ROLE_ADMIN_DATA_MASTER', true);call migrations.fill_roles('contractors', 'GET /files/{type}/empty', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('contractors', 'POST /files/{type}', 'ROLE_ADMIN_CORP_CLIENT', true);
call migrations.fill_roles('contractors', 'GET /files/{type}/result/{fileName}', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('contractors', 'GET /files/{type}/result/{fileName}', 'ROLE_ADMIN_CORP_CLIENT', true);
call migrations.fill_roles('contractors', 'GET /files/{type}/result', 'ROLE_ADMIN_DATA_MASTER', true);
call migrations.fill_roles('contractors', 'GET /files/{type}/result', 'ROLE_ADMIN_CORP_CLIENT', true);