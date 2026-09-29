alter table request.request_for_cargo drop column organization_id;
alter table request.request_for_taxi drop column organization_id;
alter table request.request_for_personal drop column organization_id;
alter table request.request_for_public drop column organization_id;
alter table request.request_for_carsharing drop column organization_id;