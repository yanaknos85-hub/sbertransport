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
VALUES ('4b6f382d-f6fa-4478-88d5-9c62b25fe16b', 'TF-0001-00000943', null, 'EMPLOYEE_TRANSPORTATION',
        '6e6e04b7-1912-422a-820a-1a6777dfde1b', 'c526065a-9d20-4889-9921-83adcb217b61', 'TAXI', true,
        '48cc2bc9-7865-4f78-a543-d751ace4ce98', 100, 100, 100, 100, 0, 0, null, null, null, 1, 1, 1, 1, 1, 0, 0, 0, 30,
        20, 20, 20, 1, 20, 100, 0, 1, 100, 0, 100, 100, null, null, null, 1, null, null,
        'bc5f6b63-a646-4090-9644-2f2abc79d4d4', null, 'COMFORT', 100, 0, 0, 1, 1, 1, 1,
        '000001_Тестовая_организация_01/перевозка_пассажиров/Москва и Московская область', 5600, false, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, 'Москва и Московская область', null, null);

insert into request.contractor_message (id, deleted, url, login, password)
values ('bc5f6b63-a646-4090-9644-2f2abc79d4d4', false, 'https://api.taxi-request.com', 'test', 'test');

INSERT INTO request.trip_purpose (id, purpose, active, organization, id_uuid)
VALUES (1300, 'Тестовый_департамент_01', true, '6e6e04b7-1912-422a-820a-1a6777dfde1b',
        '91bf3e90-0e53-4ea4-b53e-57c68f77c615');

INSERT INTO request.taxi_trip (trip_type, id, date_time_registered, organization_id, tariff_id, trip_finish_time,
                               trip_start_time, shared_request_id, request_id, status, taxi_id, trip_fact_distance,
                               trip_fact_duration, trip_fact_price, trip_fact_wait_time, trip_assignment_date_time,
                               active, human_readable_id, contractor_comment, resolution, decision_code, car_brand_name,
                               car_model, car_color, car_registration_number, time_work_start, time_work_finish,
                               last_known_position_id, last_xml_received_date_time, fact_parameters_setting_time,
                               ride_id, driver, outcome_tariff_id, registry_fact_waiting_time,
                               registry_human_readable_id, registry_fact_cost, registry_fact_distance,
                               registry_fact_payment)
VALUES ('COOP', '2a04c613-5b18-4267-9700-cfad231781a8', null, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6',
        '4b6f382d-f6fa-4478-88d5-9c62b25fe16b', '2025-08-01 14:54:36.583456', null, null, null, 'ORDER_FINISHED',
        'TP-0320-00000484', 0.513, null, null, 103620000000000, null, true, 'TT-OT-0001-00023473', null, null, null,
        'Автомобиль', 'Четвертый', null, 'А555РР666RUS', null, null, null, null, null,
        'db6c813f-b0ed-4d79-9ea0-4bd1f86c1ed9', '{"name":"Aleksandra","secName":"Neverina","phone":"+7 900 000 00 18"}',
        '4b6f382d-f6fa-4478-88d5-9c62b25fe16b', null, null, null, null, null);

INSERT INTO request.request_for_taxi (id, humanreadableid, author_id, passenger_id, creation_time, transport_type,
                                      approved_by_id, approval_date, segments, tariff_id, request_status, status_code,
                                      approval_state, expected_cost, expected_distance, expected_time, trip_purpose,
                                      request_options, desired_date, coop_trip, passenger_count, trip_class, comment,
                                      rating_advantages, rating_drawbacks, rating_mark, rating_comment, finished_time,
                                      magenta_order_id, shared_ride_id, contractor_id, driver_id, dispatcher_id,
                                      auto_cancel_deadline_min, sent_to_contractor, deadline_state, active,
                                      taxi_trip_id, resolution, time_zone, taxi_awaiting_search_start_date, vehicle_id,
                                      driver_assignment_deadline, trigger_time, bonus_cost, ride_id, shared_ride_owner,
                                      organization_id, bus_rent_duration, bus_count, fact_waiting_time, fact_distance,
                                      cost_share_part, savings_cash, savings_procents, driver_arrived_datetime,
                                      driver_arrived_deadline, employee_device_time_zone, number_passengers_joined,
                                      tariff, approval_deadline, approval_deadline_state, request_closed_datetime,
                                      joined_passenger_ids, source, min_tariff_taxi, comment_for_purpose,
                                      executor_group_id, executor_group_name, outcome_tariff_id, outcome_tariff,
                                      outcome_expected_cost)
