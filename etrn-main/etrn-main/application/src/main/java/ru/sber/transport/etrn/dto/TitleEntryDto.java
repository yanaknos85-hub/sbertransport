package ru.sber.transport.etrn.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.sber.transport.etrn.config.converters.IsoLocalDateTimeSerializer;

import java.time.LocalDateTime;

/**
 * DTO-представление элемента цепочки титулов (TitleEntry).
 */
public record TitleEntryDto(
        String title,
        @JsonSerialize(using = IsoLocalDateTimeSerializer.class)
        LocalDateTime signedAt,
        String signedBy
) {}
