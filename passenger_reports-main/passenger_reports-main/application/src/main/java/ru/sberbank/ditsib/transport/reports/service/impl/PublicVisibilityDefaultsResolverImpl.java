package ru.sberbank.ditsib.transport.reports.service.impl;

import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.reports.dto.PublicUIVisibilityDTO;
import ru.sberbank.ditsib.transport.reports.service.DefaultResolver;

@Component
class PublicVisibilityDefaultsResolverImpl implements DefaultResolver<PublicUIVisibilityDTO> {
    @Override
    public PublicUIVisibilityDTO getDefault() {
        return new PublicUIVisibilityDTO(
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true
        );
    }
}
