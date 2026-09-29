alter table request.request_for_cargo add column organization_id UUID;
alter table request.request_for_taxi add column organization_id UUID;
alter table request.request_for_personal add column organization_id UUID;
alter table request.request_for_public add column organization_id UUID;
alter table request.request_for_carsharing add column organization_id UUID;