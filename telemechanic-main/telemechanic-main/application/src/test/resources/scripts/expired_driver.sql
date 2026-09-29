insert into telemechanic.driving_license
(id , series, number, issue_date, expiry_date, previous_id, active)
values
('c048da90-fe87-4929-bbd9-21eb4c1e1a05', 'expired_series', 1111, '2000-01-01', '2010-01-01', null, true);

insert into telemechanic.driving_license_category
(driving_license_id, category_id)
values
('c048da90-fe87-4929-bbd9-21eb4c1e1a05', 'be8abe82-1cbd-4f0e-9b31-2b9db90231df');

insert into telemechanic.driver
(id, employee_id, driving_license_id, tin, snils)
values
('a7be6a7a-46a4-4ea3-b1b7-fe22e5822ca4', '15BDC75D-533B-44BB-9539-3DDC8693F3D3', 'c048da90-fe87-4929-bbd9-21eb4c1e1a05', '0000001', '186-345-573 03');
