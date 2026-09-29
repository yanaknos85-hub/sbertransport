insert into trips_cargo.contractors (id,
                                     digit_id, autoassign) select id, digit_id, autoassign from contractors.contractor on conflict do nothing;

update trips_cargo.contractors set digit_id = c.digit_id, autoassign = c.autoassign from contractors.contractor as c where trips_cargo.contractors.id = c.id;

insert into trips_cargo.driver (
    id,
    human_readable_id,
    last_name,
    first_name,
    patronymic,
    passport,
    contractor_id,
    active,
    rating,
    driver_license_number,
    cargo_licence_number,
    latitude,
    longitude,
    point_time,
    time_zone,
    serving,
    online,
    active_trip_id,
    shift_id,
    experience,
    contact_phone,
    email) select id,
                  humanreadableid,
                  last_name,
                  first_name,
                  patronymic,
                  passport,
                  contractor_id,
                  is_active,
                  rating,
                  driver_license_number,
                  cargo_licence_number,
                  latitude,
                  longitude,
                  point_time,
                  time_zone,
                  serving,
                  online,
                  active_trip_id,
                  shift_id,
                  experience,
                  contact_phone_number,
                  email from contractors.driver where driver_speciality = 'CARGO'
                                                   or driver_speciality = 'BOTH' on conflict do nothing;

update trips_cargo.driver set human_readable_id = dr.humanreadableid,
                              last_name =  dr.last_name,
                              first_name = dr.first_name,
                              patronymic = dr.patronymic,
                              passport = dr.passport,
                              contractor_id = dr.contractor_id,
                              active = dr.is_active,
                              rating = dr.rating,
                              driver_license_number = dr.driver_license_number,
                              cargo_licence_number = dr.cargo_licence_number,
                              latitude = dr.latitude,
                              longitude = dr.longitude,
                              point_time = dr.point_time,
                              time_zone = dr.time_zone,
                              serving = dr.serving,
                              online = dr.online,
                              active_trip_id = dr.active_trip_id,
                              shift_id = dr.shift_id,
                              experience = dr.experience,
                              contact_phone = dr.contact_phone_number,
                              email = dr.email from contractors.driver as dr where trips_cargo.driver.id = dr.id;


insert into trips_cargo.dispatcher (id,
                                    human_readable_id,
                                    last_name,
                                    first_name,
                                    patronymic,
                                    phone,
                                    email,
                                    contractor_id) select id,
                                                          human_readable_id,
                                                          last_name,
                                                          first_name,
                                                          patronymic,
                                                          phone,
                                                          email,
                                                          contractor_id
from contractors.dispatcher on conflict do nothing;

update trips_cargo.dispatcher set human_readable_id = ds.human_readable_id,
                                  last_name = ds.last_name,
                                  first_name = ds.first_name,
                                  patronymic = ds.patronymic,
                                  phone = ds.phone,
                                  email = ds.email,
                                  contractor_id = ds.contractor_id from contractors.dispatcher
                                                                            as ds where trips_cargo.dispatcher.id = ds.id;

insert into trips_cargo.vehicle (id,
                                 brand,
                                 model,
                                 state_number,
                                 color,
                                 contractor_id,
                                 deleted) select id,
                                                 model_brand,
                                                 model_name,
                                                 state_number,
                                                 color,
                                                 (select contractor_id from contractors.autopark where id = contractors.vehicle.autopark_id),
                                                 not active
from contractors.vehicle on conflict do nothing;

update trips_cargo.vehicle set brand = vh.model_brand,
                               model = vh.model_name,
                               state_number = vh.state_number,
                               color = vh.color,
                               contractor_id = (select contractor_id from contractors.autopark where id = vh.autopark_id),
                               deleted = not vh.active
from contractors.vehicle as vh where trips_cargo.vehicle.id = vh.id;

insert into trips_cargo.shift (id,
                               contractor_id,
                               driver_id,
                               vehicle_id,
                               start_date,
                               end_date,
                               deleted,
                               active) select id,
                                              contractor_id,
                                              driver_id,
                                              vehicle_id,
                                              start_date,
                                              end_date,
                                              is_deleted,
                                              active
from contractors.shift where driver_id in (select id from contractors.driver where driver_speciality = 'CARGO'
                                                                                or driver_speciality = 'BOTH') on conflict do nothing;

update trips_cargo.shift set contractor_id = sh.contractor_id,
                             driver_id = sh.driver_id,
                             vehicle_id = sh.vehicle_id,
                             start_date = sh.start_date,
                             end_date = sh.end_date,
                             deleted = sh.is_deleted,
                             active = sh.active from contractors.shift as sh where trips_cargo.shift.id = sh.id;

insert into trips_cargo.check_in (id,
                                  trip_id,
                                  longitude,
                                  latitude,
                                  time,
                                  time_zone,
                                  status,
                                  type) select id,
                                               trip_id,
                                               longitude,
                                               latitude,
                                               time,
                                               time_zone,
                                               status,
                                               type from contractors.check_in where trip_id in
                                                                                    (select id from contractors.trips
                                                                                     where type = 'CARGO')
on conflict do nothing;

update trips_cargo.check_in set trip_id = ch.trip_id,
                                longitude = ch.longitude,
                                latitude = ch.latitude,
                                time = ch.time,
                                time_zone = ch.time_zone,
                                status = ch.status,
                                type = ch.type from contractors.check_in as ch where trips_cargo.check_in.id = ch.id;

update trips_cargo.trips set status = t.status,
                             contractor_id = t.contractor_id,
                             start_time = t.start_time,
                             end_time = t.end_time,
                             digit_id = t.digit_id,
                             fact_distance = t.fact_distance,
                             driver_id = t.driver_id,
                             vehicle_id = t.vehicle_id,
                             dispatcher_id = t.dispatcher_id,
                             arrived_date = t.arrived_date,
                             autoassign_counter = t.autoassign_counter
from contractors.trips as t where trips_cargo.trips.id = t.id;

insert into trips_cargo.trip_history (change_time,
                                      trip_id,
                                      old_dispatcher_id,
                                      new_dispatcher_id) select change_time,
                                                                trip_id,
                                                                old_dispatcher_id,
                                                                new_dispatcher_id
from contractors.trip_history where trip_id in (select id from contractors.trips where type='CARGO') on conflict  do nothing;

update trips_cargo.trip_history set change_time = trh.change_time,
                                    trip_id = trh.trip_id,
                                    old_dispatcher_id = trh.old_dispatcher_id,
                                    new_dispatcher_id = trh.new_dispatcher_id from contractors.trip_history as trh
where trips_cargo.trip_history.change_time = trh.change_time
  and trips_cargo.trip_history.trip_id = trh.trip_id;