package ru.sber.transport.request_checks.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Сущность заявки на поездку
 */
@Builder
@NoArgsConstructor
@Getter
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TripRequestEntity implements Serializable {

    @EqualsAndHashCode.Include
    private UUID id;

    private String humanReadableId;

    private String transportType;

    private UUID passengerId;

    private OffsetDateTime desiredDate;

    private String timeZone;

    private Integer distance;

    private Long duration;

    private String status;

}
