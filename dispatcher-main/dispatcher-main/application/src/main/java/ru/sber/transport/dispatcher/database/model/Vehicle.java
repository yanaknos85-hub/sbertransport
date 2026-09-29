package ru.sber.transport.dispatcher.database.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import ru.sber.transport.dispatcher.dto.enums.VehicleType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity of transport.
 */
@Entity
@Table(schema = "dispatcher", name = "vehicle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
@Builder
@SQLDelete(sql = "UPDATE dispatcher.vehicle SET active = false WHERE id = ?")
public class Vehicle {

    /**
     * ID Транспортного средства
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Автопарк
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autopark_id")
    private Autopark autopark;

    /**
     * Гос номер
     */
    @Column(name = "state_number")
    private String stateNumber;

    /**
     * Идентификационный номер транспортного средства
     */
    @Column(name = "vin")
    private String vin;

    /**
     * Марка и модель автомобиля
     */
    @Embedded
    private CarModel model;

    /**
     * Серия номер страховки
     */
    @Column(name = "insurance_number")
    private String insuranceNumber;

    /**
     * Эко класс
     */
    @Column(name = "eco_class")
    @Enumerated(EnumType.STRING)
    private EcoClass ecoClass;

    /**
     * Потребление топлива, л
     */
    @Column(name = "fuel_consumption")
    private Double fuelConsumption;

    /**
     * Комплектация
     */
    @Column(name = "package_class")
    private String packageClass;

    /**
     * Пробег автомобиля, км
     */
    @Column
    private Integer mileage;

    /**
     * Цвет
     */
    @Column
    private String color;

    /**
     * Год производства
     */
    @Column(name = "manufacture_year")
    private Integer manufactureYear;

    /**
     * Разрешенная максимальная масса, кг
     */
    @Column(name = "max_allowed_weight")
    private Integer maxAllowedWeight;

    /**
     * Тип привода
     */
    @Column(name = "chassis_type")
    private String chassisType;

    /**
     * Тип трансмиссии
     */
    @Column(name = "transmission_type")
    private String transmissionType;

    /**
     * Тип кузова
     */
    @Column(name = "body_type")
    private String bodyType;

    /**
     * Паспорт ТС
     */
    @Column(name = "passport")
    private String passport;

    /**
     * Тип двигателя
     */
    @Column(name = "engine_type")
    private String engineType;

    /**
     * В эксплуатации
     */
    @Column(name = "in_exploitation")
    private boolean inExploitation;

    @Column(name = "active")
    @Builder.Default
    private boolean active = true;

    /**
     * Тип автомобиля
     */
    @Column(name = "vehicle_type")
    @Enumerated(value = EnumType.STRING)
    private VehicleType vehicleType;

    @Column(name = "transport_id")
    private UUID transportId;

    @Column(name = "location_address")
    @Size(max = 255)
    private String locationAddress;

    @Column(name = "parking_address")
    @Size(max = 255)
    private String parkingAddress;

    /**
     * Данные для грузовых автомобилей
     */
    @Type(JsonBinaryType.class)
    @Column(name = "vehicle_additional")
    private Map<String, Object> vehicleAdditional = new HashMap<>();

    @Column
    private String type;

    @Column
    private String subtype;

    public Vehicle(UUID id, String model, String brand, String stateNumber) {
        this.id = id;
        this.stateNumber = stateNumber;
        var modelData = new CarModel();
        modelData.setName(model);
        modelData.setBrand(brand);
        this.model = modelData;
    }
}
