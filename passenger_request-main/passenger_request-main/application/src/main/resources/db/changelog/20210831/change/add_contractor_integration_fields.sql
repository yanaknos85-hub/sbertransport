drop table if exists request.contractor;
alter table request.contractor_message add column if not exists contractor_name varchar(255);
alter table request.contractor_message add column if not exists contractor_rus_name varchar(255);
alter table request.contractor_message add column if not exists integration_email varchar(255);

comment on column request.contractor_message.contractor_name is 'Название контрагента латиницей';
comment on column request.contractor_message.contractor_rus_name is 'Название контрагента кириллицей';
comment on column request.contractor_message.integration_email is 'Интеграционный email контрагента';