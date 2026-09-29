insert into telemechanic.attorney
(id, number, issue_date, expiry_date, creation_system)
values
('382dac4a-069e-4ce9-a0ed-95ae7c8dc2d6', '57fc7b44-8c8c-420f-bf87-d62e4606339d', '1999-01-01', '2000-01-01', 'sys');

insert into telemechanic.dispatcher
(id, employee_id, organization_id, department_id, attorney_id, active)
values
('a8dd0938-d8ab-4ed7-8d82-1f5b46240fed', '3cd35c19-fd39-413c-99a0-30f35bd642a8', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6',
'482e6dcb-03a9-4927-90b4-c7081114a9d8', '382dac4a-069e-4ce9-a0ed-95ae7c8dc2d6', true);