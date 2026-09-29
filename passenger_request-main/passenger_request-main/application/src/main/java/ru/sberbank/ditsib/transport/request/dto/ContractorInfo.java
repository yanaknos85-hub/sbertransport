package ru.sberbank.ditsib.transport.request.dto;

import java.util.UUID;

public record ContractorInfo(
        UUID id,
        String url,
        String login,
        String password
) {
}
