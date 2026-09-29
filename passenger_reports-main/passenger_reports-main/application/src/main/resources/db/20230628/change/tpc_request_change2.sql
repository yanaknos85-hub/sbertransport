alter table reports.request add column cost_share_part double precision;
comment on column reports.request.cost_share_part is 'Коэффициент части оплаты';
alter table reports.request add column savings_cash bigint;
comment on column reports.request.savings_cash is 'Экономия в рублях';
alter table reports.request add column savings_procents bigint;
comment on column reports.request.savings_procents is 'Экономия в процентах';
alter table reports.request add column shared_ride_owner boolean;
comment on column reports.request.shared_ride_owner is 'Инициатор поездки';
