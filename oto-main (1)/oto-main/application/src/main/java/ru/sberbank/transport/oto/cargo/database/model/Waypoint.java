package ru.sberbank.transport.oto.cargo.database.model;

import lombok.*;

import jakarta.persistence.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Модель точки пути
 */
@Entity
@Table(schema = "oto_cargo", name = "waypoint")
@Getter
@Setter
@ToString(exclude = "request")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Waypoint {

    public Waypoint(UUID id){
        this.id=id;
    }
    /**
     * Идентификатор
     */
    @Id
    @Column(name = "id")
    private UUID id;

    /**
     * Адрес
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "address_id")
    private Address address;

    /**
     * Связанная заявка, в которой задана данная точка
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    /**
     * Время ожидания
     */
    @Column(name = "wait_time", columnDefinition = "int8 (Types#BIGINT")
    private Duration waitTime;
    
    /**
     * Порядок остановок
     */
    @Column(name = "ordering_index")
    private Integer orderingIndex;

    /**
     * Автоматический чекин
     */
    @Column(name = "checkin_automatic")
    private boolean checkinAutomatic = false;

    /**
     * Ручной чекин
     */
    @Column(name = "checkin_manual")
    private boolean checkinManual = false;
    
    @Builder.Default
    @OneToMany(mappedBy = "waypoint", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    private List<WaypointContact> contacts = new ArrayList<>();
    
    /**
     * Организация
     */
    @Column(name = "organization")
    private String organization;
    
    @PreRemove
    private void tearDown() {
        this.request = null;
        this.address = null;
    }
}
