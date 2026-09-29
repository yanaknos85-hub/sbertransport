INSERT INTO request.tariff (id, humanreadableid, department_id, service_type, organization_id, region_id,
                            transport_type, active, contract_id, cost_per_km_suburb, cost_per_min_suburb,
                            suburb_service_cost_per_km, suburb_service_cost_per_min, cost_per_min_inter_region,
                            cost_per_km_inter_region, coef_engine_1_6, coef_engine_1_6_to_2_0, coef_engine_2_0_to_2_5,
                            coef_work_day_morning, coef_work_day_noon, coef_work_day_evening, coef_work_day_night,
                            coef_day_off, savings_deviation_pct, distance_deviation_km, time_deviation_min,
                            min_cancel_time_min, contractor_max_diff_computed_distance_percent,
                            contractor_max_diff_fact_distance_percent, contractor_max_diff_computed_cost_percent,
                            contractor_max_diff_contractor_cost_percent, contractor_max_diff_computed_waiting_percent,
                            ride_cost_per_min, minutes_included, distance_included, min_ride_distance_cost,
                            min_ride_time_cost, wait_cost_per_min, wait_cost_per_min_intermediate, seasonal_coefficient,
                            season_start, season_end, coef_traffic, coef_material_assets, trust_idx, contractor_id,
                            contractor_tariff_id, taxi_class, ride_cost_per_km, free_waiting_time, car_service_cost,
                            coef_child_seat, coef_pet_transport, coef_bicycle, coef_org, work_group, trigger_time,
                            is_night_tariff, coef_casco, metro_ticket_cost, tram_ticket_cost, trolleybus_ticket_cost,
                            bus_ticket_cost, city_local_train_cost, metro_availability, tram_availability,
                            trolleybus_availability, bus_availability, city_local_train_availability,
                            travel_card_metro_cost, travel_card_tram_cost, travel_card_trolleybus_cost,
                            travel_card_bus_cost, travel_card_local_train_cost, travel_card_all_city_transport_cost,
                            travel_card_metro_availability, travel_card_tram_availability,
                            travel_card_trolleybus_availability, travel_card_bus_availability,
                            travel_card_local_train_availability, travel_card_all_city_transport_availability, region,
                            min_create_time, min_cancel_time)
VALUES ('4b6f382d-f6fa-4478-88d5-9c62b25fe16b', 'TF-0001-00000943', '482e6dcb-03a9-4927-90b4-c7081114a9d8', 'EMPLOYEE_TRANSPORTATION',
        '6e6e04b7-1912-422a-820a-1a6777dfde1b', 'c526065a-9d20-4889-9921-83adcb217b61', 'CARSHARING', true,
        '48cc2bc9-7865-4f78-a543-d751ace4ce98', 100, 100, 100, 100, 0, 0, null, null, null, 1, 1, 1, 1, 1, 0, 0, 0, 30,
        20, 20, 20, 1, 20, 100, 0, 1, 100, 0, 100, 100, null, null, null, 1, null, null,
        'bc5f6b63-a646-4090-9644-2f2abc79d4d4', null, 'COMFORT', 100, 0, 0, 1, 1, 1, 1,
        '000001_Тестовая_организация_01/перевозка_пассажиров/Москва и Московская область', 5600, false, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, 'Москва и Московская область', null, null);

INSERT INTO request.contractor_message (id, deleted, url, login, password)
VALUES ('bc5f6b63-a646-4090-9644-2f2abc79d4d4', false, 'https://api.carsharing-request.com', 'test', 'test');

INSERT INTO request.trip_purpose (id, purpose, active, organization, id_uuid)
VALUES (1300, 'Тестовый_департамент_01', true, '6e6e04b7-1912-422a-820a-1a6777dfde1b',
        '91bf3e90-0e53-4ea4-b53e-57c68f77c615');

