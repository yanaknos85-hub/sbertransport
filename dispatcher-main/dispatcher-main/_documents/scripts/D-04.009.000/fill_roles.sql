CALL migrations.fill_roles('dispatcher','PATCH /self/dispatcher/','ROLE_DISPATCHER_CONTRACTOR',true);

CALL migrations.fill_roles('dispatcher','PATCH /self/driver/','ROLE_DRIVER_CONTRACTOR',true);

CALL migrations.fill_roles('user_data_confirmation','PUT /phone/','ROLE_EMPLOYEE_CORP_CLIENT',true);
CALL migrations.fill_roles('user_data_confirmation','PUT /phone/','ROLE_DRIVER_CONTRACTOR',true);
CALL migrations.fill_roles('user_data_confirmation','PUT /phone/','ROLE_DISPATCHER_CONTRACTOR',true);
