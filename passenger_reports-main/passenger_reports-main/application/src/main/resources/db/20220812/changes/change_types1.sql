delete from reports.shared_ride;

alter table reports.request drop column magenta_order_id;
alter table reports.request add column ride_id UUID;

alter table reports.request drop column shared_ride_id;
alter table reports.request add column shared_ride_id UUID;

alter table reports.shared_ride drop column magenta_id;
alter table reports.shared_ride add column magenta_id UUID not null constraint shared_ride_pkey primary key;

alter table reports.order_kpi drop column order_id;
alter table reports.order_kpi add column order_id UUID;

alter table reports.taxi_trip add column ride_id UUID;
alter table reports.taxi_trip drop column shared_request_id;
alter table reports.taxi_trip add column shared_ride_id UUID;

alter table reports.order_kpi drop constraint if exists order_kpi_savings_check;
alter table reports.order_kpi drop constraint if exists order_kpi_savings_pct_check;

alter table reports.order_kpi rename column order_id to request_id;

