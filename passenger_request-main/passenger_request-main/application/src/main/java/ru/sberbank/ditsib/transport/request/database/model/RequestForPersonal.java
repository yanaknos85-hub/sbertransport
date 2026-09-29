package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.NotAudited;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.request.database.model.magenta.CoopRequest;
import ru.sberbank.ditsib.transport.request.dto.mapper.db.converter.PersonalCarDtoConverter;
import ru.sberbank.ditsib.transport.request.dto.personal.PersonalCarDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@DynamicUpdate
@Table(schema = "request", name = "request_for_personal")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Slf4j
@SQLDelete(sql = "UPDATE request_for_personal SET active = false WHERE id = ?")
@SQLRestriction("active=true")
public class RequestForPersonal extends AbstractRequestForTnP implements CoopRequest {
    
    /**
     * Количество занятых мест в личном транспорте. Задается для transportType = PERSONAL
     */
    @Column(name = "occupied_places_count")
    private Integer occupiedPlacesCount;
    
    /**
     * ID of personal car
     */
    @Column(name = "personal_car_id")
    private UUID personalCarId;
    
    //Данные использованного личного транспорта
    @Builder.Default
    @Convert(converter = PersonalCarDtoConverter.class)
    @Column(name = "personal_car")
    private PersonalCarDTO personalCar = new PersonalCarDTO();
    
    @Column(name = "employee_driver_id")
    private UUID employeeDriverId;
    
    /**
     * Время начала поездки
     */
    @Column(name = "trip_start_time")
    private LocalDateTime tripStartTime;
    
    /**
     * Широта начала поездки
     */
    @Builder.Default
    @Column(name = "trip_start_latitude")
    private double tripStartLatitude = 0d;
    
    /**
     * Долгота начала поездки
     */
    @Builder.Default
    @Column(name = "trip_start_longitude")
    private double tripStartLongitude = 0d;
    
    /**
     * History of status changes
     */
    @OneToMany(mappedBy = "requestForPersonal", cascade = CascadeType.ALL, orphanRemoval = true, fetch =
            FetchType.LAZY)
    @Builder.Default
    private final List<RequestHistoryElementForPersonal> historyItemsForPersonal = new ArrayList<>();
    
    /**
     * Флаг просрочки SLA
     */
    @Column(name = "is_sla_expired")
    private boolean isSlaExpired;
    
    /**
     * Дата и время начала формирования приказа на выплату
     */
    @Column(name = "order_payment_formation_start_date")
    private LocalDateTime orderPaymentFormationStartDate;
    
    /**
     * Дата и время окончания формирования приказа на выплату
     */
    @Column(name = "order_payment_formation_finishing_date")
    private LocalDateTime orderPaymentFormationFinishingDate;
    
    /**
     * Сумма доплаты за всех пассажиров в копейках
     */
    @Column(name = "additional_sum")
    private Long additionalSum;
    
    /**
     * Количество присоединившихся пассажиров (заявок)
     */
    @Column(name = "number_passengers_joined")
    private Integer numberPassengersJoined;
    /**
     * Причина для начисления дополнительной суммы
     */
    @Column(name = "additional_sum_reason")
    private String additionalSumReason;
    
    /**
     * Данные оценки
     */
    @NotAudited
    @Embedded
    private RequestRating requestRating;
    
    /**
     * Время утверждения/отклонения маршрута заявки
     */
    @Column(name = "trip_approval_datetime")
    private LocalDateTime tripApprovalDatetime;
    
    /**
     * Максимальное время утверждения маршрута заявки без нарушения контрольного срока
     */
    @Column(name = "trip_approval_deadline")
    private LocalDateTime tripApprovalDeadline;
    
    /**
     * Индикатор контрольного срока утверждения маршрута заявки
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "trip_approval_deadline_state")
    private DeadlineState tripApprovalDeadlineState = DeadlineState.NONE;
    
    /**
     * Время выплаты компенсации по заявке
     */
    @Column(name = "payment_done_datetime")
    private LocalDateTime paymentDoneDatetime;
    
    /**
     * Максимальное время выплаты компенсации по заявке без нарушения контрольного срока
     */
    @Column(name = "payment_done_deadline")
    private LocalDateTime paymentDoneDeadline;
    
    /**
     * Индикатор контрольного срока выплаты компенсации по заявке
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_done_deadline_state")
    private DeadlineState paymentDoneDeadlineState = DeadlineState.NONE;
    
    @PrePersist
    private void addLinks() {
        if (getWaypoints() != null) {
            getWaypoints().forEach(wp -> wp.setRequest(this));
        }
        if (getHistoryItemsForPersonal() != null) {
            getHistoryItemsForPersonal().forEach(item -> item.setRequestForPersonal(this));
        }
    }
    
    @PreRemove
    private void removeLinkedEntities() {
        this.historyItemsForPersonal.clear();
    }
    
    /**
     * Является ли сотрудник водителем
     *
     * @return true, если поездка совместная и id совпадают
     */
    public boolean isDriver() {
        if (!Boolean.TRUE.equals(isCoopTrip())) {
            return false;
        }
        return Optional.ofNullable(employeeDriverId)
                       .map(i -> i.equals(getPassenger().getId()))
                       .orElseThrow(() -> new NullPointerException("Driver id is null"));
    }
    
    /**
     * Является ли сотрудник водителем
     *
     * @return true, если поездка совместная и id совпадают
     */
    public boolean isDriverWithoutError() {
        return Optional.ofNullable(employeeDriverId)
                       .map(i -> i.equals(getPassenger().getId())).orElse(false);
    }
}
