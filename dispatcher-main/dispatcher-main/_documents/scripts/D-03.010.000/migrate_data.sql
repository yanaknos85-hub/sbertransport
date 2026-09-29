insert into dispatcher.contractor
    (id, name, active, main_dispatcher_id, autoassign, employee_count, technical_account_owner_email, technical_account_owner, tin)
    select id, name, active, null, autoassign, employee_count, null, null, tin
from contractors.contractor where contractor.integration_type = 'DISPATCHER' on conflict do nothing;

insert into dispatcher.dispatcher
    (id, human_readable_id, last_name, first_name, patronymic, phone, email, contractor_id, active, consent)
    select id, human_readable_id, last_name, first_name, patronymic, phone, email, contractor_id, active, consent from contractors.dispatcher
        where contractor_id in (select id from dispatcher.contractor) on conflict do nothing;

update dispatcher.contractor set main_dispatcher_id = c.main_dispatcher_id
from contractors.contractor as c where dispatcher.contractor.id = c.id;

insert into dispatcher.attribute
    (id, contractor_id, name, status)
    select id, contractor_id, name, status
from contractors.attribute where contractor_id in (select id from dispatcher.contractor) on conflict do nothing;

insert into dispatcher.autopark
(id, name, contractor_id, active)
select id, name, contractor_id, active
from contractors.autopark where contractor_id in (select id from dispatcher.contractor) on conflict do nothing;

insert into dispatcher.driver
(id, contractor_id, last_name, first_name, patronymic, passport, is_active, contact_phone_number, rating, driver_license_number, service_license_number, experience, humanreadableid, email, latitude, longitude, point_time, time_zone, serving, online, active_trip_id, shift_id, cargo_licence_number, driver_speciality, consent)
select id, contractor_id, last_name, first_name, patronymic, passport, is_active, contact_phone_number, rating, driver_license_number, service_license_number, experience, humanreadableid, email, latitude, longitude, point_time, time_zone, serving, online, active_trip_id, shift_id, cargo_licence_number, driver_speciality, consent
from contractors.driver where contractor_id in (select id from dispatcher.contractor) on conflict do nothing;

insert into dispatcher.driver_attribute
(driver, attribute)
select driver, attribute
from contractors.driver_attribute where driver in (select id from dispatcher.driver) on conflict do nothing;

insert into dispatcher.driver_licenses
(driver_id, license)
select driver_id, license
from contractors.driver_licenses where driver_id in (select id from dispatcher.driver) on conflict do nothing;

insert into dispatcher.roles
(role, url_id)
select role, url_id
from contractors.roles on conflict do nothing;

insert into dispatcher.urls
(id, url, pattern, method)
select id, url, pattern, method
from contractors.urls on conflict do nothing;

insert into dispatcher.shift
(id, contractor_id, driver_id, vehicle_id, start_date, end_date, is_deleted, active)
select id, contractor_id, driver_id, vehicle_id, start_date, end_date, is_deleted, active
from contractors.shift where contractor_id in (select id from dispatcher.contractor) on conflict do nothing;

insert into dispatcher.vehicle
(id, passport, state_number, vin, body_type, chassis_type, color, eco_class, fuel_consumption, insurance_number, manufacture_year, max_allowed_weight, mileage, package_class, transmission_type, autopark_id, engine_type, in_exploitation, active, model_name, model_brand, model_year, vehicle_type, vehicle_additional)
select id, passport, state_number, vin, body_type, chassis_type, color, eco_class, fuel_consumption, insurance_number, manufacture_year, max_allowed_weight, mileage, package_class, transmission_type, autopark_id, engine_type, in_exploitation, active, model_name, model_brand, model_year, vehicle_type, vehicle_additional
from contractors.vehicle where autopark_id in (select id from dispatcher.autopark) on conflict do nothing;





