package ru.sberbank.transport.oto.cargo.database.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.transport.oto.cargo.dto.ContractorDTO;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Table(schema = "oto_cargo", name = "routelist")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Routelist {
    /**
     * Идентификатор маршрута
     */
    @Id
    private UUID id;
    
    /**
     * Человекочитаемый идентификатор
     */
    @Column(name = "humanreadableid", updatable = false, nullable = false)
    private String humanReadableId;
    
    /**
     * JSON со сведениями о контрагенте
     */
    @Column(name = "contractor_info", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private ContractorDTO contractor;
    
    @NotNull
    @Column(name = "active")
    @Builder.Default
    private boolean active = true;
    
    @Column(name = "tariff_id")
    private UUID tariffId;
    
    /**
     * Общая стоимость
     */
    @Column
    private Long cost;
    
    /**
     * Общее расстояние
     */
    @Column
    private Double distance;
    
    @Enumerated(EnumType.STRING)
    @Column
    private TripRequestStatus status;
    
    /**
     * Код статуса
     */
    @Builder.Default
    @Column(name = "status_code")
    private Integer statusCode = 0;
}

