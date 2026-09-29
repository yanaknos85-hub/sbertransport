package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NaturalId;
import org.hibernate.type.SqlTypes;
import ru.sber.transport.telemechanic.enumerate.EwbCommunicationType;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.enumerate.EwbTransportationSubtype;
import ru.sber.transport.telemechanic.enumerate.EwbTransportationType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ewb")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "ewbUuid")
@Accessors(chain = true)
@NamedEntityGraphs({
        @NamedEntityGraph(name = "search-ewb", attributeNodes = {
                @NamedAttributeNode(value = Ewb_.AUTHOR),
                @NamedAttributeNode(value = Ewb_.MEDIC, subgraph = Ewb_.MEDIC),
                @NamedAttributeNode(value = Ewb_.MEDIC_CONTRACTOR),
                @NamedAttributeNode(value = Ewb_.TRANSPORT),
                @NamedAttributeNode(value = Ewb_.ORGANIZATION),
                @NamedAttributeNode(value = Ewb_.REQUEST),
                @NamedAttributeNode(value = Ewb_.MEDIC_REQUEST),
                @NamedAttributeNode(value = Ewb_.DRIVER, subgraph = Ewb_.DRIVER),
                @NamedAttributeNode(value = Ewb_.TELEMECH_OUT),
                @NamedAttributeNode(value = Ewb_.TELEMECH_IN)
        }, subgraphs = {
                @NamedSubgraph(name = Ewb_.AUTHOR, attributeNodes = {
                        @NamedAttributeNode(value = Employee_.ORGANIZATION),
                        @NamedAttributeNode(value = Employee_.DEPARTMENT),
                        @NamedAttributeNode(value = Employee_.POSITION)
                }),
                @NamedSubgraph(name = Ewb_.REQUEST, attributeNodes = {
                        @NamedAttributeNode(value = Request_.CHECKS),
                        @NamedAttributeNode(value = Request_.AUTHOR)
                }),
                @NamedSubgraph(name = Ewb_.DRIVER, attributeNodes = {
                        @NamedAttributeNode(value = Driver_.EMPLOYEE, subgraph = Ewb_.AUTHOR)
                }),
                @NamedSubgraph(name = Ewb_.MEDIC, attributeNodes = {
                        @NamedAttributeNode(value = Employee_.ORGANIZATION),
                        @NamedAttributeNode(value = Employee_.POSITION)
                })
        }),
        @NamedEntityGraph(name = "on-the-line", attributeNodes = {
                @NamedAttributeNode(value = Ewb_.REQUEST)
        })
})
public class Ewb {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private Employee author;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medic_id", referencedColumnName = "id")
    private Employee medic;
    
    private UUID organizationMedicalLicenseId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medic_request_id", referencedColumnName = "id")
    private MedicRequest medicRequest;
    
    private LocalDateTime medicDecisionTime;
    
    @OneToOne(fetch = FetchType.LAZY)
    private Request request;
    
    private LocalDateTime creationTime;
    
    @Enumerated(EnumType.STRING)
    private EwbStatus status;
    
    @NotBlank
    private String humanReadableId;
    
    @NaturalId
    @NotNull
    private UUID ewbUuid;
    
    private LocalDate startDate;
    
    private LocalDate finishDate;
    
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private EwbTransportationType transportationType;
    
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private EwbTransportationSubtype transportationSubtype;
    
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private EwbCommunicationType communicationType;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private Organization organization;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transport_id", referencedColumnName = "id")
    private Transport transport;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", referencedColumnName = "id")
    private Driver driver;
    
    private UUID driverLicenseId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "telemech_out_id", referencedColumnName = "id")
    private Employee telemechOut;
    
    private LocalDateTime telemechDecisionOut;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "telemech_in_id", referencedColumnName = "id")
    private Employee telemechIn;
    
    private LocalDateTime telemechDecisionIn;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attorney_out_id", referencedColumnName = "id")
    private Attorney attorneyOutId;
    
    private Integer odometerOut;
    
    private Integer odometerIn;
    
    private boolean qrCode;
    
    private UUID tariffDepartmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medic_contractor_id", referencedColumnName = "id")
    private MedicContractor medicContractor;

    private Integer fuelLitreageIn;

    private Integer fuelLitreageOut;
    
    @Column(nullable = false, length = 9)
    private String timeZone;
    
}
