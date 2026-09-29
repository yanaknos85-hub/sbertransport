create table reports.user_attributes
(
    id                                                  uuid                  not null
        constraint user_attributes_pkey
            primary key,
    user_id                                             uuid                  not null unique,
    taxi_request_id_visible                             boolean default true  not null,
    taxi_request_status_visible                         boolean default true  not null,
    taxi_passenger_fio_visible                          boolean default true  not null,
    taxi_limit_id_visible                               boolean default true  not null,
    taxi_passenger_position_visible                     boolean default true  not null,
    taxi_approved_by_fio_visible                        boolean default false not null,
    taxi_trip_purpose_visible                           boolean default true  not null,
    taxi_creation_time_visible                          boolean default true  not null,
    taxi_trip_fact_start_time_visible                   boolean default true  not null,
    taxi_waypoint_from_visible                          boolean default true  not null,
    taxi_waypoint_to_visible                            boolean default true  not null,
    taxi_total_waiting_time_visible                     boolean default true  not null,
    taxi_passengers_count_visible                       boolean default false not null,
    taxi_trip_type_visible                              boolean default true  not null,
    taxi_trip_fact_price_visible                        boolean default true  not null,
    taxi_actual_range_visible                           boolean default false not null,
    taxi_actual_duration_visible                        boolean default false not null,
    taxi_saving_visible                                 boolean default true  not null,
    taxi_tariff_id_visible                              boolean default false not null,
    taxi_carrier_visible                                boolean default true  not null,
    taxi_comment_for_driver_visible                     boolean default false not null,
    taxi_request_rating_visible                         boolean default false not null,
    public_request_id_visible                           boolean default true  not null,
    public_request_status_visible                       boolean default true  not null,
    public_passenger_fio_visible                        boolean default true  not null,
    public_trip_purpose_visible                         boolean default true  not null,
    public_creation_time_visible                        boolean default false not null,
    public_desired_date_visible                         boolean default true  not null,
    public_cost_visible                                 boolean default true  not null,
    personal_request_id_visible                         boolean default true  not null,
    personal_request_status_visible                     boolean default true  not null,
    personal_passenger_fio_visible                      boolean default true  not null,
    personal_trip_purpose_visible                       boolean default true  not null,
    personal_creation_time_visible                      boolean default true  not null,
    personal_trip_fact_start_time_visible               boolean default true  not null,
    personal_trip_type_visible                          boolean default true  not null,
    personal_trip_fact_price_visible                    boolean default true  not null,
    personal_actual_range_visible                       boolean default true  not null,
    public_mvz_visible                                  boolean default true  not null,
    public_approve_date_visible                         boolean default true  not null,
    public_itinerant_type_visible                       boolean default false not null,
    public_transport_type_visible                       boolean default true  not null,
    public_has_attachment_visible                       boolean default false not null,
    public_waypoints_count_visible                      boolean default false not null,
    public_waypoints_count_with_check_in_visible        boolean default false not null,
    public_waypoints_count_without_check_in_visible     boolean default false not null,
    public_payment_period_visible                       boolean default false not null,
    personal_mvz_visible                                boolean default true  not null,
    personal_approve_date_visible                       boolean default true  not null,
    personal_kk_personal_number_visible                 boolean default true  not null,
    personal_itinerant_type_visible                     boolean default true  not null,
    personal_ownership_of_car_visible                   boolean default false not null,
    personal_marriage_certificate_number_visible        boolean default false not null,
    personal_car_registration_number_visible            boolean default false not null,
    personal_car_brand_name_visible                     boolean default false not null,
    personal_car_engine_volume_visible                  boolean default false not null,
    personal_osago_number_visible                       boolean default false not null,
    personal_waypoints_count_visible                    boolean default false not null,
    personal_waypoints_count_with_check_in_visible      boolean default false not null,
    personal_waypoints_count_without_check_in_visible   boolean default false not null,
    personal_payment_period_visible                     boolean default false not null,
    taxi_shared_ride_id_visible                         boolean default true  not null,
    taxi_intermediate_address_visible                   boolean default false not null,
    taxi_vsp_gosb_tb_exist_visible                      boolean default false not null,
    carsharing_request_id_visible                       boolean default true  not null,
    carsharing_request_status_visible                   boolean default true  not null,
    carsharing_passenger_fio_visible                    boolean default true  not null,
    carsharing_passenger_department_visible             boolean default true  not null,
    carsharing_trip_purpose_visible                     boolean default true  not null,
    carsharing_creation_time_visible                    boolean default true  not null,
    carsharing_desired_date_visible                     boolean default true  not null,
    carsharing_waypoint_from_visible                    boolean default true  not null,
    carsharing_waypoint_to_visible                      boolean default true  not null,
    carsharing_intermediate_address_visible             boolean default false not null,
    carsharing_passengers_count_visible                 boolean default false not null,
    carsharing_trip_type_visible                        boolean default true  not null,
    carsharing_shared_ride_id_visible                   boolean default true  not null,
    carsharing_cost_visible                             boolean default true  not null,
    carsharing_actual_range_visible                     boolean default false not null,
    carsharing_actual_duration_visible                  boolean default false not null,
    carsharing_saving_visible                           boolean default true  not null,
    carsharing_tariff_id_visible                        boolean default false not null,
    carsharing_carrier_visible                          boolean default true  not null,
    carsharing_actual_departure_time_visible            boolean default false not null,
    carsharing_limit_id_visible                         boolean default true  not null,
    carsharing_approved_by_fio_visible                  boolean default false not null,
    taxi_passenger_cost_center_visible                  boolean default true  not null,
    taxi_passenger_itinerant_type_visible               boolean default true  not null,
    taxi_passenger_department_1_visible                 boolean default true  not null,
    taxi_passenger_department_2_visible                 boolean default true  not null,
    taxi_passenger_department_3_visible                 boolean default true  not null,
    taxi_passenger_department_4_visible                 boolean default true  not null,
    taxi_passenger_department_5_visible                 boolean default true  not null,
    taxi_passenger_department_6_visible                 boolean default true  not null,
    personal_shared_ride_id_visible                     boolean default false not null,
    personal_waypoint_from_visible                      boolean default true  not null,
    personal_waypoint_to_visible                        boolean default true  not null,
    personal_intermediate_address_visible               boolean default false not null,
    personal_passenger_department_1_visible             boolean default true  not null,
    personal_passenger_department_2_visible             boolean default true  not null,
    personal_passenger_department_3_visible             boolean default true  not null,
    personal_passenger_department_4_visible             boolean default true  not null,
    personal_passenger_department_5_visible             boolean default true  not null,
    personal_passenger_department_6_visible             boolean default true  not null,
    public_compensation_type_visible                    boolean default true  not null,
    public_waypoint_from_visible                        boolean default true  not null,
    public_waypoint_to_visible                          boolean default true  not null,
    public_intermediate_address_visible                 boolean default false not null,
    public_passenger_department_1_visible               boolean default true  not null,
    public_passenger_department_2_visible               boolean default true  not null,
    public_passenger_department_3_visible               boolean default true  not null,
    public_passenger_department_4_visible               boolean default true  not null,
    public_passenger_department_5_visible               boolean default true  not null,
    public_passenger_department_6_visible               boolean default true  not null,
    public_personel_number_visible                      boolean default true  not null,
    taxi_desired_date_visible                           boolean default false not null,
    taxi_fact_parameters_setting_time_visible           boolean default false not null,
    personal_desired_date_visible                       boolean default false not null,
    personal_general_price_trip_visible                 boolean default false not null,
    personal_order_payment_formation_start_date_visible boolean default true  not null,
    public_order_payment_formation_start_date_visible   boolean default true  not null,
    taxi_request_rating_comment_visible                 boolean default true  not null,
    personal_request_rating_visible                     boolean default true  not null,
    personal_request_rating_comment_visible             boolean default true  not null,
    public_request_rating_visible                       boolean default true  not null,
    public_request_rating_comment_visible               boolean default true  not null,
    carsharing_request_rating_visible                   boolean default true  not null,
    carsharing_request_rating_comment_visible           boolean default true  not null
);

