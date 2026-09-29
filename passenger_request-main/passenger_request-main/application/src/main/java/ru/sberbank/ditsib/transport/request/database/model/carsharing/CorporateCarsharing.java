package ru.sberbank.ditsib.transport.request.database.model.carsharing;

import jakarta.persistence.*;
import lombok.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Программно генерируемая сущность Корпоративного каршеринга. Таблица пополняется автоматически по приходу сообщений
 * о контрактах с контрагентами
 */
@Entity
@Table(schema = "request", name = "corporate_carsharing")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CorporateCarsharing {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    /** Конкретный Корп.клиент для каршеринга. Берется из контракта */
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
    
    /** Контракт с Контрагентом. Может содержать UUID нескольких корп.клиентов */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private CarsharingContract contract;
    
    /** Подключенные к каршерингу пользователи */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "corporate_carsharing_joined_employee", schema = "request",
               joinColumns = @JoinColumn(name = "corporate_carsharing_id", nullable = false),
               inverseJoinColumns = @JoinColumn(name = "employee_id", nullable = false))
    @Builder.Default
    private Set<Employee> joinedEmployees = new HashSet<>();
    
    /** Флаг активности */
    @Builder.Default
    @Column
    private boolean active = true;
}