INSERT INTO request.request_for_carsharing
(id, humanreadableid, author_id, passenger_id, creation_time, transport_type,
tariff_id, request_status, status_code, approval_state, expected_cost, expected_distance, expected_time, trip_purpose,
request_options, desired_date, coop_trip, passenger_count, contractor_id, carsharing_class, active, time_zone,
bonus_cost, ride_id, shared_ride_owner, status_comment, organization_id, cost_share_part, savings_cash, savings_procents,
phone_number, employee_device_time_zone, approval_deadline, approval_deadline_state, rating_mark, rating_comment,
joined_passenger_ids, source, min_tariff_taxi, comment_for_purpose, executor_group_id, executor_group_name, outcome_tariff_id, outcome_expected_cost)
VALUES('81bf3e90-0e53-4da4-b53e-50c68f77c639', 'RF-0001-00000943', '3cd35c19-fd39-413c-99a0-30f35bd642a8',
 '3cd35c19-fd39-413c-99a0-30f35bd642a8', '2025-08-01 14:28:25.726370', 'CARSHARING', '4b6f382d-f6fa-4478-88d5-9c62b25fe16b',
 'CARSHARING_AWAITING_APPROVAL', 0, 'AWAITING_APPROVAL', 4600, 22.482, 1450000000000, '91bf3e90-0e53-4ea4-b53e-57c68f77c615',
 '[]', '2025-08-01 14:33:25.544414', false, 1, 'bc5f6b63-a646-4090-9644-2f2abc79d4d4', 'COMFORT', true, 'GMT+03', 0,
 'db6c813f-b0ed-4d79-9ea0-4bd1f86c1ed9', false, '', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 0, 0, 0, '+73123123123',
  'GMT+03', '2025-08-01 18:43:25.726370', 'NONE', 0, '', '[]', 'WEB', '{
    "cost": 4600,
    "tariffId": "4b6f382d-f6fa-4478-88d5-9c62b25fe16b"
  }', '', 'a64da45b-34a5-47ba-8820-dd2e9aee3ca2', 'СРБ/Московская область Восточное ГО/Пассажирские перевозки',
'4b6f382d-f6fa-4478-88d5-9c62b25fe16b', 4600);

INSERT INTO request.request_for_carsharing
(id, humanreadableid, author_id, passenger_id, creation_time, transport_type, approved_by_id, approval_date,
tariff_id, request_status, status_code, approval_state, expected_cost, expected_distance, expected_time, trip_purpose,
request_options, desired_date, coop_trip, passenger_count, contractor_id, carsharing_class, active, time_zone,
bonus_cost, ride_id, shared_ride_owner, status_comment, organization_id, cost_share_part, savings_cash, savings_procents,
phone_number, employee_device_time_zone, approval_deadline, approval_deadline_state, rating_mark, rating_comment,
joined_passenger_ids, source, min_tariff_taxi, comment_for_purpose, executor_group_id, executor_group_name, outcome_tariff_id, outcome_expected_cost)
VALUES('71bf3e90-0e53-4da4-b53e-50c68f77c639', 'RF-0001-00000945', '3cd35c19-fd39-413c-99a0-30f35bd642a8',
 '3cd35c19-fd39-413c-99a0-30f35bd642a8', '2025-08-01 14:28:25.726370', 'CARSHARING', '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70',
 '2025-08-01 15:43:25.726370', '4b6f382d-f6fa-4478-88d5-9c62b25fe16b',
 'CARSHARING_APPROVED', 0, 'APPROVED', 4600, 22.482, 1450000000000, '91bf3e90-0e53-4ea4-b53e-57c68f77c615',
 '[]', '2025-08-01 14:33:25.544414', false, 1, 'bc5f6b63-a646-4090-9644-2f2abc79d4d4', 'COMFORT', true, 'GMT+03', 0,
 'db6c813f-b0ed-4d79-9ea0-4bd1f86c1ed9', false, '', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 0, 0, 0, '+73123123123',
  'GMT+03', '2025-08-01 18:43:25.726370', 'NONE', 0, '', '[]', 'WEB', '{
    "cost": 4600,
    "tariffId": "4b6f382d-f6fa-4478-88d5-9c62b25fe16b"
  }', '', 'a64da45b-34a5-47ba-8820-dd2e9aee3ca2', 'СРБ/Московская область Восточное ГО/Пассажирские перевозки',
'4b6f382d-f6fa-4478-88d5-9c62b25fe16b', 4600);