comment on column reports.user_attributes.taxi_trip_fact_start_time_visible is 'Видимость столбца Фактическая дата и время поездки';

comment on column reports.user_attributes.taxi_trip_fact_price_visible is 'Видимость столбца Фактическая стоимость';

comment on column reports.user_attributes.personal_trip_fact_start_time_visible is 'Видимость столбца Фактическая дата и время поездки';

comment on column reports.user_attributes.personal_trip_fact_price_visible is 'Видимость столбца Фактическая стоимость';

comment on column reports.user_attributes.taxi_shared_ride_id_visible is 'Отображение id совместной поездки';

comment on column reports.user_attributes.taxi_intermediate_address_visible is 'Отображение промежуточных остановок';

comment on column reports.user_attributes.taxi_vsp_gosb_tb_exist_visible is 'Адрес отправления/назначения является адресом ВСП/ГОСБ/ТБ';

comment on column reports.user_attributes.carsharing_request_id_visible is 'Видимость столбца ID заявки';

comment on column reports.user_attributes.carsharing_request_status_visible is 'Видимость столбца Статус заявки';

comment on column reports.user_attributes.carsharing_passenger_fio_visible is 'Видимость столбца ФИО пассажира';

comment on column reports.user_attributes.carsharing_passenger_department_visible is 'Видимость столбца Подразделение пассажира';

