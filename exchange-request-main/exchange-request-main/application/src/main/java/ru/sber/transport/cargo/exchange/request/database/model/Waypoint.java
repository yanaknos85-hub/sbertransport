package ru.sber.transport.cargo.exchange.request.database.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.util.StringUtils;
import ru.sber.transport.cargo.exchange.request.dto.AddressInfoDto;
import ru.sber.transport.cargo.exchange.request.dto.WaypointContactDto;
import ru.sber.transport.cargo.exchange.request.enums.WaypointType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Сущность, представляющая точку маршрута (Waypoint) в заявке.
 * Хранится в таблице 'waypoints' схемы 'exchange_request'.
 * Используется для описания погрузок/выгрузок: адрес, время, порядок и т.д.
 */
@Entity
@Table(schema = "exchange_request", name = "waypoint")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Waypoint {

    /**
     * Уникальный идентификатор точки маршрута.
     * Генерируется автоматически как UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Порядковый номер точки в маршруте, начиная с 0.
     * Определяет последовательность следования точек.
     */
    @Column(nullable = false)
    private Integer orderingIndex;

    /**
     * Радиус поиска в метрах вокруг точки.
     * Используется для географических проверок.
     */
    @Column
    private Integer radius;

    /**
     * Ссылка на заявку, к которой относится точка.
     * Внешний ключ на exchange_request.request.id.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    /**
     * Тип точки: 'LOAD' — погрузка, 'UNLOAD' — выгрузка.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WaypointType type;

    /**
     * JSON со сведениями об адресе.
     * Хранится как jsonb в PostgreSQL.
     * Пример структуры:
     * {
     *   "city": "Москва",
     *   "street": "улица Пушкина",
     *   "house": "9",
     *   "latitude": 55.81692751543959,
     *   "longitude": 37.64619834438934,
     *   "addressStringRepresentation": "Москва, улица Пушкина, 9"
     * }
     */
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private AddressInfoDto addressInfo;

    /**
     * Дата операции (без времени), используется совместно с полями from и to.
     */
    @Column(nullable=false)
    private LocalDate date;

    /**
     * Время начала операции (без даты)
     */
    @Column(name = "from_time", nullable = false)
    private LocalTime from;

    /**
     * Время окончания операции (без даты)
     */
    @Column(name = "to_time", nullable = false)
    private LocalTime to;

    /**
     * Контактная информация, хранится как JSON в БД
     */
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private WaypointContactDto contactInfo;

    @PrePersist
    private void prePersist() {
        if (!StringUtils.hasText(addressInfo.getAddressStringRepresentation())) {
            addressInfo.setAddressStringRepresentation(addressInfo.toFullAddressString());
        }
    }
}