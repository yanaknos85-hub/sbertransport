package ru.sber.transport.etrn.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.sber.transport.etrn.config.converters.IsoLocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.UUID;

public record EtrnJournalDto(
        UUID id,
        String humanReadableId,
        String sla,
        String status,
        String currentTitle,
        String senderName,
        String receiverName,
        String carrierName,
        @JsonSerialize(using = IsoLocalDateTimeSerializer.class)
        LocalDateTime createdAt,
        @JsonSerialize(using = IsoLocalDateTimeSerializer.class)
        LocalDateTime updatedAt
) {}
