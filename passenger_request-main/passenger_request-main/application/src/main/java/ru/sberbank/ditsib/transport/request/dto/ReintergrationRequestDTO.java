package ru.sberbank.ditsib.transport.request.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record ReintergrationRequestDTO( @NotEmpty List<UUID> requestIds) {
}
