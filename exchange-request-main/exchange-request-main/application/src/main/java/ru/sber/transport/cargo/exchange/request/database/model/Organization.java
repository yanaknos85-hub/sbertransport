package ru.sber.transport.cargo.exchange.request.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "organization", schema = "exchange_request")
public class Organization {

    /**
     * Уникальный идентификатор организации (автоматически генерируемый UUID)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Полное наименование организации (до 500 символов)
     */
    @Column(length = 500, nullable = false)
    private String name;

    /**
     * Идентификационный номер налогоплательщика (ИНН), 10 или 12 цифр
     */
    @Column(length = 12, nullable = false)
    private String inn;

    /**
     * Код причины постановки на учёт (КПП), может отсутствовать
     */
    @Column(length = 9)
    private String kpp;

    /**
     * Юридический адрес организации (до 500 символов), может отсутствовать
     */
    @Column(length = 500)
    private String legalAddress;

    /**
     * Расчетный счёт организации в банке (до 20 символов), может отсутствовать
     */
    @Column(length = 20)
    private String bankAccount;

    /**
     * Наименование банка (до 200 символов), может отсутствовать
     */
    @Column(length = 200)
    private String bankName;

    /**
     * Банковский идентификационный код (БИК), 9 цифр, может отсутствовать
     */
    @Column(length = 9)
    private String bic;

    /**
     * Контактный номер телефона
     */
    @Column(length = 20)
    private String contactPhone;
}
