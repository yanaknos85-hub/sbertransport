package ru.sberbank.ditsib.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.sberbank.ditsib.enumerate.LeadStatus;
import ru.sberbank.ditsib.enumerate.TransportClass;
import ru.sberbank.ditsib.enumerate.TransportType;
import ru.sberbank.ditsib.enumerate.TripType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Сущность пользовательской заявки на транспорт.
 * Содержит информацию о поездке,
 * типе транспорта, точках маршрута.
 */
@Entity
@Table(name = "lead")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    /**
     * Уникальный идентификатор заявки.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Сотрудник.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /**
     * Идентификатор главной заявки.
     */
    private UUID mainLeadId;


    /**
     * Комментарий к заявке.
     */
    @Column
    private String comment;

    /**
     * Время отправления.
     */
    @NotNull
    private LocalDateTime departureTime;

    /**
     * Стоимость
     */
    private Integer cost;

    /**
     * Является ли пользователь водителем.
     */
    private boolean isDriver;

    /**
     * Дата и время создания заявки.
     */
    @NotNull
    @CreationTimestamp
    private LocalDateTime createDateTime;

    /**
     * Статус заявки.
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    private LeadStatus status;

    /**
     * Тип поездки.
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    private TripType tripType;

    /**
     * Тип транспорта.
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    private TransportType transportType;

    /**
     * Тип транспорта.
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    private TransportClass transportClass;

    /**
     * Список точек маршрута.
     */
    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PointLead> points;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lead lead)) return false;
        return Objects.equals(id, lead.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
