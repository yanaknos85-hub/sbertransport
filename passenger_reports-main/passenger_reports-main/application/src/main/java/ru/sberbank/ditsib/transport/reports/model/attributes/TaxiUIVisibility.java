package ru.sberbank.ditsib.transport.reports.model.attributes;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
@Data
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxiUIVisibility {
    /**
     * Видимость столбца ID поездки
     */
    @Column(name = "taxi_request_id_visible", nullable = false)
    @Builder.Default
    private Boolean requestIdVisible = true;
    
    /**
     * Видимость столбца Статус поездки
     */
    @Column(name = "taxi_request_status_visible", nullable = false)
    @Builder.Default
    private Boolean requestStatusVisible = true;
    
    /**
     * Видимость столбца ФИО пассажира
     */
    @Column(name = "taxi_passenger_fio_visible", nullable = false)
    @Builder.Default
    private Boolean passengerFioVisible = true;
    
    /**
     * Видимость столбца Места возникноваения затрат для пассажира
     */
    @Column(name = "taxi_passenger_cost_center_visible", nullable = false)
    @Builder.Default
    private Boolean passengerCostCenterVisible = true;
    
    /**
     * Видимость столбца Разъездной характер работ пассажира
     */
    @Column(name = "taxi_passenger_itinerant_type_visible", nullable = false)
    @Builder.Default
    private Boolean passengerItinerantTypeVisible = true;
    
    /**
     * Видимость столбца ID лимита
     */
    @Column(name = "taxi_limit_id_visible", nullable = false)
    @Builder.Default
    private Boolean limitIdVisible = true;
    
    /**
     * Видимость столбца Должность пассажира
     */
    @Column(name = "taxi_passenger_position_visible", nullable = false)
    @Builder.Default
    private Boolean passengerPositionVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 1 лвл
     */
    @Column(name = "taxi_passenger_department_1_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentOneVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 2 лвл
     */
    @Column(name = "taxi_passenger_department_2_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentTwoVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 3 лвл
     */
    @Column(name = "taxi_passenger_department_3_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentThreeVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 4 лвл
     */
    @Column(name = "taxi_passenger_department_4_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentFourVisible = true;
    
    
    /**
     * Видимость столбца Подразделение пассажира 5 лвл
     */
    @Column(name = "taxi_passenger_department_5_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentFiveVisible = true;
    
    /**
     * Видимость столбца Подразделение пассажира 6 лвл
     */
    @Column(name = "taxi_passenger_department_6_visible", nullable = false)
    @Builder.Default
    private Boolean passengerDepartmentSixVisible = true;
    
    /**
     * Видимость столбца ФИО согласующего
     */
    @Column(name = "taxi_approved_by_fio_visible", nullable = false)
    @Builder.Default
    private Boolean approvedByFioVisible = true;
    
    /**
     * Видимость столбца Цели поездки
     */
    @Column(name = "taxi_trip_purpose_visible", nullable = false)
    @Builder.Default
    private Boolean tripPurposeVisible = true;
    
    /**
     * Видимость столбца Дата и время создания поездки
     */
    @Column(name = "taxi_creation_time_visible", nullable = false)
    @Builder.Default
    private Boolean creationTimeVisible = true;
    
    /**
     * Видимость столбца Фактическая дата и время поездки
     */
    @Column(name = "taxi_trip_fact_start_time_visible", nullable = false)
    @Builder.Default
    private Boolean tripFactStartTimeVisible = true;
    
    /**
     * Видимость столбца Адрес отправления
     */
    @Column(name = "taxi_waypoint_from_visible", nullable = false)
    @Builder.Default
    private Boolean waypointFromVisible = true;
    
    /**
     * Видимость столбца Адрес назначения
     */
    @Column(name = "taxi_waypoint_to_visible", nullable = false)
    @Builder.Default
    private Boolean waypointToVisible = true;
    
    /**
     * Видимость промежуточного адреса
     */
    @Column(name = "taxi_intermediate_address_visible", nullable = false)
    @Builder.Default
    private Boolean intermediateAddressVisible = true;
    
    /**
     * Видимость столбца Общее время ожидания
     */
    @Column(name = "taxi_total_waiting_time_visible", nullable = false)
    @Builder.Default
    private Boolean totalWaitingTimeVisible = true;
    
    /**
     * Видимость столбца Количество пассажиров
     */
    @Column(name = "taxi_passengers_count_visible", nullable = false)
    @Builder.Default
    private Boolean passengersCountVisible = true;
    
    /**
     * Видимость столбца Тип поездки
     */
    @Column(name = "taxi_trip_type_visible", nullable = false)
    @Builder.Default
    private Boolean tripTypeVisible = true;
    
    /**
     * Видимость столбца id совместной поездки
     */
    @Column(name = "taxi_shared_ride_id_visible", nullable = false)
    @Builder.Default
    private Boolean sharedRideIdVisible = true;
    
    /**
     * Видимость столбца фактическая стоимость
     */
    @Column(name = "taxi_trip_fact_price_visible", nullable = false)
    @Builder.Default
    private Boolean tripFactPriceVisible = true;
    
    /**
     * Видимость столбца Фактическая дальность, км
     */
    @Column(name = "taxi_actual_range_visible", nullable = false)
    @Builder.Default
    private Boolean actualRangeVisible = true;
    
    /**
     * Видимость столбца Фактическая длительность, мин
     */
    @Column(name = "taxi_actual_duration_visible", nullable = false)
    @Builder.Default
    private Boolean actualDurationVisible = true;
    
    /**
     * Видимость столбца Экономия, руб
     */
    @Column(name = "taxi_saving_visible", nullable = false)
    @Builder.Default
    private Boolean savingVisible = true;
    
    /**
     * Видимость столбца ID тарифа
     */
    @Column(name = "taxi_tariff_id_visible", nullable = false)
    @Builder.Default
    private Boolean tariffIdVisible = true;
    
    /**
     * Видимость столбца Перевозчик
     */
    @Column(name = "taxi_carrier_visible", nullable = false)
    @Builder.Default
    private Boolean carrierVisible = true;
    
    /**
     * Видимость столбца Комментарий для водителя
     */
    @Column(name = "taxi_comment_for_driver_visible", nullable = false)
    @Builder.Default
    private Boolean commentForDriverVisible = true;
    
    /**
     * Видимость столбца Оценка поездки пользователем
     */
    @Column(name = "taxi_request_rating_visible", nullable = false)
    @Builder.Default
    private Boolean ratingVisible = true;
    
    /**
     * Видимость столбца Комментарий к оценке поездки пользователем
     */
    @Column(name = "taxi_request_rating_comment_visible", nullable = false)
    @Builder.Default
    private Boolean ratingCommentVisible = true;
    
    /**
     * Видимость столбца Адрес отправления/назначения является адресом ВСП/ГОСБ/ТБ
     */
    @Column(name = "taxi_vsp_gosb_tb_exist_visible", nullable = false)
    @Builder.Default
    private Boolean vspGosbTbExistVisible = true;
    
    /**
     * Видимость столбца Желаемая дата поездки
     */
    @Column(name = "taxi_desired_date_visible", nullable = false)
    @Builder.Default
    private Boolean desiredDateVisible = true;
    
    /**
     * Видимость столбца Дата внесения фактических параметров поездки
     */
    @Column(name = "taxi_fact_parameters_setting_time_visible", nullable = false)
    @Builder.Default
    private Boolean factParametersSettingTimeVisible = true;
    
    /**
     * Видимость столбца Фактическая дистанция поездки
     */
    @Column(name = "taxi_trip_fact_distance", nullable = false)
    @Builder.Default
    private boolean taxiTripFactDistance = true;
    
    /**
     * Видимость столбца Фактическая время ожидания водителя
     */
    @Column(name = "taxi_trip_fact_wait_time", nullable = false)
    @Builder.Default
    private boolean taxiTripFactWaitTime = true;
    
    /**
     * Видимость столбца HumanReadableID поездки
     */
    @Column(name = "taxi_trip_human_readable_id", nullable = false)
    @Builder.Default
    private boolean taxiTripHumanReadableId = true;
    
    /**
     * Видимость столбца finishedTime
     */
    @Column(name = "finished_time", nullable = false)
    @Builder.Default
    private boolean finishedTimeVisible = true;
    
    /**
     * Контрольный срок подачи ТС
     */
    @Column(name = "taxi_deadline", nullable = false)
    @Builder.Default
    private boolean deadlineVisible = true;
    
    /**
     * Фактическое время подачи ТС
     */
    @Column(name = "taxi_driver_arrived_datetime", nullable = false)
    @Builder.Default
    private boolean driverArrivedDatetimeVisible = true;
    
    /**
     * Нарушение КС (Да/Нет)
     */
    @Column(name = "taxi_deadline_violation", nullable = false)
    @Builder.Default
    private boolean deadlineViolationVisible = true;
    
}
