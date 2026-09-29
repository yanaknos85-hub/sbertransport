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
public class PublicUIVisibility {
    /**
     * Видимость столбца ID поездки
     */
    @Column(name = "public_request_id_visible", nullable = false)
    @Builder.Default
    private Boolean requestIdVisible = true;
    
    /**
     * Видимость столбца Статус поездки
     */
    @Column(name = "public_request_status_visible", nullable = false)
    @Builder.Default
    private Boolean requestStatusVisible = true;
    
    /**
     * Видимость столбца ФИО пассажира
     */
    @Column(name = "public_passenger_fio_visible", nullable = false)
    @Builder.Default
    private Boolean passengerFioVisible = true;
    
    /**
     * Видимость столбца Цели поездки
     */
    @Column(name = "public_trip_purpose_visible", nullable = false)
    @Builder.Default
    private Boolean tripPurposeVisible = true;
    
    /**
     * Видимость столбца Дата и время создания поездки
     */
    @Column(name = "public_creation_time_visible", nullable = false)
    @Builder.Default
    private Boolean creationTimeVisible = true;
    
    /**
     * Видимость столбца Дата и время поездки
     */
    @Column(name = "public_desired_date_visible", nullable = false)
    @Builder.Default
    private Boolean desiredDateVisible = true;
    
    /**
     * Видимость столбца Стоимость, руб
     */
    @Column(name = "public_cost_visible", nullable = false)
    @Builder.Default
    private Boolean costVisible = true;
    
    /**
     * Видимость столбца МВЗ
     */
    @Column(name = "public_mvz_visible", nullable = false)
    @Builder.Default
    private Boolean mvzVisible = true;
    
    /**
     * Видимость столбца Дата согласования поездки
     */
    @Column(name = "public_approve_date_visible", nullable = false)
    @Builder.Default
    private Boolean approveDateVisible = true;
    
    /**
     * Видимость столбца Разъездной характер деятельности сотрудника
     */
    @Column(name = "public_itinerant_type_visible", nullable = false)
    @Builder.Default
    private Boolean itinerantTypeVisible = true;
    
    /**
     * Видимость столбца Тип транспорта маршрута
     */
    @Column(name = "public_transport_type_visible", nullable = false)
    @Builder.Default
    private Boolean transportTypeVisible = true;
    
    /**
     * Видимость столбца Тип транспорта маршрута
     */
    @Column(name = "public_compensation_type_visible", nullable = false)
    @Builder.Default
    private Boolean compensationTypeVisible = true;
    
    /**
     * Видимость столбца Адрес отправления
     */
    @Column(name = "public_waypoint_from_visible", nullable = false)
    @Builder.Default
    private Boolean waypointFromVisible = true;
    
    /**
     * Видимость столбца Адрес назначения
     */
    @Column(name = "public_waypoint_to_visible", nullable = false)
    @Builder.Default
    private Boolean waypointToVisible = true;
    
    /**
     * Видимость промежуточного адреса
     */
    @Column(name = "public_intermediate_address_visible", nullable = false)
    @Builder.Default
    private Boolean intermediateAddressVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 1 лвл
     */
    @Column(name = "public_passenger_department_1_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentOneVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 2 лвл
     */
    @Column(name = "public_passenger_department_2_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentTwoVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 3 лвл
     */
    @Column(name = "public_passenger_department_3_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentThreeVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 4 лвл
     */
    @Column(name = "public_passenger_department_4_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentFourVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 5 лвл
     */
    @Column(name = "public_passenger_department_5_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentFiveVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 6 лвл
     */
    @Column(name = "public_passenger_department_6_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentSixVisible = true;
    
    /**
     * Видимость столбца Есть вложение (в заявку вложен билет/картинка)
     */
    @Column(name = "public_has_attachment_visible", nullable = false)
    @Builder.Default
    private Boolean hasAttachmentVisible = true;
    
    /**
     * Видимость столбца Кол-во пунктов маршрута общее
     */
    @Column(name = "public_waypoints_count_visible", nullable = false)
    @Builder.Default
    private Boolean waypointsCountVisible = true;
    
    /**
     * Видимость столбца Кол-во пунктов маршрута поездки с совпадением координат "Отметиться"
     */
    @Column(name = "public_waypoints_count_with_check_in_visible", nullable = false)
    @Builder.Default
    private Boolean waypointsCountWithCheckInVisible = true;
    
    /**
     * Видимость столбца Кол-во пунктов поездки без совпадения координат "Отметиться"
     */
    @Column(name = "public_waypoints_count_without_check_in_visible", nullable = false)
    @Builder.Default
    private Boolean waypointsCountWithoutCheckInVisible = true;
    
    /**
     * Видимость столбца Период выплаты
     */
    @Column(name = "public_payment_period_visible", nullable = false)
    @Builder.Default
    private Boolean paymentPeriodVisible = true;
    
    /**
     * Видимость столбца Табельный номер пассажира
     */
    @Column(name = "public_personel_number_visible", nullable = false)
    @Builder.Default
    private Boolean personelNumberVisible = true;
    
    /**
     * Видимость столбца Дата утверждения поездки
     */
    @Column(name = "public_order_payment_formation_start_date_visible", nullable = false)
    @Builder.Default
    private Boolean orderPaymentFormationStartDateVisible = true;
    
    /**
     * Видимость столбца Оценка поездки пользователем
     */
    @Column(name = "public_request_rating_visible", nullable = false)
    @Builder.Default
    private Boolean ratingVisible = true;
    
    /**
     * Видимость столбца Комментарий к оценке поездки пользователем
     */
    @Column(name = "public_request_rating_comment_visible", nullable = false)
    @Builder.Default
    private Boolean ratingCommentVisible = true;
    
    /**
     * Видимость столбца paymentTime
     */
    @Column(name = "public_payment_time", nullable = false)
    @Builder.Default
    private boolean paymentTimeVisible = true;
}
