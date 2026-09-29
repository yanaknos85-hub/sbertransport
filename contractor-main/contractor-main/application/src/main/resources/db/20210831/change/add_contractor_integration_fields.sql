alter table contractors.contractor add column if not exists contractor_name varchar(255);
update contractors.contractor set contractor_name='yandex' where contractor_name is null;
alter table contractors.contractor add column if not exists contractor_rus_name varchar(255);
update contractors.contractor set contractor_rus_name='ТАКСИЯНДЕКС' where contractor_rus_name is null;
alter table contractors.contractor add column if not exists integration_email varchar(255);
update contractors.contractor set contractor_name='robot-sber-test@yandex-team.ru' where contractor_name is null;

comment on column contractors.contractor.contractor_name is 'Название контрагента латиницей';
comment on column contractors.contractor.contractor_rus_name is 'Название контрагента кириллицей';
comment on column contractors.contractor.integration_email is 'Интеграционный email контрагента';