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
public class CarsharingUIVisibility {
    
    /**
     * Видимость столбца ID поездки
     */
    @Column(name = "carsharing_request_id_visible", nullable = false)
    @Builder.Default
    private Boolean requestIdVisible = true;
    
    /**
     * Видимость столбца Статус поездки
     */
    @Column(name = "carsharing_request_status_visible", nullable = false)
    @Builder.Default
    private Boolean requestStatusVisible = true;
    
    /**
     * Видимость столбца ФИО пассажира
     */
    @Column(name = "carsharing_passenger_fio_visible", nullable = false)
    @Builder.Default
    private Boolean passengerFioVisible = true;
    
    /**
     * Видимость столбца ID лимита
     */
    @Column(name = "carsharing_limit_id_visible", nullable = false)
    @Builder.Default
    private Boolean limitIdVisible = true;
    /**
     * Видимость столбца Подразделение пассажира
     */
    @Column(name = "carsharing_passenger_department_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentVisible = true;
    
    /**
     * Видимость столбца ФИО согласующего
     */
    @Column(name = "carsharing_approved_by_fio_visible", nullable = false)
    @Builder.Default
    private Boolean approvedByFioVisible = true;
    
    /**
     * Видимость столбца Цели поездки
     */
    @Column(name = "carsharing_trip_purpose_visible", nullable = false)
    @Builder.Default
    private Boolean tripPurposeVisible = true;
    
    /**
     * Видимость столбца Дата и время создания поездки
     */
    @Column(name = "carsharing_creation_time_visible", nullable = false)
    @Builder.Default
    private Boolean creationTimeVisible = true;
    
    /**
     * Видимость столбца Дата и время поездки
     */
    @Column(name = "carsharing_desired_date_visible", nullable = false)
    @Builder.Default
    private Boolean desiredDateVisible = true;
    
    /**
     * Видимость столбца Адрес отправления
     */
    @Column(name = "carsharing_waypoint_from_visible", nullable = false)
    @Builder.Default
    private Boolean waypointFromVisible = true;
    
    /**
     * Видимость столбца Адрес назначения
     */
    @Column(name = "carsharing_waypoint_to_visible", nullable = false)
    @Builder.Default
    private Boolean waypointToVisible = true;
    
    /**
     * Видимость промежуточного адреса
     */
    @Column(name = "carsharing_intermediate_address_visible", nullable = false)
    @Builder.Default
    private Boolean intermediateAddressVisible = true;
    
    /**
     * Видимость столбца Количество пассажиров
     */
    @Column(name = "carsharing_passengers_count_visible", nullable = false)
    @Builder.Default
    private Boolean passengersCountVisible = true;
    
    /**
     * Видимость столбца Тип поездки
     */
    @Column(name = "carsharing_trip_type_visible", nullable = false)
    @Builder.Default
    private Boolean tripTypeVisible = true;
    
    /**
     * Видимость столбца ID совместной поездки
     */
    @Column(name = "carsharing_shared_ride_id_visible", nullable = false)
    @Builder.Default
    private Boolean sharedRideIdVisible = true;
    
    /**
     * Видимость столбца Стоимость, руб
     */
    @Column(name = "carsharing_cost_visible", nullable = false)
    @Builder.Default
    private Boolean costVisible = true;
    
    /**
     * Видимость столбца Фактическая дальность, км
     */
    @Column(name = "carsharing_actual_range_visible", nullable = false)
    @Builder.Default
    private Boolean actualRangeVisible = true;
    
    /**
     * Видимость столбца Фактическая длительность, мин
     */
    @Column(name = "carsharing_actual_duration_visible", nullable = false)
    @Builder.Default
    private Boolean actualDurationVisible = true;
    
    /**
     * Видимость столбца Экономия, руб
     */
    @Column(name = "carsharing_saving_visible", nullable = false)
    @Builder.Default
    private Boolean savingVisible = true;
    
    /**
     * Видимость столбца ID тарифа
     */
    @Column(name = "carsharing_tariff_id_visible", nullable = false)
    @Builder.Default
    private Boolean tariffIdVisible = true;
    
    /**
     * Видимость столбца Перевозчик
     */
    @Column(name = "carsharing_carrier_visible", nullable = false)
    @Builder.Default
    private Boolean carrierVisible = true;
    
    /**
     * Видимость столбца Фактическое время выезда
     */
    @Column(name = "carsharing_actual_departure_time_visible", nullable = false)
    @Builder.Default
    private Boolean actualDepartureTimeVisible = true;
    
    /**
     * Видимость столбца Оценка поездки пользователем
     */
    @Column(name = "carsharing_request_rating_visible", nullable = false)
    @Builder.Default
    private Boolean ratingVisible = true;
    
    /**
     * Видимость столбца Комментарий к оценке поездки пользователем
     */
    @Column(name = "carsharing_request_rating_comment_visible", nullable = false)
    @Builder.Default
    private Boolean ratingCommentVisible = true;
}
