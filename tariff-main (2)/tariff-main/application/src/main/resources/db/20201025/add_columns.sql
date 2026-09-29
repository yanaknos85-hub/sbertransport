alter table tariff.tariff
    rename column coefficient to seasonal_coefficient;
alter table tariff.tariff
    add column min_ride_distance_cost         int4   not null default 0,
    add column distance_included              int4   not null default 0,
    add column min_ride_time_cost             int4   not null default 0,
    add column minutes_included               int4   not null default 0,
    add column coef_bicycle                   float8 default 1,
    add column coef_child_seat                float8 default 1,
    add column coef_traffic                   float8 default 0,
    add column coef_work_day_morning          float8 default 1,
    add column coef_work_day_noon             float8 default 1,
    add column coef_work_day_evening          float8 default 1,
    add column coef_work_day_night            float8 default 1,
    add column coef_day_off                   float8 default 1,
    add column coef_org                       float8 default 1,
    add column coef_personal_discount         float8 default 1,
    add column coef_pet_transport             float8 default 1,
    add column suburb_service_cost_per_km     int4   default 0,
    add column suburb_service_cost_per_min    int4   default 0,
    add column cost_per_min_inter_region      int4   default 0,
    add column cost_per_km_inter_region       int4   default 0,
    add column cost_per_min_suburb            int4   default 0,
    add column cost_per_km_suburb             int4   default 0,
    add column wait_cost_per_min_intermediate int4   default 0,
    add column contractor_id                  uuid,
    add column coef_insurance                 float8 default 1,
    add column coef_material_assets           float8 default 1,
    add column coef_casco                     float8 default 1,
    add column coef_engine_1_6                float8 default 1,
    add column coef_engine_1_6_to_2_0         float8 default 1,
    add column coef_engine_2_0_to_2_5         float8 default 1,
    add column booking_cost                   int4   default 0;
comment on column tariff.tariff.min_ride_distance_cost is 'Стоимость минимальной поездки с включенным расстоянием, коп';
comment on column tariff.tariff.distance_included is 'Бесплатных километров пути, включенных в тариф';
comment on column tariff.tariff.min_ride_time_cost is 'Стоимость минимальной поездки с включенным временем, коп';
comment on column tariff.tariff.minutes_included is 'Бесплатных минут пути, включенных в тариф';
comment on column tariff.tariff.coef_bicycle is 'Кэф доплаты за лыжи/сноуборд/велосипед';
comment on column tariff.tariff.coef_child_seat is 'Коэффициент доплаты за детское кресло';
comment on column tariff.tariff.coef_org is 'Коэффициент организации';
comment on column tariff.tariff.coef_personal_discount is 'Скидочный коэффициент при использовании в личных целях';
comment on column tariff.tariff.coef_pet_transport is 'Коэффициент доплаты за перевозку животного';
comment on column tariff.tariff.coef_traffic is 'Коэффициент загрузки дорог (пробок, баллы Яндекс, прогнозные) Более 7 баллов';
comment on column tariff.tariff.coef_work_day_morning is 'Коэффициент временного интервала поездки: утро будние дни 07:00-10:00';
comment on column tariff.tariff.coef_work_day_noon is 'Коэффициент временного интервала поездки: день будние дни 10:00-18:00';
comment on column tariff.tariff.coef_work_day_evening is 'Коэффициент временного интервала поездки: вечерний будние дни 18:00-22:00';
comment on column tariff.tariff.coef_work_day_night is 'Коэффициент временного интервала поездки: ночной будние дни 22:00-07:00';
comment on column tariff.tariff.coef_day_off is 'Коэффициент выходного дня: СБ, ВСКР';
comment on column tariff.tariff.suburb_service_cost_per_km is 'Стоимость 1 км платной подачи за чертой города, коп.';
comment on column tariff.tariff.suburb_service_cost_per_min is 'Стоимость 1 минуты платной подачи за чертой города, коп.';
comment on column tariff.tariff.cost_per_min_inter_region is 'Стоимость 1 минуты межрегиональной поездки, коп';
comment on column tariff.tariff.cost_per_km_inter_region is 'Стоимость пробега 1 км межрегиональной поездки, коп.';
comment on column tariff.tariff.cost_per_min_suburb is 'Цена за км  за чертой города, коп';
comment on column tariff.tariff.cost_per_km_suburb is 'Стоимость за минуту  за чертой города, коп';
comment on column tariff.tariff.wait_cost_per_min_intermediate is 'Стоимость за минуту ожидания в промежуточной точке, коп.';
comment on column tariff.tariff.contractor_id is 'Контрагент';
comment on column tariff.tariff.coef_insurance is 'Коэффициент на страхование';
comment on column tariff.tariff.coef_casco is 'Коэффициент на полное покрытие ответственности КАСКО';
comment on column tariff.tariff.coef_engine_1_6 is 'К объема двигателя для ТС до 1,6 л. ( только для бензиновых двигателей)';
comment on column tariff.tariff.coef_engine_1_6_to_2_0 is 'К объема двигателя для ТС от 1,7 до 2,0 л (только для бензиновых двигателей)';
comment on column tariff.tariff.coef_engine_2_0_to_2_5 is 'К объема двигателя для ТС от 2,0 до 2,5 л (только для бензиновых двигателей)';
comment on column tariff.tariff.booking_cost is 'Стоимость брони FIX, коп';
comment on column tariff.tariff.coef_material_assets is 'Коэффициент перевозки ТМЦ'





