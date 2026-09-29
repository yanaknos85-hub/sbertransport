package ru.sber.transport.etrn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EtrnCreateRequest(
        @NotBlank(message = "Поле humanReadableId обязательно")
        String humanReadableId,
        String senderName,
        String receiverName,
        String carrierName,
        String timeZone
) {}