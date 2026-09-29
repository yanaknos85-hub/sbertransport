INSERT INTO authentication."role" (code,name,description,data_master,default_for) VALUES
	 ('ROLE_DRIVER_CONTRACTOR','Водитель филиала автопарка','Роль для сотрудников контрагентов, выполняющих функции водителя филиала',false,'["DRIVER"]')
	 on conflict (code) do update set "name" = 'Водитель филиала автопарка', description = 'Роль для сотрудников контрагентов, выполняющих функции водителя филиала', data_master = false, default_for ='["DRIVER"]' ;

INSERT INTO sudir."role" (code,name,description,data_master,default_for) VALUES
	 ('ROLE_DRIVER_CONTRACTOR','Водитель филиала автопарка','Роль для сотрудников контрагентов, выполняющих функции водителя филиала',false,'["DRIVER"]')
	 on conflict (code) do update set "name" = 'Водитель филиала автопарка', description = 'Роль для сотрудников контрагентов, выполняющих функции водителя филиала', data_master = false, default_for ='["DRIVER"]' ;

INSERT INTO roles."role" (code,name,description,data_master,default_for, "exclusive") VALUES
	 ('ROLE_DRIVER_CONTRACTOR','Водитель филиала автопарка','Роль для сотрудников контрагентов, выполняющих функции водителя филиала',false,'["DRIVER"]','["EXTERNAL","INTERNAL"]')
	 on conflict (code) do update set "name" = 'Водитель филиала автопарка', description = 'Роль для сотрудников контрагентов, выполняющих функции водителя филиала', data_master = false, default_for ='["DRIVER"]', "exclusive" = '["EXTERNAL","INTERNAL"]' ;
