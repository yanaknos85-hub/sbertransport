package ru.sberbank.ditsib.transport.reports.service.impl;

import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.reports.dto.PersonalUIVisibilityDTO;
import ru.sberbank.ditsib.transport.reports.service.DefaultResolver;

@Component
class PersonalVisibilityDefaultsResolverImpl implements DefaultResolver<PersonalUIVisibilityDTO> {
    @Override
    public PersonalUIVisibilityDTO getDefault() {
        return new PersonalUIVisibilityDTO(
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
