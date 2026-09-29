package ru.sber.transport.trips.cargo.business.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Объект заявки.
 */
@Getter
@Setter
public final class CargoRequest {
    private UUID id;
    private String humanReadableId;
    private Employee author;
    private Employee sender;
    private Employee recipient;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime creationTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime approvalDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime desiredDate;
    private String timeZone;
    private String transportType;
    private Employee approvedBy;
    private UUID tariffId;
    private String status;
    private int statusCode;
    private String approvalState;
    private Map<String, Object> expected;
    private Set<String> requestOptions;
    private boolean active;
    private Double length;
    private Double width;
    private Double height;
    private Double volume;
    private Double weight;
    private Integer occupiedPlacesCount;
    private List<Map<String, Object>> cargoData;
    private String comment;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime finishedTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime transferTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime shipmentTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime timeWorkStart;
    private int sourceLoaders;
    private int destinationLoaders;
    private OffsetDateTime timeWorkFinish;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CargoRequest req && req.getId().equals(id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CargoRequest.class.getCanonicalName(), id.hashCode());
    }

}