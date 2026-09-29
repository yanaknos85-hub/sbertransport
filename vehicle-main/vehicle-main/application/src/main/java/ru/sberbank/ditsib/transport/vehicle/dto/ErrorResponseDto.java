package ru.sberbank.ditsib.transport.vehicle.dto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatusCode;

import java.util.UUID;

public record ErrorResponseDto(UUID errorId, int status, String detail, String instance) {

    public static ErrorResponseDto of(UUID errorId, HttpStatusCode status, @NotNull String detail, HttpServletRequest request) {
        return new ErrorResponseDto(errorId, status.value(), detail, request.getRequestURI());
    }
}