comment on column reports.user_attributes.carsharing_trip_purpose_visible is 'Видимость столбца Цели поездки';

comment on column reports.user_attributes.carsharing_creation_time_visible is 'Видимость столбца Дата и время создания поездки';

comment on column reports.user_attributes.carsharing_desired_date_visible is 'Видимость столбца Дата и время поездки';

comment on column reports.user_attributes.carsharing_waypoint_from_visible is 'Видимость столбца Адрес отправления';

comment on column reports.user_attributes.carsharing_waypoint_to_visible is 'Видимость столбца Адрес назначения';

comment on column reports.user_attributes.carsharing_intermediate_address_visible is 'Видимость промежуточного адреса';

comment on column reports.user_attributes.carsharing_passengers_count_visible is 'Видимость столбца Количество пассажиров';

comment on column reports.user_attributes.carsharing_trip_type_visible is 'Видимость столбца Тип поездки';

comment on column reports.user_attributes.carsharing_shared_ride_id_visible is 'Видимость столбца ID совместной поездки';

comment on column reports.user_attributes.carsharing_cost_visible is 'Видимость столбца Стоимость, руб';

comment on column reports.user_attributes.carsharing_actual_range_visible is 'Видимость столбца Фактическая дальность, км';

comment on column reports.user_attributes.carsharing_actual_duration_visible is 'Видимость столбца Фактическая длительность, мин';

comment on column reports.user_attributes.carsharing_saving_visible is 'Видимость столбца Экономия, руб';

comment on column reports.user_attributes.carsharing_tariff_id_visible is 'Видимость столбца ID тарифа';

comment on column reports.user_attributes.carsharing_carrier_visible is 'Видимость столбца Перевозчик';

comment on column reports.user_attributes.carsharing_actual_departure_time_visible is 'Видимость столбца Фактическое время выезда';

comment on column reports.user_attributes.carsharing_limit_id_visible is 'Видимость столбца ID лимита';

comment on column reports.user_attributes.carsharing_approved_by_fio_visible is 'Видимость столбца ФИО согласующего';

comment on column reports.user_attributes.taxi_passenger_cost_center_visible is 'Видимость столбца МВЗ';

comment on column reports.user_attributes.taxi_passenger_itinerant_type_visible is 'Видимость столбца Разъездной характер работ';

comment on column reports.user_attributes.taxi_passenger_department_1_visible is 'Видимость столбца Подразделение 1 ур';

comment on column reports.user_attributes.taxi_passenger_department_2_visible is 'Видимость столбца Подразделение 2 ур';

comment on column reports.user_attributes.taxi_passenger_department_3_visible is 'Видимость столбца Подразделение 3 ур';

