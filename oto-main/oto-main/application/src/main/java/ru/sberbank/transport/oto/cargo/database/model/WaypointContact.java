package ru.sberbank.transport.oto.cargo.database.model;

import lombok.*;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = "oto_cargo", name = "waypoint_contact")
@Getter
@Setter
@ToString(exclude = "waypoint")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class WaypointContact {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    /**
     * Связанная точка, в которой используется груз
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waypoint_id")
    private Waypoint waypoint;


    @Column(name = "mobile_phone")
    private String mobilePhone;

    @Column(name = "fullname")
    private String fullname;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;
}