INSERT INTO request.request_for_carsharing
(id, humanreadableid, author_id, passenger_id, creation_time, transport_type, approved_by_id, approval_date,
tariff_id, request_status, status_code, approval_state, expected_cost, expected_distance, expected_time, trip_purpose,
request_options, desired_date, coop_trip, passenger_count, contractor_id, carsharing_class, active, time_zone,
bonus_cost, ride_id, shared_ride_owner, status_comment, organization_id, cost_share_part, savings_cash, savings_procents,
phone_number, employee_device_time_zone, approval_deadline, approval_deadline_state, rating_mark, rating_comment,
joined_passenger_ids, source, min_tariff_taxi, comment_for_purpose, executor_group_id, executor_group_name, outcome_tariff_id, outcome_expected_cost)
VALUES('41bf3e90-0e53-4da4-b53e-50c68f77c639', 'RF-0001-00000946', '3cd35c19-fd39-413c-99a0-30f35bd642a8',
 '3cd35c19-fd39-413c-99a0-30f35bd642a8', '2025-08-01 14:28:25.726370', 'CARSHARING', '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70',
 '2025-08-01 15:43:25.726370', '4b6f382d-f6fa-4478-88d5-9c62b25fe16b',
 'CARSHARING_TRIP_FINISHED', 0, 'APPROVED', 4600, 22.482, 1450000000000, '91bf3e90-0e53-4ea4-b53e-57c68f77c615',
 '[]', '2025-08-01 14:33:25.544414', false, 1, 'bc5f6b63-a646-4090-9644-2f2abc79d4d4', 'COMFORT', true, 'GMT+03', 0,
 'db6c813f-b0ed-4d79-9ea0-4bd1f86c1ed9', false, '', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 0, 0, 0, '+73123123123',
  'GMT+03', '2025-08-01 18:43:25.726370', 'NONE', 0, '', '[]', 'WEB', '{
    "cost": 4600,
    "tariffId": "4b6f382d-f6fa-4478-88d5-9c62b25fe16b"
  }', '', 'a64da45b-34a5-47ba-8820-dd2e9aee3ca2', 'СРБ/Московская область Восточное ГО/Пассажирские перевозки',
'4b6f382d-f6fa-4478-88d5-9c62b25fe16b', 4600);

INSERT INTO request.carsharing_trip
(id, request_id, rent_id, car_model, car_number, start_point, rent_created_at, total_cost, driving_time, driving_time_cost,
 driving_length, driving_length_cost, parking_time, parking_time_cost, reserve_time, reserve_time_cost, finish_point, rent_finished_at,
 created, updated, start_address, finish_address)
VALUES('91bf3e90-0e53-4ea4-b53e-57c68f77c645', '71bf3e90-0e53-4da4-b53e-50c68f77c639', 2, 'Model', 'Л333ЛЛ33', '{"latitude": 55.706621, "longitude": 37.935297}',
'2025-08-01 14:28:25.726370', 4600, 30, 400, 22, 500, 10, 500, 10, 50, '{"latitude": 55.706629, "longitude": 37.935299}',
null, '2025-08-01 14:28:25.726370', null, '107045, г. Москва, ул. Ленина, д. 1', '125319, г. Москва, пр-т Мира, д. 100');

INSERT INTO request.carsharing_trip
(id, request_id, rent_id, car_model, car_number, start_point, rent_created_at, total_cost, driving_time, driving_time_cost,
 driving_length, driving_length_cost, parking_time, parking_time_cost, reserve_time, reserve_time_cost, finish_point, rent_finished_at,
 created, updated, start_address, finish_address)
VALUES('21bf3e90-0e53-4ea4-b53e-57c68f77c645', '41bf3e90-0e53-4da4-b53e-50c68f77c639', 3, 'Model', 'Л333ЛЛ33', '{"latitude": 55.706621, "longitude": 37.935297}',
'2025-08-01 14:28:25.726370', 4600, 30, 400, 22, 500, 10, 500, 10, 50, '{"latitude": 55.706629, "longitude": 37.935299}',
null, '2025-08-01 14:28:25.726370', null, '107045, г. Москва, ул. Ленина, д. 1', '125319, г. Москва, пр-т Мира, д. 100');
