package ru.sberbank.ditsib.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.sberbank.ditsib.enumerate.MainLeadStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Главная заявка.
 * Представляет собой основную заявку, которая может содержать
 * несколько пользовательских заявок.
 */
@Entity
@Table(name = "main_lead")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MainLead {

    /**
     * Уникальный идентификатор главной заявки, приходит в ответе от модели в поле groupId.
     */
    @Id
    private UUID id;

    /**
     * Статус главной заявки.
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    private MainLeadStatus status;

    /**
     * Дата и время создания главной заявки.
     */
    @NotNull
    private LocalDateTime createDateTime;

    /**
     * Флаг, указывающий является ли это заявкой.
     */
    private boolean isApplication;
}
