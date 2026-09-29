package ru.sberbank.ditsib.transport.request.database.model.carsharing;

import jakarta.persistence.*;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;

import java.util.UUID;

/**
 * Cущность контрагента, получаемая из сообщения
 */
@Entity
@Table(schema = "request", name = "contractor_message")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contractor {
    
    /**
     * ID контрагента
     */
    @Id
    private UUID id;
    
    /**
     * Наименование контрагента
     */
    @Column
    private String name;
    
    /**
     * ОГРН
     */
    @Column
    private String msrn;
    
    /**
     * ИНН
     */
    @Column
    private String tin;
    
    /**
     * Название контрагента латиницей (указывается при интеграции в имени файла)
     */
    @Column(name = "contractor_name")
    private String contractorName;
    
    /**
     * Название контрагента кириллицей (указывается при интеграции в поле ИСПОЛНИТЕЛЬ)
     */
    @Column(name = "contractor_rus_name")
    private String contractorRusName;
    
    /**
     * Интеграционный email контрагента
     */
    @Column(name = "integration_email")
    private String integrationEmail;
    
    /**
     * Флаг удаления
     */
    @Column
    private boolean deleted;


    @Column(name = "url")
    private String url;

    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "integration_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TaxiExternalIntegrationType integrationType;
}
