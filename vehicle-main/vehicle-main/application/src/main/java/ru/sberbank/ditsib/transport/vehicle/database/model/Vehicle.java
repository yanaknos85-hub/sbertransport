package ru.sberbank.ditsib.transport.vehicle.database.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Автомобиль
 */
@Table(name = "vehicle")
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(of = {"id"})
@EqualsAndHashCode(of = "id")
@NamedEntityGraph(name = "vehicle-search", attributeNodes = {
        @NamedAttributeNode(value = Vehicle_.MODEL, subgraph = Vehicle_.MODEL),
        @NamedAttributeNode(Vehicle_.DRIVE),
        @NamedAttributeNode(value = Vehicle_.FUEL_TYPES, subgraph = Vehicle_.FUEL_TYPES),
        @NamedAttributeNode(value = Vehicle_.CATEGORY),
        @NamedAttributeNode(value = Vehicle_.BODY_TYPE),
        @NamedAttributeNode(value = Vehicle_.TRANSMISSION_TYPE),
        @NamedAttributeNode(value = Vehicle_.ENGINE_TYPE)
}, subgraphs = {
        @NamedSubgraph(name = Vehicle_.MODEL, attributeNodes = {
                @NamedAttributeNode(Model_.BRAND)
        }),
        @NamedSubgraph(name = Vehicle_.FUEL_TYPES, attributeNodes = {
                @NamedAttributeNode(FuelType_.ENGINE_TYPE)
        })
})
public class Vehicle {
    /**
     * ID ТС
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Модель ТС
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "model_id", referencedColumnName = "id", nullable = false)
    private Model model;

    /**
     * Категория ТС
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
    private Category category;

    /**
     * Организация изготовитель (страна)
     */
    @NotBlank
    @Size(min = 1, max = 50)
    private String manufacturer;

    /**
     * Экологический класс
     */
    @NotBlank
    @Pattern(regexp = "\\d")
    @Size(min = 1, max = 1)
    private String ecologicalClass;

    /**
     * Мощность ЛС
     */
    @Positive
    @Digits(integer = 4, fraction = 2)
    private BigDecimal enginePower;

    /**
     * Объем двигателя
     */
    @Positive
    @Max(99999)
    private int engineCapacity;

    /**
     * Объем топливного бака
     */
    @Positive
    @Max(999999)
    private int fuelTankVolume;

    /**
     * Типы топлива
     */
    @ManyToMany
    @JoinTable(
            name = "vehicle_fuel_type",
            joinColumns = @JoinColumn(name = "vehicle_id"),
            inverseJoinColumns = @JoinColumn(name = "fuel_type_id")
    )
    private Set<FuelType> fuelTypes = new HashSet<>();

    /**
     * Тип двигателя
     */
    @ManyToOne(optional = false)
    @JoinColumn(name ="engine_type_id", referencedColumnName = "id", nullable = false)
    private EngineType engineType;

    /**
     * Привод
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "drive_id", referencedColumnName = "id", nullable = false)
    private Drive drive;

    /**
     * Тип кузова
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "body_type_id", referencedColumnName = "id", nullable = false)
    private BodyType bodyType;

    /**
     * Тип трансмиссии
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "transmission_type_id", referencedColumnName = "id", nullable = false)
    private TransmissionType transmissionType;

    /**
     * Размер переднего колеса
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "front_wheel_size_id", referencedColumnName = "id", nullable = false)
    private WheelSize frontWheelSize;

    /**
     * Размер заднего колеса
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "rear_wheel_size_id", referencedColumnName = "id", nullable = false)
    private WheelSize rearWheelSize;

    /**
     * Наличие брызговиков
     */
    private boolean mudguardInstalled = false;

    /**
     * Держать запасного колеса
     */
    private boolean spareWheelHolderInstalled = false;

    /**
     * Масса без нагрузки
     */
    @Positive
    @Max(999999)
    private int weight;

    /**
     * Макс снаряженная масса
     */
    @Positive
    @Max(999999)
    private int maxWeight;

    /**
     * Высота, мм
     */
    @Positive
    @Max(99999)
    private int height;

    /**
     * Ширина, мм
     */
    @Positive
    @Max(99999)
    private int width;

    /**
     * Длина, мм
     */
    @Positive
    @Max(99999)
    private int length;

    /**
     * Межсервисный интервал по времени
     * */
    @Positive
    @Max(999)
    private int serviceIntervalDays;

    /**
     * Допуск по времени
     * */
    @Positive
    @Max(9999)
    private int serviceAuthorizationDays;

    /**
     * Межсервисный интервал по пробегу
     * */
    @Positive
    @Max(99999)
    private int serviceIntervalMileage;

    /**
     * Допуск по пробегу
     */
    @Positive
    @Max(9999)
    private int serviceAuthorizationMileage;

    /**
     * Год начала производства
     */
    @Positive
    @Max(9999)
    @Min(1900)
    private int yearManufactureBegin;

    /**
     * Год снятия с производства
     */
    @Positive
    @Max(9999)
    @Min(1900)
    private Integer yearManufactureEnd;

    /**
     * Городской расход
     */
    @Min(1)
    @DecimalMax(value = "999.99")
    @Nullable
    private BigDecimal cityConsumptionRate;

    /**
     * Загородный расход
     */
    @Min(1)
    @DecimalMax(value = "999.99")
    @Nullable
    private BigDecimal countryConsumptionRate;

    /**
     * Смешанный расход
     */
    @Min(1)
    @DecimalMax(value = "999.99")
    @Nullable
    private BigDecimal hybridConsumptionRate;

}
