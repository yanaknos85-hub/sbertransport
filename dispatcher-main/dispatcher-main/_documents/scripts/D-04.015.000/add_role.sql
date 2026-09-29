insert into roles ."role" (code, "name", description, data_master, default_for, "exclusive")
values ('ROLE_MAIN_DISPATCHER_CONTRACTOR',
'Менеджер автопарка',
'Роль для управления автопарком организации',
false,
'[]'::json,
'["EXTERNAL","INTERNAL"]'::json);

insert into authentication."role" (code, "name", description, data_master, default_for)
values ('ROLE_MAIN_DISPATCHER_CONTRACTOR',
'Менеджер автопарка',
'Роль для управления автопарком организации',
false,
'[]'::json);

insert into sudir."role" (code, "name", description, data_master, default_for)
values ('ROLE_MAIN_DISPATCHER_CONTRACTOR',
'Менеджер автопарка',
'Роль для управления автопарком организации',
false,
'[]'::json);

INSERT INTO authentication."role" (code,name,description,data_master,default_for) VALUES
	 ('ROLE_DISPATCHER_CONTRACTOR','Диспетчер филиала автопарка','Роль для сотрудников контрагентов, выполняющих функции диспетчера филиала',false,'["DISPATCHER"]')
	 on conflict (code) do update set "name" = 'Диспетчер филиала автопарка', description = 'Роль для сотрудников контрагентов, выполняющих функции диспетчера филиала', data_master = false, default_for ='["DISPATCHER"]' ;

INSERT INTO sudir."role" (code,name,description,data_master,default_for) VALUES
	 ('ROLE_DISPATCHER_CONTRACTOR','Диспетчер филиала автопарка','Роль для сотрудников контрагентов, выполняющих функции диспетчера филиала',false,'["DISPATCHER"]')
	 on conflict (code) do update set "name" = 'Диспетчер филиала автопарка', description = 'Роль для сотрудников контрагентов, выполняющих функции диспетчера филиала', data_master = false, default_for ='["DISPATCHER"]' ;

INSERT INTO roles."role" (code,name,description,data_master,default_for, "exclusive") VALUES
	 ('ROLE_DISPATCHER_CONTRACTOR','Диспетчер филиала автопарка','Роль для сотрудников контрагентов, выполняющих функции диспетчера филиала',false,'["DISPATCHER"]','["EXTERNAL","INTERNAL"]')
	 on conflict (code) do update set "name" = 'Диспетчер филиала автопарка', description = 'Роль для сотрудников контрагентов, выполняющих функции диспетчера филиала', data_master = false, default_for ='["DISPATCHER"]', "exclusive" = '["EXTERNAL","INTERNAL"]' ;

