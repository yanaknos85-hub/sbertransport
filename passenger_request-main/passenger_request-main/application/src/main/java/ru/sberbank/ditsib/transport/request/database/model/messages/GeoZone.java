package ru.sberbank.ditsib.transport.request.database.model.messages;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Table(schema = "request", name = "messages_geo_zone")
@Entity
public class GeoZone {
    
    @Id
    private UUID id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "code", columnDefinition = "int8 (Types#BIGINT)")
    private String code;
    
    @Column(name = "parent_id")
    private UUID parentId;
    
}
