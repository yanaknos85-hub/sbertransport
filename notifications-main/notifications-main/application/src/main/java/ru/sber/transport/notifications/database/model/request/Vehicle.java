package ru.sber.transport.notifications.database.model.request;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Модель данных ТС.
 */
@Entity
@Table(schema = "notifications_contractor", name = "vehicle")
@Setter
@Getter
public class Vehicle {

    @Id
    private UUID id;

    /**
     * Марка.
     */
    @Column
    private String brand;

    /**
     * Модель.
     */
    @Column
    private String model;

    /**
     * Цвет.
     */
    @Column
    private String color;

    /**
     * Госномер.
     */
    @Column
    private String registrationNumber;

    /**
     * Проверка содержимого на пустоту.
     *
     * @return <code>true</code> если все поля в значении <code>null</code>.
     */
    public boolean empty() {
        return brand == null && model == null && color == null && registrationNumber == null;
    }

}
