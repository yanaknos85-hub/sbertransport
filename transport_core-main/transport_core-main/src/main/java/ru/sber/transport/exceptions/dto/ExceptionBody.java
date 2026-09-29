package ru.sber.transport.exceptions.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;

/**
 * Тело исключения.
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ExceptionBody {

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final OffsetDateTime timestamp = OffsetDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));

    private final String path;

    private final Entity entity;

    private final String message;

    @Singular("problem")
    private final Set<Problem> problems;

}