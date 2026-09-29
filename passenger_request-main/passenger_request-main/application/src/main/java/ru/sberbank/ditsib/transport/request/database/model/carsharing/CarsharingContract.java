package ru.sberbank.ditsib.transport.request.database.model.carsharing;

import jakarta.persistence.*;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.Set;
import java.util.UUID;

/**
 * Cущность контракта с контрагентом, получаемая из сообщения
 */
@Entity
@Table(schema = "request", name = "contract_message")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarsharingContract {
    
    /** ID контракта */
    @Id
    private UUID id;
    
    /** Вид транспортной услуги */
    @Column(name = "service_type")
    @Enumerated(EnumType.STRING)
    private TransportServiceType serviceType;
    
    /** Регион */
    @Column
    private String region;
    
    /** тип транспорта */
    @Column(name = "transport_type")
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;
    
    /** ID контрагента */
    @Column(name = "contractor_id")
    private UUID contractorId;
    
    /** Организации */
    @ElementCollection
    @CollectionTable(schema = "request", name = "contract_message_organization",
                     joinColumns = @JoinColumn(name = "contract_id"))
    @Column(name = "organization_id")
    private Set<UUID> organizations;
    
    /** Флаг активности */
    @Column
    private boolean active;
    
    /** Флаг удаления контракта */
    @Column
    private boolean deleted;
}
