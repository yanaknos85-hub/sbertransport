package ru.sberbank.ditsib.transport.reports.model.taxiTrip;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Builder
@Data
public class CarInfo {
    /**
     * Марка ьс
     */
    @Column(name = "car_brand_name")
    @Schema(description = "Марка")
    String brandName;

    /**
     * Модель тс
     */
    @Column(name = "car_model")
    String model;

    /**
     * Цвет тс
     */
    @Column(name = "car_color")
    String color;

    /**
     * Государственный номер
     */
    @Column(name = "car_registration_number")
    String registrationNumber;
}
