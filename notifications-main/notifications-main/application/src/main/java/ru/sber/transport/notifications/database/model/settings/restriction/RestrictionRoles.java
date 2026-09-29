package ru.sber.transport.notifications.database.model.settings.restriction;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(schema = "notifications_settings", name = "restriction_roles")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "restrictionSettings")
public class RestrictionRoles {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_code", nullable = false)
    @NotNull
    private Role role;
    
    public RestrictionRoles(Role role) {
        this.setRole(role);
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restriction_id", nullable = false)
    private RestrictionSettings restrictionSettings;

    public RestrictionRoles(RestrictionRoles restrictionRoles) {
        this(null, restrictionRoles.getRole(), new RestrictionSettings(restrictionRoles.getRestrictionSettings()));
    }
}
