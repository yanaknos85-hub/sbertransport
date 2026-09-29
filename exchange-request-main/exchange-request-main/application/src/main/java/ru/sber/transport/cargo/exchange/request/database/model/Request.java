package ru.sber.transport.cargo.exchange.request.database.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyDto;
import ru.sber.transport.cargo.exchange.request.dto.RequestDto;
import ru.sber.transport.cargo.exchange.request.enums.PaymentForm;
import ru.sber.transport.cargo.exchange.request.enums.PaymentTerms;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.enums.ViewType;
import ru.sber.transport.cargo.exchange.request.mapper.RequestMapperImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Сущность "Заявка" (requests)
 * Основная сущность биржи грузоперевозок.
 * Хранится в таблице 'request' схемы 'exchange_request'.
 */
@NamedEntityGraph(
        name = "requestFull",
        attributeNodes = {
                @NamedAttributeNode(value = "waypoints", subgraph = "waypointGraph"),
        },
        subgraphs = {
        @NamedSubgraph(
                name = "waypointGraph",
                attributeNodes = {
                        @NamedAttributeNode("addressInfo")
                }
        )
}
)
@Entity
@Table(schema = "exchange_request", name = "request")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    /**
     * Уникальный идентификатор заявки (UUID)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Человекочитаемый номер: формат ОП-ГГГГММ-ННННННН, например ОП-202601-0000001
     */
    @Column(name = "humanreadable_id", nullable = false, unique = true, length = 50)
    private String humanReadableId;

    /**
     * Номер заказа в системе грузовладельца
     * Допустимы любые символы, кроме: <>?@#${}[]*()!~`'';:,&%
     */
    @Column(name = "internal_id", length = 100)
    private String internalId;

    /**
     * Ссылка на пользователя-создателя заявки
     */
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    /**
     * Текущий статус заявки
     * Допустимые значения: cargo_draft, cargo_published, cargo_archived
     * По умолчанию: cargo_draft
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RequestStatus status = RequestStatus.DRAFT;

    /**
     * Ссылка на организацию
     */
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    /**
     * Ссылка на организацию грузоперевозчика
     */
    private UUID carrierOrganizationId;

    /**
     * Информация о грузоперевозчике
     */
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private CarrierReplyDto carrierInfo;

    /**
     * Информация о черновике
     */
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private RequestDto draftInfo;

    /**
     * Флаг использования ЭТрН
     * По умолчанию: TRUE
     */
    @Column(name = "use_etrn", nullable = false)
    @Builder.Default
    private Boolean useEtrn = true;

    /**
     * Вид заявки для перевозчиков
     * MVP: только 'fixed'
     */
    @Column(name = "view_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ViewType viewType = ViewType.FIXED;

    /**
     * Форма оплаты: cash, non_cash
     */
    @Column(name = "payment_form", length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentForm paymentForm;

    /**
     * Условия оплаты: prepayment, on_delivery, deferred_payment
     */
    @Column(name = "payment_terms", length = 100)
    @Enumerated(EnumType.STRING)
    private PaymentTerms paymentTerms;

    /**
     * Срок оплаты в днях (1–30), заполняется только при отсрочке
     */
    @Column(name = "payment_days")
    private Integer paymentDays;

    /**
     * Дата создания заявки в системе заказчика
     */
    @Column(name = "request_created")
    private LocalDate requestCreated;

    /**
     * ФИО отправителя
     */
    @Column(name = "sender_fio")
    private String senderFio;

    /**
     * Телефон отправителя
     */
    @Column(name = "sender_phone", length = 20)
    private String senderPhone;

    /**
     * ФИО получателя
     */
    @Column(name = "recipient_fio")
    private String recipientFio;

    /**
     * Телефон получателя
     */
    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    /**
     * Дата и время создания записи
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Дата автоматического удаления черновика (created_at + 30 дней)
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Дата публикации заявки
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * Дата завершения заявки
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderingIndex ASC")  // Сортировка на уровне БД
    @Builder.Default
    private List<Waypoint> waypoints = new ArrayList<>();

    /**
     * Дата погрузки: берётся из поля 'date' первого waypoint (по orderingIndex) с типом LOAD.
     * Вычисляется через SQL-подзапрос.
     */
    @Formula("(SELECT w.date FROM exchange_request.waypoint w " +
            "WHERE w.request_id = id AND w.type = 'LOAD' " +
            "ORDER BY w.ordering_index ASC LIMIT 1)")
    private LocalDate loadingDate;

    /**
     * Дата доставки: берётся из поля 'date' последнего waypoint (по orderingIndex) с типом UNLOAD.
     * Вычисляется через SQL-подзапрос.
     */
    @Formula("(SELECT w.date FROM exchange_request.waypoint w " +
            "WHERE w.request_id = id AND w.type = 'UNLOAD' " +
            "ORDER BY w.ordering_index DESC LIMIT 1)")
    private LocalDate deliveryDate;

    /**
     * Список идентификаторов организаций грузоперевозчиков, откликнувшихся на заявку
     */
    @Formula("(select jsonb_agg(cr.organization_id) from exchange_request.request_carrier_reply cr " +
            "where cr.request_id = id)")
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<UUID> carrierReplyOrganizationIds;

    /**
     * Детали груза, связанные с этой заявкой (отношение 1:1)
     * Загружается лениво, чтобы избежать лишних запросов при отсутствии необходимости.
     */
    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private CargoDetails cargoDetails;

    /**
     * Специальные условия перевозки, связанные с этой заявкой (отношение 1:1)
     * Загружается лениво.
     */
    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private SpecialConditions specialConditions;

    /**
     * Требования к транспортному средству для данной заявки (отношение 1:1)
     * Загружается лениво. Удаляется каскадно при удалении заявки.
     */
    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private VehicleRequirements vehicleRequirements;

    /**
     * Стоимость заявки, максимальное значение 1000000000
     */
    @Column(name = "cost_request")
    private Double costRequest;

    /**
     * Флаг включения НДС
     */
    @Column(name = "vat_include")
    private Boolean vatInclude;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RequestHistory> history;

    /**
     * Признак экспедитора у грузоотправителя.
     * Указывает, является ли отправитель (sender) экспедитором.
     * По умолчанию — false.
     */
    @Column(name = "is_sender_forwarder", nullable = false)
    private boolean senderForwarder = false;

    public RequestDto getCopyOfDraftInfoWithId() {
        var draftInfoDto = new RequestMapperImpl().copyOf(this.getDraftInfo());
        draftInfoDto.setId(this.id);

        return draftInfoDto;
    }
}