package ru.sberbank.ditsib.database.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

/**
 * Сущность часто используемого адреса пользователя.
 */
@Entity
@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    /**
     * Уникальный идентификатор адреса.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Идентификатор сотрудника.
     */
    private UUID employeeId;

    /**
     * Название адреса (пользовательское поле).
     */
    private String addressName;
    /**
     * Название улицы.
     */
    private String street;
    /**
     * Номер дома.
     */
    private String house;
    /**
     * Город.
     */
    private String city;
    /**
     * Регион.
     */
    private String region;
    /**
     * Страна.
     */
    private String country;
    /**
     * Тип региона (например, область, край).
     */
    private String regionType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address address)) return false;
        return Objects.equals(id, address.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}