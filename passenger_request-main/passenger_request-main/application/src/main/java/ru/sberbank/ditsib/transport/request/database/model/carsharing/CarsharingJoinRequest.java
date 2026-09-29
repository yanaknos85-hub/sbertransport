package ru.sberbank.ditsib.transport.request.database.model.carsharing;

import jakarta.persistence.*;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Заявка на подключение к корп.каршерингу
 */
@Entity
@Table(schema = "request", name = "carsharing_join_request")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarsharingJoinRequest {
    
    /** ID заявки на подключение */
    @Id
    @GeneratedValue
    private UUID id;
    
    /** Человекочитаемый ID */
    @Column(name = "human_readable_id", nullable = false)
    private String humanReadableId;
    
    /** Статус заявки */
    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false)
    @Builder.Default
    private CarsharingJoinRequestStatus requestStatus = CarsharingJoinRequestStatus.UNDER_CONSIDERATION;
    
    /** Код статуса */
    @Column(name="status_code")
    private Integer statusCode;
    
    /**
     * Причина отмены заявки
     */
    @Column(name="cancel_reason")
    private String cancelReason;
    
    /** Подключаемый сотудник */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    /** Номер телефона, с которым сотрудник зарегистрирован в каршеринговом сервисе */
    @Column(nullable = false)
    private String phone;
    
    /** Почта, с которой сотрудник зарегистрирован в каршеринговом сервисе */
    @Column(nullable = false)
    private String email;
    
    /** Подключаемые корп.каршеринги со статусами подключения */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "carsharing_join_request_and_contractor_status", schema = "request",
               inverseJoinColumns = {@JoinColumn(name = "contractor_status_id", referencedColumnName = "id")},
               joinColumns = {@JoinColumn(name = "join_request_id", referencedColumnName = "id")})
    @Builder.Default
    private Set<ContractorAndJoinStatus> contractors = new HashSet<>();
    
    /** Согласие с правилами Памятка П-144 */
    @Column(name = "rules_p144_agree", nullable = false)
    @Builder.Default
    private boolean rulesP144Agree = false;
    
    /** Согласие на обработку персональных данных */
    @Column(name = "personal_data_agree", nullable = false)
    @Builder.Default
    private boolean personalDataAgree = false;
    
    /** Текстовые поля заявки. Нельзя изменять при работе с заявкой, только через спец. контроллер */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "text_id")
    private CarsharingJoinRequestText text;
    
    /** Дата и время создания заявки */
    @Column(name = "creation_time", updatable = false, nullable = false)
    private LocalDateTime creationTime;
}
