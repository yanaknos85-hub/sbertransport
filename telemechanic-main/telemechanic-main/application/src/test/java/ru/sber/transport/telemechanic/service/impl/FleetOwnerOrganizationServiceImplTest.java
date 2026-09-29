package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.database.dao.FleetOwnerOrganizationRepository;
import ru.sber.transport.telemechanic.dto.GetAllActiveOrganizationNamesDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса по работе с организациями владельцев автопарков")
class FleetOwnerOrganizationServiceImplTest {

    @InjectMocks
    private FleetOwnerOrganizationServiceImpl fleetOwnerOrganizationService;
    @Mock
    private FleetOwnerOrganizationRepository fleetOwnerOrganizationRepository;


    @Test
    void getFleetOwnerOrganizations() {
        var expected1 = Instancio.create(GetAllActiveOrganizationNamesDto.class);
        var expected2 = Instancio.create(GetAllActiveOrganizationNamesDto.class);
        var expected = List.of(expected1, expected2);
        doReturn(expected).when(fleetOwnerOrganizationRepository).findAllActive();
        assertThat(fleetOwnerOrganizationService.getAllActive()).usingRecursiveComparison()
                .isEqualTo(expected);
    }
}