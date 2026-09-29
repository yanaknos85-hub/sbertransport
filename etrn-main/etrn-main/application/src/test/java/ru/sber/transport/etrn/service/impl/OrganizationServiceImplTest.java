package ru.sber.transport.etrn.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.etrn.database.dao.OrganizationRepository;
import ru.sber.transport.etrn.database.model.Organization;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceImplTest {

    private static final UUID ORG_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Mock
    private OrganizationRepository repository;

    @InjectMocks
    private OrganizationServiceImpl service;

    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                .id(ORG_ID)
                .officialName("ООО Тестовая организация")
                .address("г. Москва, ул. Тестовая, д. 1")
                .build();
    }

    @Test
    @DisplayName("save — сохранение организации")
    void save_returnsSaved() {
        when(repository.save(organization)).thenReturn(organization);

        Organization result = service.save(organization);

        assertThat(result).isEqualTo(organization);
        verify(repository).save(organization);
    }

    @Test
    @DisplayName("findById — найден")
    void findById_found_returnsOptional() {
        when(repository.findById(ORG_ID)).thenReturn(Optional.of(organization));

        Optional<Organization> result = service.findById(ORG_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getOfficialName()).isEqualTo("ООО Тестовая организация");
    }

    @Test
    @DisplayName("findById — не найден")
    void findById_notFound_returnsEmpty() {
        when(repository.findById(ORG_ID)).thenReturn(Optional.empty());

        Optional<Organization> result = service.findById(ORG_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("delete — удаление организации")
    void delete_callsRepositoryDelete() {
        service.delete(organization);
        verify(repository).delete(organization);
    }

    @Test
    @DisplayName("get — получение по id")
    void get_found_returnsOptional() {
        when(repository.findById(ORG_ID)).thenReturn(Optional.of(organization));

        Optional<Organization> result = service.get(ORG_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getAddress()).isEqualTo("г. Москва, ул. Тестовая, д. 1");
    }
}
