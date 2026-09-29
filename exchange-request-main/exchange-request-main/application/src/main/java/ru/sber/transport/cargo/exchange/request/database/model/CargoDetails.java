package ru.sber.transport.cargo.exchange.request.database.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Сущность, представляющая данные о грузе в заявке.
 * Хранится в таблице 'cargo_details' схемы 'exchange_request'.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "exchange_request", name = "cargo_details")
public class CargoDetails {

    /**
     * Уникальный идентификатор записи о грузе.
     * Первичный ключ.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Ссылка на заявку, к которой относится груз.
     * Внешний ключ на exchange_request.request.id.
     * Отношение 1:1 — у одной заявки один набор данных о грузе.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    /**
     * Вес груза в килограммах. Должен быть больше 0.
     * Ограничения: min=1, max=20000 кг.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weightKg;

    /**
     * Объём груза в кубометрах. Должен быть больше 0.
     * Ограничения: min=1, max=90 м³.
     */
    @Column(name = "volume_m3", nullable = false, precision = 10, scale = 3)
    private BigDecimal volumeM3;

    /**
     * Объявленная стоимость груза в рублях.
     * Максимальное значение: 1 000 000 000 руб.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal declaredValue;

    /**
     * Длина груза в метрах.
     * Ограничения: min=2, max=13 м.
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal length;

    /**
     * Ширина груза в метрах.
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal width;

    /**
     * Высота груза в метрах.
     */
    @Column( precision = 5, scale = 2)
    private BigDecimal height;

    /**
     * Тип груза (ссылка на справочник cargo_type.id).
     * Значение выбирается из справочника.
     */
    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> cargoType;

    /**
     * Тип упаковки (ссылка на справочник packages_type.id).
     * Значение выбирается из справочника.
     */
    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> cargoPackage;

    /**
     * Метод определения массы груза.     *
     * Значение выбирается из справочника.
     */
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> methodDeterminingMass;

    /**
     * Количество грузовых мест. Должно быть больше 0.
     */
    @Column
    private Integer occupiedPlacesCount = 1;

    /**
     * Вид тары (например, коробка, паллета, контейнер и т.д.).
     * Значение выбирается из справочника.
     */
    @Column(name = "type_of_container", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> typeOfContainer = List.of("00");

}