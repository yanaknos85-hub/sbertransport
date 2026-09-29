package ru.sber.transport.dispatcher.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShiftForEwbDto {

    private UUID id;

    private UUID dispatcherId;

    private OffsetDateTime startDate;

    private OffsetDateTime finishDate;

    private String transportationType;

    private String communicationType;

    private UUID tariffDepartmentId;

    private UUID transportId;

    private UUID driverId;

    public ShiftForEwbDto (UUID id, Timestamp startDate, Timestamp finishDate, UUID tariffDepartmentId, UUID transportId, UUID driverId){
        this.id = id;
        this.startDate = startDate.toLocalDateTime().atOffset(ZoneOffset.UTC);
        this.finishDate = finishDate.toLocalDateTime().atOffset(ZoneOffset.UTC);
        this.transportationType = "СН";
        this.communicationType = "Г";
        this.tariffDepartmentId = tariffDepartmentId;
        this.transportId = transportId;
        this.driverId = driverId;
    }

}
