package ru.sberbank.ditsib.database.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sberbank.ditsib.enumerate.PointType;

import java.util.UUID;

/**
 * Точка маршрута заявки.
 * Представляет собой географическую точку в маршруте заявки
 * с координатами и порядковым номером.
 */
@Entity
@Table(name = "point_lead")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PointLead {

    /**
     * Уникальный идентификатор точки маршрута.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Заявка, к которой относится точка маршрута.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    /**
     * Главная заявка, к которой относится точка маршрута.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_lead_id")
    private MainLead mainLead;

    /**
     * Тип точки маршрута (например, "start", "end", "waypoint").
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PointType typePoint;

    /**
     * Долгота географической точки.
     */
    @Column(nullable = false)
    private Double longitude;

    /**
     * Широта географической точки.
     */
    @Column(nullable = false)
    private Double latitude;

    /**
     * Описание точки маршрута (адрес или название места).
     */
    @Column(nullable = false)
    private String waypoint;

    /**
     * Порядковый номер точки в маршруте.
     */
    @Column(nullable = false, columnDefinition = "numeric")
    private Integer pointNumber;
}
