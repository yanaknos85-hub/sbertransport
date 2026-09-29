package ru.sberbank.ditsib.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.database.model.Organization;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса по работе с организациями")
class OrganizationServiceImplTest {

    @InjectMocks
    private OrganizationServiceImpl service;
    private Organization organization1;
    @Mock
    private OrganizationRepository repository;
    @Captor
    private ArgumentCaptor<Organization> captor;

    @BeforeEach
    void setup() {
        organization1 = Organization.builder()
                .id(UUID.randomUUID())
                .officialName("officialName1")
                .digitId(1L)
                .build();
    }

    @Test
    void getTest() {
        var notExistId = UUID.randomUUID();
        when(repository.findById(organization1.getId())).thenReturn(Optional.of(organization1));
        when(repository.findById(notExistId)).thenReturn(Optional.empty());
        assertThat(service.get(organization1.getId()).orElse(null)).isEqualTo(organization1);
        assertThat(service.get(notExistId)).isEmpty();
    }

    @Test
    void delete() {
        var expected = organization1.toBuilder()
                .active(false)
                .build();
        when(repository.save(captor.capture())).thenReturn(expected);
        service.delete(organization1);
        verify(repository).save(any(Organization.class));
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    void save() {
        when(repository.save(captor.capture())).thenReturn(organization1);
        var actual = service.save(organization1);
        assertThat(actual).isEqualTo(organization1);
    }
}