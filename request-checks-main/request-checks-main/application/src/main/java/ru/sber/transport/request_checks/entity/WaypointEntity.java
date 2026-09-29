package ru.sber.transport.request_checks.entity;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Сущность путевой точки
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WaypointEntity implements Serializable {

    @EqualsAndHashCode.Include
    private UUID id;

    private UUID tripRequestId;

    private Integer orderingIndex;

    private Double latitude;

    private Double longitude;

}
