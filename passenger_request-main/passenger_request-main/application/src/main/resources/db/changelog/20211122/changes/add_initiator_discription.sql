alter table request.request_for_carsharing_history
add column if not exists initiator_description varchar(128) not null default 'EMPLOYEE';

alter table request.request_for_personal_history
add column if not exists initiator_description varchar(128) not null default 'EMPLOYEE';

alter table request.request_for_public_history
add column if not exists initiator_description varchar(128) not null default 'EMPLOYEE';

alter table request.request_for_taxi_history
add column if not exists initiator_description varchar(128) not null default 'EMPLOYEE';

comment on column request.request_for_carsharing_history.initiator_description is 'Тип сотрудника';
comment on column request.request_for_personal_history.initiator_description is 'Тип сотрудника';
comment on column request.request_for_public_history.initiator_description is 'Тип сотрудника';
comment on column request.request_for_taxi_history.initiator_description is 'Тип сотрудника';
