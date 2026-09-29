alter table request.template_requests add column error_status varchar(20);
alter table request.template_requests add column error_comment varchar(2000);

comment on column request.template_requests.error_status is 'Статус ошибки';
comment on column request.template_requests.error_comment is 'Описание ошибки';