comment on column reports.user_attributes.taxi_passenger_department_4_visible is 'Видимость столбца Подразделение 4 ур';

comment on column reports.user_attributes.taxi_passenger_department_5_visible is 'Видимость столбца Подразделение 5 ур';

comment on column reports.user_attributes.taxi_passenger_department_6_visible is 'Видимость столбца Подразделение 6 ур';

comment on column reports.user_attributes.personal_shared_ride_id_visible is 'Видимость столбца id совместной поездки';

comment on column reports.user_attributes.personal_waypoint_from_visible is 'Видимость столбца  Адрес отправления';

comment on column reports.user_attributes.personal_waypoint_to_visible is 'Видимость столбца Адрес назначения';

comment on column reports.user_attributes.personal_intermediate_address_visible is 'Видимость столбца Промежуточного адреса';

comment on column reports.user_attributes.personal_passenger_department_1_visible is 'Видимость столбца Подразделение 1 ур';

comment on column reports.user_attributes.personal_passenger_department_2_visible is 'Видимость столбца Подразделение 2 ур';

comment on column reports.user_attributes.personal_passenger_department_3_visible is 'Видимость столбца Подразделение 3 ур';

comment on column reports.user_attributes.personal_passenger_department_4_visible is 'Видимость столбца Подразделение 4 ур';

comment on column reports.user_attributes.personal_passenger_department_5_visible is 'Видимость столбца Подразделение 5 ур';

comment on column reports.user_attributes.personal_passenger_department_6_visible is 'Видимость столбца Подразделение 6 ур';

comment on column reports.user_attributes.public_compensation_type_visible is 'Видимость столбца Вид компенсации';

comment on column reports.user_attributes.public_waypoint_from_visible is 'Видимость столбца  Адрес отправления';

comment on column reports.user_attributes.public_waypoint_to_visible is 'Видимость столбца Адрес назначения';

comment on column reports.user_attributes.public_intermediate_address_visible is 'Видимость столбца Промежуточного адреса';

comment on column reports.user_attributes.public_passenger_department_1_visible is 'Видимость столбца Подразделение 1 ур';

comment on column reports.user_attributes.public_passenger_department_2_visible is 'Видимость столбца Подразделение 2 ур';

comment on column reports.user_attributes.public_passenger_department_3_visible is 'Видимость столбца Подразделение 3 ур';

comment on column reports.user_attributes.public_passenger_department_4_visible is 'Видимость столбца Подразделение 4 ур';

comment on column reports.user_attributes.public_passenger_department_5_visible is 'Видимость столбца Подразделение 5 ур';

comment on column reports.user_attributes.public_passenger_department_6_visible is 'Видимость столбца Подразделение 6 ур';

comment on column reports.user_attributes.taxi_desired_date_visible is 'Планируемая дата и время поездки';

comment on column reports.user_attributes.taxi_fact_parameters_setting_time_visible is 'Дата внесения фактических параметров поездки';

comment on column reports.user_attributes.personal_desired_date_visible is 'Планируемая дата и время поездки';

comment on column reports.user_attributes.personal_general_price_trip_visible is 'Видимость колонки Общая сумма поездки на выплату, руб';

comment on column reports.user_attributes.taxi_request_rating_comment_visible is 'Видимость колонки Такси комментарий к оценке поездки';

comment on column reports.user_attributes.personal_request_rating_visible is 'Видимость колонки ЛТ оценка поездки';

comment on column reports.user_attributes.personal_request_rating_comment_visible is 'Видимость колонки ЛТ комментарий к оценке поездки';

comment on column reports.user_attributes.public_request_rating_visible is 'Видимость колонки ОТ оценка поездки';

comment on column reports.user_attributes.public_request_rating_comment_visible is 'Видимость колонки ОТ комментарий к оценке поездки';

comment on column reports.user_attributes.carsharing_request_rating_visible is 'Видимость колонки Каршеринг оценка поездки';

comment on column reports.user_attributes.carsharing_request_rating_comment_visible is 'Видимость колонки Каршеринг комментарий к оценке поездки';

