package ru.sber.transport.cargo.exchange.request.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Сущность, представляющая особые условия перевозки груза.
 * Хранится в таблице 'special_condition' схемы 'exchange_request'.
 * Включает информацию об опасных, температурных и негабаритных грузах.
 */
@Entity
@Table(schema = "exchange_request", name = "special_conditions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialConditions {

    /**
     * Уникальный идентификатор записи с особыми условиями.
     * Первичный ключ.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Ссылка на заявку, к которой относятся особые условия.
     * Отношение 1:1 — у одной заявки один набор особых условий.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    /**
     * Признак, что груз является опасным.
     * TRUE — опасный груз, FALSE или NULL — нет.
     */
    @Column
    private Boolean isDangerous;

    /**
     * Класс опасности (от 1 до 9).
     * Заполняется только если is_dangerous = TRUE.
     */
    @Column(length = 10)
    private String dangerousClass;

    /**
     * Признак необходимости контроля температуры.
     * TRUE — требуется поддержание температурного режима.
     */
    @Column
    private Boolean hasTemperature;

    /**
     * Минимальная допустимая температура хранения/перевозки (в °C).
     * Диапазон: от -200°C до +50°C.
     * Заполняется только если has_temperature = TRUE.
     */
    @Column
    private Short tempMin;

    /**
     * Максимальная допустимая температура хранения/перевозки (в °C).
     * Диапазон: от -200°C до +50°C.
     * Заполняется только если has_temperature = TRUE.
     */
    @Column
    private Short tempMax;

    /**
     * Признак, что груз является негабаритным.
     * TRUE — негабаритный груз.
     */
    @Column
    private Boolean isOversized;


    /**
     * Дополнительные условия перевозки в текстовом виде.
     * Поле необязательное.
     */
    @Column(columnDefinition = "TEXT")
    private String otherConditions;
}