VALUES ('7ffd0e5c-b2d9-4c5c-b206-804c34c87617', 'OT-0001-00023473', '3cd35c19-fd39-413c-99a0-30f35bd642a8',
        '3cd35c19-fd39-413c-99a0-30f35bd642a8', '2025-08-01 14:28:25.726370', 'TAXI',
        '3cd35c19-fd39-413c-99a0-30f35bd642a8', '2025-08-01 14:28:46.253957', '[
    {
      "cost": 0.0,
      "time": 42000,
      "distance": 0.15,
      "coordinates": [
        {
          "latitude": 55.706635,
          "longitude": 37.935319
        },
        {
          "latitude": 55.706621,
          "longitude": 37.935297
        },
        {
          "latitude": 55.706612,
          "longitude": 37.935282
        },
        {
          "latitude": 55.70657,
          "longitude": 37.935151
        },
        {
          "latitude": 55.706558,
          "longitude": 37.935087
        },
        {
          "latitude": 55.70656,
          "longitude": 37.934489
        },
        {
          "latitude": 55.70657,
          "longitude": 37.934432
        },
        {
          "latitude": 55.706591,
          "longitude": 37.9344
        },
        {
          "latitude": 55.706726,
          "longitude": 37.934264
        },
        {
          "latitude": 55.707045,
          "longitude": 37.933945
        },
        {
          "latitude": 55.707079,
          "longitude": 37.933911
        },
        {
          "latitude": 55.707139,
          "longitude": 37.933843
        },
        {
          "latitude": 55.707238,
          "longitude": 37.93373
        },
        {
          "latitude": 55.707298,
          "longitude": 37.933646
        }
      ]
    },
    {
      "cost": 0.0,
      "time": 4000,
      "distance": 0.016,
      "coordinates": [
        {
          "latitude": 55.707298,
          "longitude": 37.933646
        },
        {
          "latitude": 55.707213,
          "longitude": 37.933437
        }
      ]
    },
    {
      "cost": 0.0,
      "time": 23000,
      "distance": 0.106,
      "coordinates": [
        {
          "latitude": 55.707213,
          "longitude": 37.933437
        },
        {
          "latitude": 55.707339,
          "longitude": 37.933278
        },
        {
          "latitude": 55.707541,
          "longitude": 37.932991
        },
        {
          "latitude": 55.707733,
          "longitude": 37.932685
        },
        {
          "latitude": 55.707807,
          "longitude": 37.932554
        },
        {
          "latitude": 55.707917,
          "longitude": 37.93236
        },
        {
          "latitude": 55.707933,
          "longitude": 37.932327
        }
      ]
    },
    {
      "cost": 0.0,
      "time": 103000,
      "distance": 0.707,
      "coordinates": [
        {
          "latitude": 55.707933,
          "longitude": 37.932327
        },
        {
          "latitude": 55.707991,
          "longitude": 37.93242
        },
        {
          "latitude": 55.709813,
          "longitude": 37.935341
        },
        {
          "latitude": 55.710688,
          "longitude": 37.936746
        },
        {
          "latitude": 55.710837,
          "longitude": 37.936985
        },
        {
          "latitude": 55.710947,
          "longitude": 37.937163
        },
        {
          "latitude": 55.711058,
          "longitude": 37.93734
        },
        {
          "latitude": 55.711122,
          "longitude": 37.937442
        },
        {
          "latitude": 55.711157,
          "longitude": 37.937498
        },
        {
          "latitude": 55.711188,
          "longitude": 37.937548
        },
        {
          "latitude": 55.711198,
          "longitude": 37.937564
        },
        {
          "latitude": 55.711798,
          "longitude": 37.938527
        },
        {
          "latitude": 55.712682,
          "longitude": 37.939945
        }
      ]
    },
    {
      "cost": 0.0,
      "time": 744000,
      "distance": 17.166,
      "coordinates": [
        {
          "latitude": 55.712682,
          "longitude": 37.939945
        },
        {
          "latitude": 55.713275,
          "longitude": 37.940932
        },
        {
          "latitude": 55.713611,
          "longitude": 37.941311
        },
        {
          "latitude": 55.713767,
          "longitude": 37.941468
        },
        {
          "latitude": 55.714524,
          "longitude": 37.942117
        },
        {
          "latitude": 55.715035,
          "longitude": 37.942534
        },
        {
          "latitude": 55.7151,
          "longitude": 37.94259
        },
        {
          "latitude": 55.715144,
          "longitude": 37.942641
        },
        {
          "latitude": 55.715188,
          "longitude": 37.942705
        },
        {
          "latitude": 55.715217,
          "longitude": 37.942788
        },
        {
          "latitude": 55.71524,
          "longitude": 37.94287
        },
        {
          "latitude": 55.715246,
          "longitude": 37.942898
        },
        {
          "latitude": 55.715257,
          "longitude": 37.942955
        },
        {
          "latitude": 55.715269,
          "longitude": 37.943067
        },
        {
          "latitude": 55.715271,
          "longitude": 37.943188
        },
        {
          "latitude": 55.714355,
          "longitude": 37.947136
        },
        {
          "latitude": 55.714273,
          "longitude": 37.947555
        },
        {
          "latitude": 55.714171,
          "longitude": 37.948024
        },
        {
          "latitude": 55.713954,
          "longitude": 37.948757
        },
        {
          "latitude": 55.713923,
          "longitude": 37.948871
        },
        {
          "latitude": 55.7138,
          "longitude": 37.949302
        },
        {
          "latitude": 55.713402,
          "longitude": 37.950739
        },
        {
          "latitude": 55.712692,
          "longitude": 37.953403
        },
        {
          "latitude": 55.712284,
          "longitude": 37.954833
        },
        {
          "latitude": 55.712191,
          "longitude": 37.95518
        },
        {
          "latitude": 55.712101,
          "longitude": 37.955449
        },
        {
          "latitude": 55.711905,
          "longitude": 37.955981
        },
        {
          "latitude": 55.711689,
          "longitude": 37.956564
        },
        {
          "latitude": 55.711515,
          "longitude": 37.957114
        },
        {
          "latitude": 55.71135,
          "longitude": 37.957638
        },
        {
          "latitude": 55.711261,
          "longitude": 37.957898
        },
        {
          "latitude": 55.711161,
          "longitude": 37.95821
        },
        {
          "latitude": 55.711112,
          "longitude": 37.958364
        },
        {
          "latitude": 55.71103,
          "longitude": 37.958623
        },
        {
          "latitude": 55.710773,
          "longitude": 37.959449
        },
        {
          "latitude": 55.710246,
          "longitude": 37.961212
        },
        {
          "latitude": 55.710113,
          "longitude": 37.961794
        },
        {
          "latitude": 55.710002,
          "longitude": 37.962452
        },
        {
          "latitude": 55.709916,
          "longitude": 37.962962
        },
        {
          "latitude": 55.709855,
          "longitude": 37.963473
        },
        {
          "latitude": 55.709847,
          "longitude": 37.963552
        },
        {
          "latitude": 55.709812,
          "longitude": 37.96396
        },
        {
          "latitude": 55.709766,
          "longitude": 37.964556
        },
        {
          "latitude": 55.709766,
          "longitude": 37.964556
        },
        {
          "latitude": 55.709752,
          "longitude": 37.964825
        },
        {
          "latitude": 55.709748,
          "longitude": 37.965234
        },
        {
          "latitude": 55.709776,
          "longitude": 37.965674
        },
        {
          "latitude": 55.709848,
          "longitude": 37.966463
        },
        {
          "latitude": 55.710285,
          "longitude": 37.969971
        },
        {
          "latitude": 55.710285,
          "longitude": 37.969971
        },
        {
          "latitude": 55.710292,
          "longitude": 37.970027
        },
        {
          "latitude": 55.710771,
          "longitude": 37.973546
        },
        {
          "latitude": 55.711084,
          "longitude": 37.975988
        },
        {
          "latitude": 55.711208,
          "longitude": 37.977
        },
        {
          "latitude": 55.711271,
          "longitude": 37.977609
        },
        {
          "latitude": 55.711456,
          "longitude": 37.979497
        },
        {
          "latitude": 55.711469,
          "longitude": 37.97965
        },
        {
          "latitude": 55.711482,
          "longitude": 37.979839
        },
        {
          "latitude": 55.7116,
          "longitude": 37.981944
        },
        {
          "latitude": 55.711637,
          "longitude": 37.982896
        },
        {
          "latitude": 55.711653,
          "longitude": 37.985247
        },
        {
          "latitude": 55.711631,
          "longitude": 37.986149
        },
        {
          "latitude": 55.711599,
          "longitude": 37.986985
        },
        {
          "latitude": 55.711533,
          "longitude": 37.988042
        },
        {
          "latitude": 55.711464,
          "longitude": 37.989048
        },
        {
          "latitude": 55.711381,
          "longitude": 37.99009
        },
        {
          "latitude": 55.711284,
          "longitude": 37.991005
        },
        {
          "latitude": 55.711198,
          "longitude": 37.991906
        },
        {
          "latitude": 55.711039,
          "longitude": 37.992925
        },
        {
          "latitude": 55.710874,
          "longitude": 37.993823
        },
        {
          "latitude": 55.710629,
          "longitude": 37.994868
        },
        {
          "latitude": 55.710283,
          "longitude": 37.996055
        },
        {
          "latitude": 55.709798,
          "longitude": 37.997625
        },
        {
          "latitude": 55.709105,
          "longitude": 37.999683
        },
        {
          "latitude": 55.708348,
          "longitude": 38.001922
        },
        {
          "latitude": 55.707852,
          "longitude": 38.003436
        },
        {
          "latitude": 55.707851,
          "longitude": 38.003441
        },
        {
          "latitude": 55.70757,
          "longitude": 38.004406
        },
        {
          "latitude": 55.707569,
          "longitude": 38.004408
        },
        {
          "latitude": 55.707107,
          "longitude": 38.006084
        },
        {
          "latitude": 55.707104,
          "longitude": 38.006099
        },
        {
          "latitude": 55.70684,
          "longitude": 38.00755
        },
        {
          "latitude": 55.706839,
          "longitude": 38.007559
        },
        {
          "latitude": 55.706646,
          "longitude": 38.009011
        },
        {
          "latitude": 55.706645,
          "longitude": 38.009016
        },
        {
          "latitude": 55.706486,
          "longitude": 38.010497
        },
        {
          "latitude": 55.706485,
          "longitude": 38.010502
        },
        {
          "latitude": 55.706381,
          "longitude": 38.011779
        },
        {
          "latitude": 55.70638,
          "longitude": 38.011789
        },
        {
          "latitude": 55.706353,
          "longitude": 38.012871
        },
        {
          "latitude": 55.70638,
          "longitude": 38.014245
        },
        {
          "latitude": 55.706397,
          "longitude": 38.015102
        },
        {
          "latitude": 55.706459,
          "longitude": 38.016112
        },
        {
          "latitude": 55.70655,
          "longitude": 38.017039
        },
        {
          "latitude": 55.706674,
          "longitude": 38.018022
        },
        {
          "latitude": 55.706675,
          "longitude": 38.01803
        },
        {
          "latitude": 55.706675,
          "longitude": 38.01803
        },
        {
          "latitude": 55.706778,
          "longitude": 38.018637
        },
        {
          "latitude": 55.706778,
          "longitude": 38.018637
        },
        {
          "latitude": 55.706905,
          "longitude": 38.019381
        },
        {
          "latitude": 55.707147,
          "longitude": 38.020431
        },
        {
          "latitude": 55.707217,
          "longitude": 38.020735
        },
        {
          "latitude": 55.707219,
          "longitude": 38.020741
        },
        {
          "latitude": 55.707678,
          "longitude": 38.022399
        },
        {
          "latitude": 55.709069,
          "longitude": 38.027195
        },
        {
          "latitude": 55.709302,
          "longitude": 38.028123
        },
        {
          "latitude": 55.709519,
          "longitude": 38.029062
        },
        {
          "latitude": 55.709651,
          "longitude": 38.029793
        },
        {
          "latitude": 55.709741,
          "longitude": 38.030518
        },
        {
          "latitude": 55.709842,
          "longitude": 38.031309
        },
        {
          "latitude": 55.709921,
          "longitude": 38.03212
        },
        {
          "latitude": 55.709975,
          "longitude": 38.032801
        },
        {
          "latitude": 55.710005,
          "longitude": 38.033482
        },
        {
          "latitude": 55.710018,
          "longitude": 38.034325
        },
        {
          "latitude": 55.710011,
          "longitude": 38.038411
        },
        {
          "latitude": 55.710036,
          "longitude": 38.039148
        },
        {
          "latitude": 55.710078,
          "longitude": 38.039766
        },
        {
          "latitude": 55.710165,
          "longitude": 38.040319
        },
        {
          "latitude": 55.710406,
          "longitude": 38.041446
        },
        {
          "latitude": 55.711309,
          "longitude": 38.04566
        },
        {
          "latitude": 55.711458,
          "longitude": 38.046181
        },
        {
          "latitude": 55.711539,
          "longitude": 38.046422
        },
        {
          "latitude": 55.711539,
          "longitude": 38.046422
        },
        {
          "latitude": 55.711572,
          "longitude": 38.04652
        },
        {
          "latitude": 55.711799,
          "longitude": 38.047161
        },
        {
          "latitude": 55.711858,
          "longitude": 38.047312
        },
        {
          "latitude": 55.711858,
          "longitude": 38.047312
        },
        {
          "latitude": 55.713424,
          "longitude": 38.051352
        },
        {
          "latitude": 55.713614,
          "longitude": 38.05189
        },
        {
          "latitude": 55.71369,
          "longitude": 38.052123
        },
        {
          "latitude": 55.713779,
          "longitude": 38.052392
        },
        {
          "latitude": 55.713923,
          "longitude": 38.052852
        },
        {
          "latitude": 55.714068,
          "longitude": 38.053334
        },
        {
          "latitude": 55.714197,
          "longitude": 38.053753
        },
        {
          "latitude": 55.714301,
          "longitude": 38.054193
        },
        {
          "latitude": 55.714401,
          "longitude": 38.054652
        },
        {
          "latitude": 55.714491,
          "longitude": 38.05506
        },
        {
          "latitude": 55.714593,
          "longitude": 38.055529
        },
        {
          "latitude": 55.714686,
          "longitude": 38.05604
        },
        {
          "latitude": 55.715437,
          "longitude": 38.060397
        },
        {
          "latitude": 55.71551,
          "longitude": 38.060929
        },
        {
          "latitude": 55.715567,
          "longitude": 38.061384
        },
        {
          "latitude": 55.715954,
          "longitude": 38.064657
        },
        {
          "latitude": 55.715983,
          "longitude": 38.065053
        },
        {
          "latitude": 55.716013,
          "longitude": 38.065582
        },
        {
          "latitude": 55.716049,
          "longitude": 38.066183
        },
        {
          "latitude": 55.71608,
          "longitude": 38.066703
        },
        {
          "latitude": 55.7161,
          "longitude": 38.067288
        },
        {
          "latitude": 55.716145,
          "longitude": 38.069774
        },
        {
          "latitude": 55.716146,
          "longitude": 38.074107
        },
        {
          "latitude": 55.716091,
          "longitude": 38.075974
        },
        {
          "latitude": 55.716013,
          "longitude": 38.077766
        },
        {
          "latitude": 55.715019,
          "longitude": 38.099962
        },
        {
          "latitude": 55.714939,
          "longitude": 38.101732
        },
        {
          "latitude": 55.714939,
          "longitude": 38.101765
        },
        {
          "latitude": 55.714933,
          "longitude": 38.102384
        },
        {
          "latitude": 55.71494,
          "longitude": 38.102675
        },
        {
          "latitude": 55.71494,
          "longitude": 38.102675
        },
        {
          "latitude": 55.714952,
          "longitude": 38.103142
        },
        {
          "latitude": 55.714991,
          "longitude": 38.104301
        },
        {
          "latitude": 55.715046,
          "longitude": 38.105928
        },
        {
          "latitude": 55.715107,
          "longitude": 38.107033
        },
        {
          "latitude": 55.715173,
          "longitude": 38.108012
        },
        {
          "latitude": 55.71527,
          "longitude": 38.109152
        },
        {
          "latitude": 55.715356,
          "longitude": 38.110169
        },
        {
          "latitude": 55.715357,
          "longitude": 38.110174
        },
        {
          "latitude": 55.715538,
          "longitude": 38.111842
        },
        {
          "latitude": 55.715539,
          "longitude": 38.111846
        },
        {
          "latitude": 55.715687,
          "longitude": 38.113023
        },
        {
          "latitude": 55.715908,
          "longitude": 38.1147
        },
        {
          "latitude": 55.715909,
          "longitude": 38.114703
        },
        {
          "latitude": 55.716136,
          "longitude": 38.116193
        },
        {
          "latitude": 55.716342,
          "longitude": 38.117376
        },
        {
          "latitude": 55.716343,
          "longitude": 38.11738
        },
        {
          "latitude": 55.716973,
          "longitude": 38.12054
        },
        {
          "latitude": 55.716974,
          "longitude": 38.120545
        },
        {
          "latitude": 55.717157,
          "longitude": 38.121352
        },
        {
          "latitude": 55.717159,
          "longitude": 38.12136
        },
        {
          "latitude": 55.717316,
          "longitude": 38.121933
        },
        {
          "latitude": 55.717317,
          "longitude": 38.121939
        },
        {
          "latitude": 55.717487,
          "longitude": 38.12247
        },
        {
          "latitude": 55.717648,
          "longitude": 38.122961
        },
        {
          "latitude": 55.717831,
          "longitude": 38.123679
        },
        {
          "latitude": 55.71803,
          "longitude": 38.124622
        },
        {
          "latitude": 55.71821,
          "longitude": 38.125612
        },
        {
          "latitude": 55.71831,
          "longitude": 38.126298
        },
        {
          "latitude": 55.718382,
          "longitude": 38.127015
        },
        {
          "latitude": 55.71842,
          "longitude": 38.127557
        },
        {
          "latitude": 55.718421,
          "longitude": 38.12756
        },
        {
          "latitude": 55.718532,
          "longitude": 38.128814
        },
        {
          "latitude": 55.718633,
          "longitude": 38.129748
        },
        {
          "latitude": 55.718822,
          "longitude": 38.131398
        },
        {
          "latitude": 55.718975,
          "longitude": 38.132605
        },
        {
          "latitude": 55.718977,
          "longitude": 38.132614
        },
        {
          "latitude": 55.719096,
          "longitude": 38.133269
        },
        {
          "latitude": 55.719097,
          "longitude": 38.133273
        },
        {
          "latitude": 55.7193,
          "longitude": 38.13423
        },
        {
          "latitude": 55.719301,
          "longitude": 38.134233
        },
        {
          "latitude": 55.719504,
          "longitude": 38.135125
        },
        {
          "latitude": 55.719769,
          "longitude": 38.136193
        },
        {
          "latitude": 55.719771,
          "longitude": 38.1362
        },
        {
          "latitude": 55.720024,
          "longitude": 38.137047
        },
        {
          "latitude": 55.720026,
          "longitude": 38.137052
        },
        {
          "latitude": 55.720323,
          "longitude": 38.137919
        },
        {
          "latitude": 55.720324,
          "longitude": 38.137922
        },
        {
          "latitude": 55.720741,
          "longitude": 38.139057
        },
        {
          "latitude": 55.720742,
          "longitude": 38.139061
        },
        {
          "latitude": 55.721042,
          "longitude": 38.139825
        },
        {
          "latitude": 55.721043,
          "longitude": 38.139827
        },
        {
          "latitude": 55.721507,
          "longitude": 38.140931
        },
        {
          "latitude": 55.721511,
          "longitude": 38.14094
        },
        {
          "latitude": 55.722442,
          "longitude": 38.142737
        },
        {
          "latitude": 55.722445,
          "longitude": 38.142742
        },
        {
          "latitude": 55.723216,
          "longitude": 38.14404
        },
        {
          "latitude": 55.72404,
          "longitude": 38.145545
        },
        {
          "latitude": 55.724358,
          "longitude": 38.146171
        },
        {
          "latitude": 55.724609,
          "longitude": 38.146699
        },
        {
          "latitude": 55.724962,
          "longitude": 38.147546
        },
        {
          "latitude": 55.725322,
          "longitude": 38.148556
        },
        {
          "latitude": 55.725629,
          "longitude": 38.149484
        },
        {
          "latitude": 55.725925,
          "longitude": 38.150412
        },
        {
          "latitude": 55.72624,
          "longitude": 38.151494
        },
        {
          "latitude": 55.72646,
          "longitude": 38.15228
        },
        {
          "latitude": 55.726661,
          "longitude": 38.153092
        },
        {
          "latitude": 55.726817,
          "longitude": 38.153781
        },
        {
          "latitude": 55.72694,
          "longitude": 38.154357
        },
        {
          "latitude": 55.727565,
          "longitude": 38.157294
        },
        {
          "latitude": 55.727675,
          "longitude": 38.157808
        },
        {
          "latitude": 55.727987,
          "longitude": 38.159227
        },
        {
          "latitude": 55.728126,
          "longitude": 38.159863
        },
        {
          "latitude": 55.728376,
          "longitude": 38.161001
        },
        {
          "latitude": 55.728562,
          "longitude": 38.161857
        },
        {
          "latitude": 55.730557,
          "longitude": 38.171063
        },
        {
          "latitude": 55.730558,
          "longitude": 38.171067
        },
        {
          "latitude": 55.730773,
          "longitude": 38.171952
        },
        {
          "latitude": 55.730774,
          "longitude": 38.171955
        },
        {
          "latitude": 55.731056,
          "longitude": 38.173021
        },
        {
          "latitude": 55.731057,
          "longitude": 38.173023
        },
        {
          "latitude": 55.731416,
          "longitude": 38.174319
        },
        {
          "latitude": 55.731418,
          "longitude": 38.174323
        },
        {
          "latitude": 55.73285,
          "longitude": 38.178921
        },
        {
          "latitude": 55.733154,
          "longitude": 38.179859
        },
        {
          "latitude": 55.733169,
          "longitude": 38.179902
        },
        {
          "latitude": 55.733575,
          "longitude": 38.181188
        },
        {
          "latitude": 55.733892,
          "longitude": 38.182154
        },
        {
          "latitude": 55.733892,
          "longitude": 38.182154
        },
        {
          "latitude": 55.735711,
          "longitude": 38.187963
        },
        {
          "latitude": 55.735711,
          "longitude": 38.187963
        },
        {
          "latitude": 55.735852,
          "longitude": 38.188393
        },
        {
          "latitude": 55.735956,
          "longitude": 38.188726
        },
        {
          "latitude": 55.737237,
          "longitude": 38.192868
        }
      ]
    },
    {
      "cost": 0.0,
      "time": 43000,
      "distance": 0.64,
      "coordinates": [
        {
          "latitude": 55.737237,
          "longitude": 38.192868
        },
        {
          "latitude": 55.737374,
          "longitude": 38.193486
        },
        {
          "latitude": 55.737506,
          "longitude": 38.194043
        },
        {
          "latitude": 55.738261,
          "longitude": 38.197453
        },
        {
          "latitude": 55.739327,
          "longitude": 38.200867
        },
        {
          "latitude": 55.739771,
          "longitude": 38.201993
        }
      ]
    },
    {
      "cost": 0.0,
      "time": 96000,
      "distance": 1.323,
      "coordinates": [
        {
          "latitude": 55.739771,
          "longitude": 38.201993
        },
        {
          "latitude": 55.739832,
          "longitude": 38.202147
        },
        {
          "latitude": 55.739907,
          "longitude": 38.202272
        },
        {
          "latitude": 55.739992,
          "longitude": 38.202357
        },
        {
          "latitude": 55.740068,
          "longitude": 38.202417
        },
        {
          "latitude": 55.740165,
          "longitude": 38.202442
        },
        {
          "latitude": 55.740278,
          "longitude": 38.202419
        },
        {
          "latitude": 55.740667,
          "longitude": 38.202287
        },
        {
          "latitude": 55.74074,
          "longitude": 38.20222
        },
        {
          "latitude": 55.740771,
          "longitude": 38.202151
        },
        {
          "latitude": 55.7408,
          "longitude": 38.202032
        },
        {
          "latitude": 55.740809,
          "longitude": 38.20193
        },
        {
          "latitude": 55.7408,
          "longitude": 38.201744
        },
        {
          "latitude": 55.73941,
          "longitude": 38.197239
        },
        {
          "latitude": 55.739366,
          "longitude": 38.197096
        },
        {
          "latitude": 55.738099,
          "longitude": 38.193609
        },
        {
          "latitude": 55.736451,
          "longitude": 38.188268
        },
        {
          "latitude": 55.735483,
          "longitude": 38.185646
        }
      ]
    },
    {
      "cost": 0.0,
      "time": 53000,
      "distance": 0.478,
      "coordinates": [
        {
          "latitude": 55.735483,
          "longitude": 38.185646
        },
        {
          "latitude": 55.735398,
          "longitude": 38.185495
        },
        {
          "latitude": 55.735384,
          "longitude": 38.185482
        },
        {
          "latitude": 55.735359,
          "longitude": 38.185468
        },
        {
          "latitude": 55.735342,
          "longitude": 38.185463
        },
        {
          "latitude": 55.735302,
          "longitude": 38.185482
        },
        {
          "latitude": 55.735042,
          "longitude": 38.186378
        },
        {
          "latitude": 55.734941,
          "longitude": 38.18656
        },
        {
          "latitude": 55.734401,
          "longitude": 38.188597
        },
        {
          "latitude": 55.734224,
          "longitude": 38.189414
        },
        {
          "latitude": 55.734172,
          "longitude": 38.18959
        },
        {
          "latitude": 55.733846,
          "longitude": 38.190755
        },
        {
          "latitude": 55.733538,
          "longitude": 38.191854
        },
        {
          "latitude": 55.733499,
          "longitude": 38.191993
        }
      ]
    },
    {
      "cost": 0.0,
      "time": 58000,
      "distance": 0.28,
      "coordinates": [
        {
          "latitude": 55.733499,
          "longitude": 38.191993
        },
        {
          "latitude": 55.732698,
          "longitude": 38.191479
        },
        {
          "latitude": 55.732484,
          "longitude": 38.191337
        },
        {
          "latitude": 55.731696,
          "longitude": 38.190586
        },
        {
          "latitude": 55.731569,
          "longitude": 38.190456
        },
        {
          "latitude": 55.731383,
          "longitude": 38.190257
        },
        {
          "latitude": 55.731322,
          "longitude": 38.190207
        },
        {
          "latitude": 55.731263,
          "longitude": 38.190177
        },
        {
          "latitude": 55.731202,
          "longitude": 38.190169
        }
      ]
    },
    {
      "cost": 0.0,
      "time": 109000,
      "distance": 0.838,
      "coordinates": [
        {
          "latitude": 55.731202,
          "longitude": 38.190169
        },
        {
          "latitude": 55.731154,
          "longitude": 38.190184
        },
        {
          "latitude": 55.731103,
          "longitude": 38.190218
        },
        {
          "latitude": 55.731056,
          "longitude": 38.190277
        },
        {
          "latitude": 55.731007,
          "longitude": 38.190353
        },
        {
          "latitude": 55.730954,
          "longitude": 38.19047
        },
        {
          "latitude": 55.730678,
          "longitude": 38.191205
        },
        {
          "latitude": 55.729673,
          "longitude": 38.193889
        },
        {
          "latitude": 55.729585,
          "longitude": 38.194123
        },
        {
          "latitude": 55.729268,
          "longitude": 38.194969
        },
        {
          "latitude": 55.729221,
          "longitude": 38.195122
        },
        {
          "latitude": 55.729184,
          "longitude": 38.19527
        },
        {
          "latitude": 55.729153,
          "longitude": 38.195429
        },
        {
          "latitude": 55.729127,
          "longitude": 38.195608
        },
        {
          "latitude": 55.729112,
          "longitude": 38.195735
        },
        {
          "latitude": 55.729108,
          "longitude": 38.195764
        },
        {
          "latitude": 55.729102,
          "longitude": 38.195818
        },
        {
          "latitude": 55.729083,
          "longitude": 38.196018
        },
        {
          "latitude": 55.729072,
          "longitude": 38.196219
        },
        {
          "latitude": 55.729069,
          "longitude": 38.196409
        },
        {
          "latitude": 55.72907,
          "longitude": 38.196842
        },
        {
          "latitude": 55.729072,
          "longitude": 38.197168
        },
        {
          "latitude": 55.729076,
          "longitude": 38.197623
        },
        {
          "latitude": 55.729086,
          "longitude": 38.198539
        },
        {
          "latitude": 55.729109,
          "longitude": 38.200852
        },
        {
          "latitude": 55.729115,
          "longitude": 38.20106
        },
        {
          "latitude": 55.729116,
          "longitude": 38.201114
        },
        {
          "latitude": 55.729131,
          "longitude": 38.201339
        },
        {
          "latitude": 55.729153,
          "longitude": 38.201542
        },
        {
          "latitude": 55.729178,
          "longitude": 38.201683
        },
        {
          "latitude": 55.729192,
          "longitude": 38.201743
        },
        {
          "latitude": 55.729205,
          "longitude": 38.201803
        },
        {
          "latitude": 55.729244,
          "longitude": 38.201928
        },
        {
          "latitude": 55.729276,
          "longitude": 38.202039
        },
        {
          "latitude": 55.729303,
          "longitude": 38.202166
        },
        {
          "latitude": 55.729325,
          "longitude": 38.202326
        }
      ]
    },
    {
      "cost": 0.0,
      "time": 1000,
      "distance": 0.005,
      "coordinates": [
        {
          "latitude": 55.729325,
          "longitude": 38.202326
        },
        {
          "latitude": 55.729302,
          "longitude": 38.202407
        }
      ]
    },
    {
      "cost": 0.0,
      "time": 53000,
      "distance": 0.303,
      "coordinates": [
        {
          "latitude": 55.729302,
          "longitude": 38.202407
        },
        {
          "latitude": 55.729113,
          "longitude": 38.202404
        },
        {
          "latitude": 55.728925,
          "longitude": 38.202401
        },
        {
          "latitude": 55.72868,
          "longitude": 38.202398
        },
        {
          "latitude": 55.728073,
          "longitude": 38.202389
        },
        {
          "latitude": 55.727985,
          "longitude": 38.202387
        },
        {
          "latitude": 55.7279,
          "longitude": 38.202386
        },
        {
          "latitude": 55.727867,
          "longitude": 38.202389
        },
        {
          "latitude": 55.72757,
          "longitude": 38.202421
        },
        {
          "latitude": 55.727414,
          "longitude": 38.202438
        },
        {
          "latitude": 55.726542,
          "longitude": 38.202531
        }
      ]
    },
    {
      "cost": 0.0,
      "time": 63000,
      "distance": 0.262,
      "coordinates": [
        {
          "latitude": 55.726542,
          "longitude": 38.202531
        },
        {
          "latitude": 55.726427,
          "longitude": 38.20221
        },
        {
          "latitude": 55.726222,
          "longitude": 38.201636
        },
        {
          "latitude": 55.72594,
          "longitude": 38.200841
        },
        {
          "latitude": 55.72569,
          "longitude": 38.200141
        },
        {
          "latitude": 55.725652,
          "longitude": 38.200033
        },
        {
          "latitude": 55.725595,
          "longitude": 38.199908
        },
        {
          "latitude": 55.725523,
          "longitude": 38.199801
        },
        {
          "latitude": 55.725128,
          "longitude": 38.199208
        }
      ]
    },
    {
      "cost": 0.0,
      "time": 41000,
      "distance": 0.148,
      "coordinates": [
        {
          "latitude": 55.725128,
          "longitude": 38.199208
        },
        {
          "latitude": 55.725131,
          "longitude": 38.199191
        },
        {
          "latitude": 55.725083,
          "longitude": 38.198558
        },
        {
          "latitude": 55.725079,
          "longitude": 38.198525
        },
        {
          "latitude": 55.725081,
          "longitude": 38.19851
        },
        {
          "latitude": 55.72507,
          "longitude": 38.19841
        },
        {
          "latitude": 55.725031,
          "longitude": 38.198271
        },
        {
          "latitude": 55.724926,
          "longitude": 38.197936
        },
        {
          "latitude": 55.724655,
          "longitude": 38.197029
        }
      ]
    },
    {
      "cost": 0.0,
      "time": 17000,
      "distance": 0.06,
      "coordinates": [
        {
          "latitude": 55.724655,
          "longitude": 38.197029
        },
        {
          "latitude": 55.724196,
          "longitude": 38.197542
        }
      ]
    },
    {
      "cost": 0.0,
      "time": 0,
      "distance": 0.0,
      "coordinates": []
    }
  ]', '4b6f382d-f6fa-4478-88d5-9c62b25fe16b', 'TAXI_DRIVER_FOUND', 0, 'APPROVED', 4600, 22.482, 1450000000000,
        '91bf3e90-0e53-4ea4-b53e-57c68f77c615', '[]', '2025-08-01 14:33:25.544414', true, 1, 'COMFORT', null, null,
        null, null, null, null, null, null, 'bc5f6b63-a646-4090-9644-2f2abc79d4d4',
        'caebcc21-bc0e-4931-a036-257b997b13e6', null, 720, true, 'NONE', true, '2a04c613-5b18-4267-9700-cfad231781a8',
        null, 'GMT+03:00', '2025-08-01 14:31:02.740194', null, null, 5600, null, 'db6c813f-b0ed-4d79-9ea0-4bd1f86c1ed9',
        true, 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', null, null, null, null, 1, 0, 0, null,
        '2025-08-01 15:43:46.253957', 'GMT+03', null, '{
    "id": "4b6f382d-f6fa-4478-88d5-9c62b25fe16b",
    "active": true,
    "region": "Москва и Московская область",
    "coefOrg": 1.0,
    "regionId": "c526065a-9d20-4889-9921-83adcb217b61",
    "taxiClass": "COMFORT",
    "workGroup": "000001_Тестовая_организация_01/перевозка_пассажиров/Москва и Московская область",
    "contractId": "48cc2bc9-7865-4f78-a543-d751ace4ce98",
    "coefBicycle": 1.0,
    "coefTraffic": 1.0,
    "serviceType": "EMPLOYEE_TRANSPORTATION",
    "triggerTime": 5600,
    "contractorId": "bc5f6b63-a646-4090-9644-2f2abc79d4d4",
    "departmentId": null,
    "timeIncluded": 0,
    "coefChildSeat": 1.0,
    "isNightTariff": false,
    "rideCostPerKm": 100,
    "transportType": [
      "TransportTypeEnum",
      "TAXI"
    ],
    "carServiceCost": 0,
    "organizationId": "6e6e04b7-1912-422a-820a-1a6777dfde1b",
    "rideCostPerMin": 100,
    "transport_type": "TAXI",
    "waitCostPerMin": 100,
    "freeWaitingTime": 0,
    "humanReadableId": "TF-0001-00000943",
    "minRideTimeCost": 0,
    "coefPetTransport": 1.0,
    "coopTariffParams": {
      "minCancelTimeMin": 30,
      "timeDeviationMin": 0,
      "distanceDeviationKm": 0.0,
      "savingsDeviationPct": 0.0
    },
    "distanceIncluded": 1.0,
    "timedTariffParams": {
      "coefDayOff": 1.0,
      "coefWorkDayNoon": 1.0,
      "coefWorkDayNight": 1.0,
      "coefWorkDayEvening": 1.0,
      "coefWorkDayMorning": 1.0
    },
    "contractorTariffId": null,
    "suburbTariffParams": {
      "costPerKmSuburb": 100,
      "costPerMinSuburb": 100,
      "costPerKmInterRegion": 0,
      "costPerMinInterRegion": 0,
      "suburbServiceCostPerKm": 100,
      "suburbServiceCostPerMin": 100
    },
    "minRideDistanceCost": 100,
    "contractorDeviationParams": {
      "maxDiffComputedCostPercent": 20,
      "maxDiffFactDistancePercent": 20,
      "maxDiffContractorCostPercent": 1,
      "maxDiffComputedWaitingPercent": 20,
      "maxDiffComputedDistancePercent": 20
    },
    "waitCostPerMinIntermediate": 100
  }', '2025-08-01 14:43:25.726370', 'NONE', null, '[]', 'WEB', '{
    "cost": 4600,
    "tariffId": "4b6f382d-f6fa-4478-88d5-9c62b25fe16b"
  }', null, 'a64da45b-34a5-47ba-8820-dd2e9aee3ca2', 'СРБ/Московская область Восточное ГО/Пассажирские перевозки',
        '4b6f382d-f6fa-4478-88d5-9c62b25fe16b', '{
    "id": "4b6f382d-f6fa-4478-88d5-9c62b25fe16b",
    "active": true,
    "region": "Москва и Московская область",
    "coefOrg": 1.0,
    "regionId": "c526065a-9d20-4889-9921-83adcb217b61",
    "taxiClass": "COMFORT",
    "workGroup": "000001_Тестовая_организация_01/перевозка_пассажиров/Москва и Московская область",
    "contractId": "48cc2bc9-7865-4f78-a543-d751ace4ce98",
    "coefBicycle": 1.0,
    "coefTraffic": 1.0,
    "serviceType": "EMPLOYEE_TRANSPORTATION",
    "triggerTime": 5600,
    "contractorId": "bc5f6b63-a646-4090-9644-2f2abc79d4d4",
    "departmentId": null,
    "timeIncluded": 0,
    "coefChildSeat": 1.0,
    "isNightTariff": false,
    "rideCostPerKm": 100,
    "transportType": [
      "TransportTypeEnum",
      "TAXI"
    ],
    "carServiceCost": 0,
    "organizationId": "6e6e04b7-1912-422a-820a-1a6777dfde1b",
    "rideCostPerMin": 100,
    "transport_type": "TAXI",
    "waitCostPerMin": 100,
    "freeWaitingTime": 0,
    "humanReadableId": "TF-0001-00000943",
    "minRideTimeCost": 0,
    "coefPetTransport": 1.0,
    "coopTariffParams": {
      "minCancelTimeMin": 30,
      "timeDeviationMin": 0,
      "distanceDeviationKm": 0.0,
      "savingsDeviationPct": 0.0
    },
    "distanceIncluded": 1.0,
    "timedTariffParams": {
      "coefDayOff": 1.0,
      "coefWorkDayNoon": 1.0,
      "coefWorkDayNight": 1.0,
      "coefWorkDayEvening": 1.0,
      "coefWorkDayMorning": 1.0
    },
    "contractorTariffId": null,
    "suburbTariffParams": {
      "costPerKmSuburb": 100,
      "costPerMinSuburb": 100,
      "costPerKmInterRegion": 0,
      "costPerMinInterRegion": 0,
      "suburbServiceCostPerKm": 100,
      "suburbServiceCostPerMin": 100
    },
    "minRideDistanceCost": 100,
    "contractorDeviationParams": {
      "maxDiffComputedCostPercent": 20,
      "maxDiffFactDistancePercent": 20,
      "maxDiffContractorCostPercent": 1,
      "maxDiffComputedWaitingPercent": 20,
      "maxDiffComputedDistancePercent": 20
    },
    "waitCostPerMinIntermediate": 100
  }', 4600);
