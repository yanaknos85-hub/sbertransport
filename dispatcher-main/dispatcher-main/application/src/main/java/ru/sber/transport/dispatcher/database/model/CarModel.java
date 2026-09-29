package ru.sber.transport.dispatcher.database.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Модель автомобиля, уникальность по Имени, Брэнду и Году появления модели в модельном ряде
 */
@Embeddable
@NoArgsConstructor
@Getter
@Setter
public class CarModel {
    
    /** Брэнд */
    @NotBlank
    @Size(max = 128)
    @Column(nullable = false, name = "model_brand")
    private String brand;
    
    /** Наименование модели */
    @Column(name = "model_name")
    private String name;
    
    /** Год пявление модели */
    @Column(name = "model_year")
    private Integer year;
    
}
