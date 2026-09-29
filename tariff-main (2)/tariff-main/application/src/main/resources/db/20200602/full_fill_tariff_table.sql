INSERT INTO tariff.tariff (id, "name", region, price_per_mile, transport_type_id, price_details)
SELECT id,
       "name",
       region,
       price_per_mile,
       '7f18ce71-99a7-47b5-b285-335058c6715c',
       to_json('{' ||
               '"minutePrice": ' || tariff_taxi.minute_price || ',' ||
               '"waitingPrice": ' || tariff_taxi.waiting_price || ',' ||
               '"freeWaitingTime": ' || tariff_taxi.free_waiting_time || ',' ||
               '"submission": ' || tariff_taxi.submission_price || ',' ||
               '"minDistance": ' || tariff_taxi.min_mileage || ',' ||
               '"minTime": ' || tariff_taxi.min_time || ',' ||
               '"taxiClass": ' || tariff_taxi.taxi_class || ',' ||
               '}')
FROM tariff.tariff_taxi;

INSERT INTO tariff.tariff (id, "name", region, price_per_mile, transport_type_id, price_details)
SELECT id,
       "name",
       region,
       price_per_mile,
       '1a33601d-4db4-4720-8d09-95f015770fe0',
       to_json('{' ||
               '"rewardForPassenger": ' || tariff_personal.reward_for_passenger || ',' ||
               '"season": {' ||
               '"coefficient": ' || tariff_personal.season_coefficient || ',' ||
               '"start": "' || tariff_personal.season_start || '",' ||
               '"end": "' || tariff_personal.season_end || '",' ||
               '}"' ||
               '}')
FROM tariff.tariff_personal;