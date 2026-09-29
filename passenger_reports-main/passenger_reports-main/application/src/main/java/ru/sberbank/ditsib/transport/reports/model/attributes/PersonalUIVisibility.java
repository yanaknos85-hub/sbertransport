package ru.sberbank.ditsib.transport.reports.model.attributes;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
@Data
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalUIVisibility {
    /**
     * Видимость столбца ID поездки
     */
    @Column(name = "personal_request_id_visible", nullable = false)
    @Builder.Default
    private Boolean requestIdVisible = true;
    
    /**
     * Видимость столбца Статус поездки
     */
    @Column(name = "personal_request_status_visible", nullable = false)
    @Builder.Default
    private Boolean requestStatusVisible = true;
    
    /**
     * Видимость столбца ФИО польз. К.К.
     */
    @Column(name = "personal_passenger_fio_visible", nullable = false)
    @Builder.Default
    private Boolean passengerFioVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 1 лвл
     */
    @Column(name = "personal_passenger_department_1_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentOneVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 2 лвл
     */
    @Column(name = "personal_passenger_department_2_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentTwoVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 3 лвл
     */
    @Column(name = "personal_passenger_department_3_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentThreeVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 4 лвл
     */
    @Column(name = "personal_passenger_department_4_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentFourVisible = true;
    
    
    /**
     * Видимость столбца Подразделение пассажира 5 лвл
     */
    @Column(name = "personal_passenger_department_5_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentFiveVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 6 лвл
     */
    @Column(name = "personal_passenger_department_6_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentSixVisible = true;
    
    /**
     * Видимость столбца Цели поездки
     */
    @Column(name = "personal_trip_purpose_visible", nullable = false)
    @Builder.Default
    private Boolean tripPurposeVisible = true;
    
    /**
     * Видимость столбца Дата и время создания поездки
     */
    @Column(name = "personal_creation_time_visible", nullable = false)
    @Builder.Default
    private Boolean creationTimeVisible = true;
    
    /**
     * Видимость столбца Фактическая дата и время поездки
     */
    @Column(name = "personal_trip_fact_start_time_visible", nullable = false)
    @Builder.Default
    private Boolean tripFactStartTimeVisible = true;
    
    /**
     * Видимость столбца Тип поездки
     */
    @Column(name = "personal_trip_type_visible", nullable = false)
    @Builder.Default
    private Boolean tripTypeVisible = true;
    
    /**
     * Видимость столбца фактическая стоимость
     */
    @Column(name = "personal_trip_fact_price_visible", nullable = false)
    @Builder.Default
    private Boolean tripFactPriceVisible = true;
    
    /**
     * Видимость столбца МВЗ
     */
    @Column(name = "personal_mvz_visible", nullable = false)
    @Builder.Default
    private Boolean mvzVisible = true;
    
    /**
     * Видимость столбца Дата согласования поездки
     */
    @Column(name = "personal_approve_date_visible", nullable = false)
    @Builder.Default
    private Boolean approveDateVisible = true;
    
    /**
     * Видимость столбца Табельный номер польз. К.К
     */
    @Column(name = "personal_kk_personal_number_visible", nullable = false)
    @Builder.Default
    private Boolean kkPersonalNumberVisible = true;
    
    /**
     * Видимость столбца Разъездной характер деятельности сотрудника
     */
    @Column(name = "personal_itinerant_type_visible", nullable = false)
    @Builder.Default
    private Boolean itinerantTypeVisible = true;
    
    /**
     * Видимость столбца id совместной поездки
     */
    @Column(name = "personal_shared_ride_id_visible", nullable = false)
    @Builder.Default
    private Boolean sharedRideIdVisible = true;
    
    /**
     * Видимость столбца Адрес отправления
     */
    @Column(name = "personal_waypoint_from_visible", nullable = false)
    @Builder.Default
    private Boolean waypointFromVisible = true;
    
    /**
     * Видимость столбца Адрес назначения
     */
    @Column(name = "personal_waypoint_to_visible", nullable = false)
    @Builder.Default
    private Boolean waypointToVisible = true;
    
    /**
     * Видимость промежуточного адреса
     */
    @Column(name = "personal_intermediate_address_visible", nullable = false)
    @Builder.Default
    private Boolean intermediateAddressVisible = true;
    
    /**
     * Видимость столбца Расстояние поездки
     */
    @Column(name = "personal_actual_range_visible", nullable = false)
    @Builder.Default
    private Boolean actualRangeVisible = true;
    
    /**
     * Видимость столбца Право собственности на автомобиль
     */
    @Column(name = "personal_ownership_of_car_visible", nullable = false)
    @Builder.Default
    private Boolean ownershipOfCarVisible = true;
    
    /**
     * Видимость столбца Номер свидетельства о браке
     */
    @Column(name = "personal_marriage_certificate_number_visible", nullable = false)
    @Builder.Default
    private Boolean marriageCertificateNumberVisible = true;
    
    /**
     * Видимость столбца Регистрационный номер автомобиля
     */
    @Column(name = "personal_car_registration_number_visible", nullable = false)
    @Builder.Default
    private Boolean carRegistrationNumberVisible = true;
    
    /**
     * Видимость столбца Марка автомобиля
     */
    @Column(name = "personal_car_brand_name_visible", nullable = false)
    @Builder.Default
    private Boolean carBrandNameVisible = true;
    
    /**
     * Видимость столбца Объем двигателя автомобиля
     */
    @Column(name = "personal_car_engine_volume_visible", nullable = false)
    @Builder.Default
    private Boolean carEngineVolumeVisible = true;
    
    /**
     * Видимость столбца Номер полиса ОСАГО
     */
    @Column(name = "personal_osago_number_visible", nullable = false)
    @Builder.Default
    private Boolean osagoNumberVisible = true;
    
    /**
     * Видимость столбца Кол-во пунктов маршрута общее
     */
    @Column(name = "personal_waypoints_count_visible", nullable = false)
    @Builder.Default
    private Boolean waypointsCountVisible = true;
    
    /**
     * Видимость столбца Кол-во пунктов маршрута поездки с совпадением координат "Отметиться"
     */
    @Column(name = "personal_waypoints_count_with_check_in_visible", nullable = false)
    @Builder.Default
    private Boolean waypointsCountWithCheckInVisible = true;
    
    /**
     * Видимость столбца Кол-во пунктов поездки без совпадения координат "Отметиться"
     */
    @Column(name = "personal_waypoints_count_without_check_in_visible", nullable = false)
    @Builder.Default
    private Boolean waypointsCountWithoutCheckInVisible = true;
    
    /**
     * Видимость столбца Период выплаты
     */
    @Column(name = "personal_payment_period_visible", nullable = false)
    @Builder.Default
    private Boolean paymentPeriodVisible = true;
    
    /**
     * Видимость столбца Желаемая дата поездки
     */
    @Column(name = "personal_desired_date_visible", nullable = false)
    @Builder.Default
    private Boolean desiredDateVisible = true;
    
    /**
     * Видимость столбца Общая сумма поездки на выплату, руб
     */
    @Column(name = "personal_general_price_trip_visible", nullable = false)
    @Builder.Default
    private Boolean generalPriceTripVisible = true;
    
    /**
     * Видимость столбца Дата утверждения поездки
     */
    @Column(name = "personal_order_payment_formation_start_date_visible", nullable = false)
    @Builder.Default
    private Boolean orderPaymentFormationStartDateVisible = true;
    
    /**
     * Видимость столбца Оценка поездки пользователем
     */
    @Column(name = "personal_request_rating_visible", nullable = false)
    @Builder.Default
    private Boolean ratingVisible = true;
    
    /**
     * Видимость столбца Комментарий к оценке поездки пользователем
     */
    @Column(name = "personal_request_rating_comment_visible", nullable = false)
    @Builder.Default
    private Boolean ratingCommentVisible = true;
    
    /**
     * Видимость столбца paymentTime
     */
    @Column(name = "personal_payment_time", nullable = false)
    @Builder.Default
    private boolean paymentTimeVisible = true;
    
    /**
     * Видимость столбца paymentCost - Сумма к выплате
     */
    @Column(name = "personal_payment_cost_visible", nullable = false)
    @Builder.Default
    private boolean paymentCostVisible = true;
    
    /**
     * Видимость столбца sharedRideOwner - Признак водитель/пассажир
     */
    @Column(name = "personal_shared_ride_owner_visible", nullable = false)
    @Builder.Default
    private boolean sharedRideOwnerVisible = true;
}
