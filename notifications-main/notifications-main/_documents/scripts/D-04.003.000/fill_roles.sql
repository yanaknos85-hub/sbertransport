CALL migrations.fill_roles('notifications','GET /user/settings','ROLE_EMPLOYEE_CORP_CLIENT',true);
CALL migrations.fill_roles('notifications','POST /user/settings','ROLE_EMPLOYEE_CORP_CLIENT',true);
CALL migrations.fill_roles('notifications','PUT /user/settings/{userId}','ROLE_EMPLOYEE_CORP_CLIENT',true);