package ru.sber.transport.telemechanic.dto.ewb;

import java.util.List;
import java.util.UUID;

public record ChainDocsDto(
        List<DocsDto> documents
) {
    
    public record DocsDto(
            UUID id,
            UUID chainId,
            String type
    ) {}
}
