package ru.sberbank.ditsib.transport.tariff.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Table(schema = "tariff", name = "organization")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Organization {

    @Id
    @Setter
    private UUID id;

    @NotNull
    @Column(columnDefinition = "numeric")
    private Long digitId;

    /**
     * Флаг активности(false - удален, true - активен)
     */
    @Builder.Default
    @Column
    private boolean active = true;

    @NotNull
    private String name;
}
