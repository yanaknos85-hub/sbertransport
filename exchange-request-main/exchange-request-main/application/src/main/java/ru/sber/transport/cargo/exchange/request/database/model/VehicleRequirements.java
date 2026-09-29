package ru.sber.transport.cargo.exchange.request.database.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Сущность, представляющая требования к транспорту в заявке.
 * Хранится в таблице 'vehicle_requirements' схемы 'exchange_request'.
 */
@Entity
@Table(schema = "exchange_request", name = "vehicle_requirements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequirements {

    /**
     * Уникальный идентификатор записи с требованиями к транспорту.
     * Первичный ключ.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Ссылка на заявку, к которой относятся требования.
     * Отношение 1:1 — у одной заявки одни требования к транспорту.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    /**
     * Тип загрузки: 'top', 'side', 'rear'.
     * Обязательное поле.
     */
    @Column(nullable = false, length = 50)
    private String loadType;

    /**
     * Тип выгрузки: 'top', 'side', 'rear'.
     * Обязательное поле.
     */
    @Column(nullable = false, length = 50)
    private String unloadType;

    /**
     * Требуемый объём кузова в кубометрах.
     * Ограничения: от 1 до 90 м³.
     */
    @Column(name = "capacity_m3", nullable = false, precision = 10, scale = 3)
    private BigDecimal capacityM3;

    /**
     * Требуемая грузоподъёмность в тоннах.
     * Ограничения: от 1 кг до 20 тонн (в тоннах: 0.001–20.0).
     */
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal loadCapacity;

    /**
     * Запрет на догрузку.
     * TRUE — без догрузки, FALSE — можно догружать.
     */
    @Column
    private boolean noAdditionalLoad;

    /**
     * Тип кузова (например, тент, рефрижератор и т.д.).
     * Значение из справочника vehicle_body_types.id.
     */
    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> vehicleBodyType;

    /**
     * Дополнительные опции транспорта (например, манипулятор, подогрев и т.д.).
     * Значение из справочника vehicle_extra_features.id.
     */
    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> vehicleExtraFeatures;

    /**
     * Дополнительный комментарий к требованиям.
     * Поле необязательное.
     */
    @Column(columnDefinition = "TEXT")
    private String comment;
}