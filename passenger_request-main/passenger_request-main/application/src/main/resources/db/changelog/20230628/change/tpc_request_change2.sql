alter table request.request_for_personal add column cost_share_part double precision;
comment on column request.request_for_personal.cost_share_part is 'Коэффициент части оплаты';
alter table request.request_for_personal add column savings_cash bigint;
comment on column request.request_for_personal.savings_cash is 'Экономия в рублях';
alter table request.request_for_personal add column savings_procents bigint;
comment on column request.request_for_personal.savings_procents is 'Экономия в процентах';

alter table request.request_for_taxi add column cost_share_part double precision;
comment on column request.request_for_taxi.cost_share_part is 'Коэффициент части оплаты';
alter table request.request_for_taxi add column savings_cash bigint;
comment on column request.request_for_taxi.savings_cash is 'Экономия в рублях';
alter table request.request_for_taxi add column savings_procents bigint;
comment on column request.request_for_taxi.savings_procents is 'Экономия в процентах';

alter table request.request_for_carsharing add column cost_share_part double precision;
comment on column request.request_for_carsharing.cost_share_part is 'Коэффициент части оплаты';
alter table request.request_for_carsharing add column savings_cash bigint;
comment on column request.request_for_carsharing.savings_cash is 'Экономия в рублях';
alter table request.request_for_carsharing add column savings_procents bigint;
comment on column request.request_for_carsharing.savings_procents is 'Экономия в процентах';

