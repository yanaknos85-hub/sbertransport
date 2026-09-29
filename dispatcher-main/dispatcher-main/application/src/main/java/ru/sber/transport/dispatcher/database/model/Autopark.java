package ru.sber.transport.dispatcher.database.model;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Entity of autopark.
 */
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Setter
@Builder(toBuilder = true)
@Table(schema = "dispatcher", name = "autopark")
@SQLDelete(sql = "UPDATE dispatcher.autopark SET active = false WHERE id = ?")
public class Autopark {

    /**
     * Идентификатор автопарка
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Название автопарка
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Контрагент
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contractor_id", nullable = false)
    private Contractor contractor;

    @Builder.Default
    @OneToMany(mappedBy = "autopark", cascade = {CascadeType.DETACH, CascadeType.REMOVE}, fetch = FetchType.LAZY)
    private Collection<Vehicle> vehicles = new ArrayList<>();

    @Column(name = "active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "routing_id")
    private UUID routingId;
    /**
     * Нормативное количество автомобилей
     */
    @Column(name = "vehicle_count_norm")
    private Integer vehicleCountNorm;
}
