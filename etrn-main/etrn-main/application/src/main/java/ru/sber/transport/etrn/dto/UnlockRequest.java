package ru.sber.transport.etrn.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UnlockRequest(
        @NotNull(message = "Поле userId обязательно")
        UUID userId
) {}
