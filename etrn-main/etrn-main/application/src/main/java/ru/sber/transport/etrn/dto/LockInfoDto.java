package ru.sber.transport.etrn.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.sber.transport.etrn.config.converters.IsoLocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.UUID;

public record LockInfoDto(
        UUID userId,
        @JsonSerialize(using = IsoLocalDateTimeSerializer.class)
        LocalDateTime lockUntil
) {}
