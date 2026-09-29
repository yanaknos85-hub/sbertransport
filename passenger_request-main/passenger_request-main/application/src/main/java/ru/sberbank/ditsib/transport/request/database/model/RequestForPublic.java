package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.TransportCompensation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сущность - Заявка на компенсацию за Общественный транспорт (OT). Поля tripData и tariffData - для компенсации за Городской ОТ. Поле
 * paymentDocuments - для компенсации за Пригородный ОТ / льготы
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@DynamicUpdate
@Table(schema = "request", name = "request_for_public")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Audited
@AuditTable(schema = "request_audit", value = "request_for_public")
@SQLDelete(sql = "UPDATE request.request_for_public SET active = false WHERE id = ?")
@SQLRestriction("active=true")
public class RequestForPublic extends Request {
    
    //todo - kireev - удалить в будущем historyItems, т.к. подключен аудит envers
    /**
     * History of status changes
     */
    @NotAudited
    @OneToMany(mappedBy = "requestForPublic", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RequestHistoryElementForPublic> historyItemsForPublic = new ArrayList<>();
    
    /**
     * Список документов (сканов, скриншотов), подтверждающих стоимость билета
     */
    @NotAudited
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    @Builder.Default
    private List<CompensationDocument> compensationDocuments = new ArrayList<>();
    
    /**
     * Список компенсаций транспорта
     */
    @NotAudited
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TransportCompensation> transportCompensation = new ArrayList<>();
    
    /**
     * Дата и время подтверждения поездки.
     */
    @Column(name = "trip_confirmation_date")
    private LocalDateTime tripConfirmationDate;
    
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
     * Данные оценки
     */
    @NotAudited
    @Embedded
    private RequestRating requestRating;
    
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
    @Column(name = "payment_done_deadline_state")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeadlineState paymentDoneDeadlineState = DeadlineState.NONE;
    
    /**
     * id заявки с оплатой платных сервисов
     */
    @Column
    private UUID payRequestId;
}
