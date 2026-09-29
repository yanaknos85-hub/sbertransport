package ru.sberbank.ditsib.geo_zones.providers.geo_zone.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сущность геозоны.
 */
@SQLRestriction("active=true")
@SQLDelete(sql = "UPDATE geo_zone SET active = false WHERE id = ?")
@Entity
@Table(schema = "geo_zones", name = "geo_zone")
@Getter
@Setter
@Indexed(index = "transport.geoZones")
@NoArgsConstructor
@ToString(exclude = "children")
public class GeoZone {
    
    public GeoZone(UUID id) {
        this.id = id;
    }
    
    /**
     * Идентификатор.
     */
    @Id
    @GeneratedValue
    @DocumentId
    private UUID id;
    
    /**
     * Код.
     */
    @Column(nullable = false)
    @GenericField
    private String code;
    
    /**
     * Название.
     */
    @Column(nullable = false)
    @FullTextField
    private String name;
    
    /**
     * Родитель.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    @IndexedEmbedded(includeDepth = 1)
    private GeoZone parent;

    /**
     * Временная зона.
     */
    @Column(name = "time_zone",nullable = false)
    @FullTextField
    private String timeZone;
    
    /**
     * Дети.
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @IndexedEmbedded(includeDepth = 1)
    private final List<GeoZone> children = new ArrayList<>();
    
    @PostPersist
    private void linkParent() {
        if (this.parent != null && !this.parent.getChildren().contains(this)) {
            this.parent.getChildren().add(this);
        }
    }
    
    @PreRemove
    private void unlinkParent() {
        if (this.parent != null) {
            this.parent.getChildren().remove(this);
            this.parent = null;
        }
    }
    
}
