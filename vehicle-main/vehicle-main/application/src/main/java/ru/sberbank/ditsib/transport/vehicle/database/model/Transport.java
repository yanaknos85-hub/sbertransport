package ru.sberbank.ditsib.transport.vehicle.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.lang.Nullable;
import ru.sberbank.ditsib.transport.vehicle.constants.TransportStatus;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Table(name = "transport")
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(of = {"id", "inventoryNumber", "assetNumber"})
@EqualsAndHashCode(of = "id")

@NamedEntityGraph(name = "whole-transport", attributeNodes = {
        @NamedAttributeNode(value = Transport_.VEHICLE, subgraph = Transport_.VEHICLE),
        @NamedAttributeNode(value = Transport_.SUBTYPE, subgraph = Transport_.SUBTYPE),
        @NamedAttributeNode(value = Transport_.ORGANIZATIONS, subgraph = Transport_.ORGANIZATIONS),
        @NamedAttributeNode(value = Transport_.DEPARTMENTS, subgraph = Transport_.DEPARTMENTS),
        @NamedAttributeNode(value = Transport_.TELEMATICS, subgraph = Transport_.TELEMATICS)

},
        subgraphs = {
                @NamedSubgraph(name = Transport_.VEHICLE, attributeNodes = {
                        @NamedAttributeNode(value = Vehicle_.FUEL_TYPES, subgraph = Vehicle_.FUEL_TYPES),
                        @NamedAttributeNode(value = Vehicle_.MODEL, subgraph = Vehicle_.MODEL),
                        @NamedAttributeNode(value = Vehicle_.DRIVE, subgraph = Vehicle_.DRIVE),
                        @NamedAttributeNode(value = Vehicle_.CATEGORY, subgraph = Vehicle_.CATEGORY),
                        @NamedAttributeNode(value = Vehicle_.BODY_TYPE),
                        @NamedAttributeNode(value = Vehicle_.TRANSMISSION_TYPE),
                        @NamedAttributeNode(value = Vehicle_.FRONT_WHEEL_SIZE),
                        @NamedAttributeNode(value = Vehicle_.REAR_WHEEL_SIZE),
                        @NamedAttributeNode(value = Vehicle_.ENGINE_TYPE, subgraph = Vehicle_.ENGINE_TYPE)

                }),
                @NamedSubgraph(name = Vehicle_.FUEL_TYPES, attributeNodes = {
                        @NamedAttributeNode(FuelType_.ENGINE_TYPE)
                }),
                @NamedSubgraph(name = Vehicle_.ENGINE_TYPE, attributeNodes = {
                        @NamedAttributeNode(EngineType_.TITLE)
                }),
                @NamedSubgraph(name = Vehicle_.MODEL, attributeNodes = {
                        @NamedAttributeNode(Model_.BRAND)
                }),
                @NamedSubgraph(name = Transport_.SUBTYPE, attributeNodes = {
                        @NamedAttributeNode(Subtype_.TYPE)
                }),
                @NamedSubgraph(name = Transport_.ORGANIZATIONS, attributeNodes = {
                        @NamedAttributeNode(Organization_.ID),
                        @NamedAttributeNode(Organization_.ACTIVE)
                }),
                @NamedSubgraph(name = Transport_.DEPARTMENTS, attributeNodes = {
                        @NamedAttributeNode(Department_.ID),
                        @NamedAttributeNode(Department_.DEPARTMENT_NAME)
                })
})
public class Transport {
    @Id
    @GeneratedValue
    private UUID id;

    @Column
    @Size(min = 1, max = 50)
    private String inventoryNumber;

    @Column
    @Size(min = 1, max = 50)
    @NotBlank
    private String assetNumber;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name="transport_organization",
            joinColumns = @JoinColumn(name="transport_id"),
            inverseJoinColumns = @JoinColumn(name="organization_id")
    )
    private Set<Organization> organizations = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name="transport_department",
            joinColumns = @JoinColumn(name="transport_id"),
            inverseJoinColumns = @JoinColumn(name="department_id")
    )
    private Set<Department> departments = new HashSet<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TransportStatus status = TransportStatus.IN_USE;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subtype_id")
    private Subtype subtype;

    @NotNull
    @Pattern(regexp = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$")
    private String stateNumber;

    @NotNull
    @Positive
    private Integer year;

    @NotEmpty
    @Size(min = 1, max = 17)
    private String vinCode;

    @Size(min = 1, max = 17)
    private String chassisNumber;

    @Size(min = 1, max = 17)
    private String bodyNumber;

    @NotEmpty
    @Size(min = 1, max = 15)
    private String certificateNumber;

    @NotNull
    private LocalDate certificateIssuedDate;

    @NotEmpty
    @Size(min = 1, max = 15)
    private String passportNumber;

    @NotNull
    private LocalDate passportIssuedDate;

    @NotEmpty
    @Size(min = 1, max = 150)
    private String brandByPassport;

    @NotEmpty
    @Size(min = 1, max = 150)
    private String modelByPassport;

    @NotBlank
    @Size(max = 150)
    private String vehicleType;

    @NotEmpty
    @Size(min = 1, max = 50)
    private String bodyColor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "telematics_id")
    private Telematics telematics;

    @NotNull
    private LocalDate exploitationStart;

    private LocalDate exploitationEnd;

    @PositiveOrZero
    @NotNull
    @Max(999999)
    private Integer currentMileage;

    @NotBlank
    @Size(max = 255)
    private String locationAddress;
    
    @NotBlank
    @Size(max = 255)
    private String parkingAddress;
    
    @Size(max = 255)
    private String comment;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accessible_position_id", referencedColumnName = "id")
    private AccessiblePosition accessiblePosition;

    @Nullable
    @Column
    private UUID contractorId;

    @Nullable
    @Column
    private UUID autoparkId;

    /**
     * Балансовая единица
     */
    @NotBlank
    @Size(max = 4)
    @Column(length = 4)
    private String balanceUnitNumber;

    /**
     * Завод
     */
    @NotBlank
    @Size(max = 4)
    @Column(length = 4)
    private String facility;

    /**
     * Единица оборудования
     */
    @NotBlank
    @Size(max = 30)
    @Column(length = 30)
    private String equipmentUnitSystemNumber;

}