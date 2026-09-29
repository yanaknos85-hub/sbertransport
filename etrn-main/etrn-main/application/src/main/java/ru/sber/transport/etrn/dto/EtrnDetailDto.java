package ru.sber.transport.etrn.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.sber.transport.etrn.config.converters.IsoLocalDateTimeSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EtrnDetailDto(
        UUID id,
        String humanReadableId,
        String applicationNumber,
        String routeNumber,
        String status,
        String sla,
        String timeZone,
        String currentTitle,
        String senderName,
        String receiverName,
        String carrierName,
        String cargoDescription,
        Integer cargoPlaces,
        BigDecimal cargoWeightKg,
        String route,
        LocalDate mrpaExpiresAt,
        BigDecimal cargoLength,
        BigDecimal cargoWidth,
        BigDecimal cargoHeight,
        String sesFullName,
        String sesRole,
        @JsonSerialize(using = IsoLocalDateTimeSerializer.class)
        LocalDateTime sesEventDatetime,
        String sesEventId,
        List<TitleEntryDto> titleChain,
        VerificationsDto verifications,
        LockInfoDto lockInfo,
        Boolean active,
        @JsonSerialize(using = IsoLocalDateTimeSerializer.class)
        LocalDateTime createdAt,
        @JsonSerialize(using = IsoLocalDateTimeSerializer.class)
        LocalDateTime updatedAt,
        Long version
) {}
