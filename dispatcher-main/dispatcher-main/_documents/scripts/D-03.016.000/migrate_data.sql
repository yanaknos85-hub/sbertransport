insert into dispatcher.contractor
(id, name, active, main_dispatcher_id, autoassign, employee_count, technical_account_owner_email,
 technical_account_owner, tin)
select id,
       name,
       active,
       null,
       autoassign,
       employee_count,
       null,
       null,
       tin
from contractors.contractor
where contractor.integration_type = 'DISPATCHER'
on conflict do nothing;

update dispatcher.contractor
set name               = cc.name,
    active             = cc.active,
    main_dispatcher_id = null,
    autoassign         = cc.autoassign,
    employee_count     = cc.employee_count,
    tin                = cc.tin
from contractors.contractor as cc
where dispatcher.contractor.id = cc.id;

insert into dispatcher.dispatcher
(id, human_readable_id, last_name, first_name, patronymic, phone, email, contractor_id, active, consent)
select id,
       human_readable_id,
       last_name,
       first_name,
       patronymic,
       phone,
       email,
       contractor_id,
       active,
       consent
from contractors.dispatcher
where contractor_id in (select id from dispatcher.contractor)
on conflict do nothing;

update dispatcher.dispatcher
set human_readable_id = cd.human_readable_id,
    last_name         = cd.last_name,
    first_name        = cd.first_name,
    patronymic        = cd.patronymic,
    phone             = cd.phone,
    email             = cd.email,
    contractor_id     = cd.contractor_id,
    active            = cd.active,
    consent           = cd.consent
from contractors.dispatcher as cd
where dispatcher.id = cd.id;

update dispatcher.contractor
set main_dispatcher_id = c.main_dispatcher_id
from contractors.contractor as c
where dispatcher.contractor.id = c.id;

insert into dispatcher.attribute
    (id, contractor_id, name, status)
select id, contractor_id, name, status
from contractors.attribute
where contractor_id in (select id from dispatcher.contractor)
on conflict do nothing;

update dispatcher.attribute
set contractor_id = ca.contractor_id,
    name          = ca.name,
    status        = ca.status
from contractors.attribute as ca
where dispatcher.attribute.id = ca.id;

insert into dispatcher.autopark
    (id, name, contractor_id, active)
select id, name, contractor_id, active
from contractors.autopark
where contractor_id in (select id from dispatcher.contractor)
on conflict do nothing;

update dispatcher.autopark
set name          = ca.name,
    contractor_id = ca.contractor_id,
    active        = ca.active
from contractors.autopark as ca
where dispatcher.autopark.id = ca.id;

insert into dispatcher.driver
(id, contractor_id, last_name, first_name, patronymic, passport, is_active, contact_phone_number, rating,
 driver_license_number, service_license_number, experience, humanreadableid, email, latitude, longitude, point_time,
 time_zone, cargo_licence_number, driver_speciality, consent, online)
select id,
       contractor_id,
       last_name,
       first_name,
       patronymic,
       passport,
       is_active,
       contact_phone_number,
       rating,
       driver_license_number,
       service_license_number,
       experience,
       humanreadableid,
       email,
       latitude,
       longitude,
       point_time,
       time_zone,
       cargo_licence_number,
       driver_speciality,
       consent,
       online
from contractors.driver
where contractor_id in (select id from dispatcher.contractor)
on conflict do nothing;

update dispatcher.driver
set contractor_id          = cd.contractor_id,
    last_name              = cd.last_name,
    first_name             = cd.first_name,
    patronymic             = cd.patronymic,
    passport               = cd.passport,
    is_active              = cd.is_active,
    contact_phone_number   = cd.contact_phone_number,
    rating                 = cd.rating,
    driver_license_number  = cd.driver_license_number,
    service_license_number = cd.service_license_number,
    experience             = cd.experience,
    humanreadableid        = cd.humanreadableid,
    email                  = cd.email,
    latitude               = cd.latitude,
    longitude              = cd.longitude,
    point_time             = cd.point_time,
    time_zone              = cd.time_zone,
    cargo_licence_number   = cd.cargo_licence_number,
    driver_speciality      = cd.driver_speciality,
    consent                = cd.consent,
    online                 = cd.online
from contractors.driver as cd
where dispatcher.driver.id = cd.id;

insert into dispatcher.driver_attribute
    (driver, attribute)
select driver, attribute
from contractors.driver_attribute
where driver in (select id from dispatcher.driver)
on conflict do nothing;

update dispatcher.driver_attribute
set driver    = cda.driver,
    attribute = cda.attribute
from contractors.driver_attribute cda
where dispatcher.driver_attribute.driver = cda.driver;

insert into dispatcher.driver_licenses
    (driver_id, license)
select driver_id, license
from contractors.driver_licenses
where driver_id in (select id from dispatcher.driver)
on conflict do nothing;

update dispatcher.driver_licenses
set driver_id = cdl.driver_id,
    license   = cdl.license
from contractors.driver_licenses as cdl
where dispatcher.driver_licenses.driver_id = cdl.driver_id;

insert into dispatcher.shift
(id, contractor_id, driver_id, vehicle_id, start_date, end_date, is_deleted, active)
select id,
       contractor_id,
       driver_id,
       vehicle_id,
       start_date,
       end_date,
       is_deleted,
       active
from contractors.shift
where contractor_id in (select id from dispatcher.contractor)
on conflict do nothing;

update dispatcher.shift
set contractor_id = cs.contractor_id,
    driver_id     = cs.driver_id,
    vehicle_id    = cs.vehicle_id,
    start_date    = cs.start_date,
    end_date      = cs.end_date,
    is_deleted    = cs.is_deleted,
    active        = cs.active
from contractors.shift as cs
where dispatcher.shift.id = cs.id;

insert into dispatcher.vehicle
(id, passport, state_number, vin, body_type, chassis_type, color, eco_class, fuel_consumption, insurance_number,
 manufacture_year, max_allowed_weight, mileage, package_class, transmission_type, autopark_id, engine_type,
 in_exploitation, active, model_name, model_brand, model_year, vehicle_type, vehicle_additional)
select id,
       passport,
       state_number,
       vin,
       body_type,
       chassis_type,
       color,
       eco_class,
       fuel_consumption,
       insurance_number,
       manufacture_year,
       max_allowed_weight,
       mileage,
       package_class,
       transmission_type,
       autopark_id,
       engine_type,
       in_exploitation,
       active,
       model_name,
       model_brand,
       model_year,
       vehicle_type,
       vehicle_additional
from contractors.vehicle
where autopark_id in (select id from dispatcher.autopark)
on conflict do nothing;

update dispatcher.vehicle
set passport = cv.passport,
    state_number = cv.state_number,
    vin = cv.vin,
    body_type = cv.body_type,
    chassis_type = cv.chassis_type,
    color = cv.color,
    eco_class = cv.eco_class,
    fuel_consumption = cv.fuel_consumption,
    insurance_number = cv.insurance_number,
    manufacture_year = cv.manufacture_year,
    max_allowed_weight = cv.max_allowed_weight,
    mileage = cv.mileage,
    package_class = cv.package_class,
    transmission_type = cv.transmission_type,
    autopark_id = cv.autopark_id,
    engine_type = cv.engine_type,
    in_exploitation = cv.in_exploitation,
    active = cv.active,
    model_name = cv.model_name,
    model_brand = cv.model_brand,
    model_year = cv.model_year,
    vehicle_type = cv.vehicle_type,
    vehicle_additional = cv.vehicle_additional
from contractors.vehicle as cv
where dispatcher.vehicle.id = cv.id;





