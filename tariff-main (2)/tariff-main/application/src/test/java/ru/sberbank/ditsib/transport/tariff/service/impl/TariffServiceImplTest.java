package ru.sberbank.ditsib.transport.tariff.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sberbank.ditsib.transport.tariff.database.dao.*;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.dto.TariffSearchDTO;
import ru.sberbank.ditsib.transport.tariff.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TariffServiceImplTest {
    @InjectMocks
    private TariffServiceImpl tariffService;
    @Mock
    private TariffRepository<BaseTariff> repository;
    @Mock
    private WalkTariffRepository walkTariffRepository;
    @Mock
    private TaxiTariffRepository taxiTariffRepository;
    @Mock
    private PersonalTariffRepository personalTariffRepository;
    @Mock
    private PublicTariffRepository publicTariffRepository;
    @Mock
    private BicycleTariffRepository bicycleTariffRepository;
    @Mock
    private ScooterTariffRepository scooterTariffRepository;
    @Mock
    private CarSharingTariffRepository carSharingTariffRepository;
    @Mock
    private EntityDTOMapper entityDTOMapper;
    @Mock
    private ContractorRepository contractorRepository;
    @Mock
    private SQGenerator sqGenerator;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private TaxiTariffSender taxiTariffSender;
    @Mock
    private PersonalTariffSender personalTariffSender;
    @Mock
    private PublicTariffSender publicTariffSender;
    @Mock
    private CarSharingTariffSender carSharingTariffSender;
    @Mock
    private GroupTransferTariffSender groupTransferTariffSender;
    @Mock
    private GeoZoneRepository geoZoneRepository;
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private GroupTransferTariffRepository groupTransferTariffRepository;

    @Test
    void search() {
        var tariffSearchDTO = Instancio.create(TariffSearchDTO.class);
        var organizationId = UUID.randomUUID();
        var pageRequest = PageRequest.of(0, 10);
        var baseTariffs = Instancio.ofList(BaseTariff.class)
                .size(5)
                .create();
        var baseTariffPage = new PageImpl<>(baseTariffs, pageRequest, 5);
        lenient().doReturn(baseTariffPage).when(repository).findAll(ArgumentMatchers.<Specification<BaseTariff>>any(), any(Pageable.class));
        lenient().doReturn(baseTariffPage).when(taxiTariffRepository).findAll(ArgumentMatchers.<Specification<TaxiTariff>>any(), any(Pageable.class));
        lenient().doReturn(baseTariffPage).when(personalTariffRepository).findAll(ArgumentMatchers.<Specification<PersonalTariff>>any(), any(Pageable.class));
        lenient().doReturn(baseTariffPage).when(carSharingTariffRepository).findAll(ArgumentMatchers.<Specification<CarSharingTariff>>any(), any(Pageable.class));
        lenient().doReturn(baseTariffPage).when(bicycleTariffRepository).findAll(ArgumentMatchers.<Specification<BicycleTariff>>any(), any(Pageable.class));
        lenient().doReturn(baseTariffPage).when(scooterTariffRepository).findAll(ArgumentMatchers.<Specification<ScooterTariff>>any(), any(Pageable.class));
        lenient().doReturn(baseTariffPage).when(publicTariffRepository).findAll(ArgumentMatchers.<Specification<PublicTariff>>any(), any(Pageable.class));
        lenient().doReturn(baseTariffs).when(walkTariffRepository).findAll();
        lenient().doReturn(baseTariffPage).when(groupTransferTariffRepository).findAll(ArgumentMatchers.<Specification<GroupTransferTariff>>any(), any(Pageable.class));
        var actual1 = tariffService.search(tariffSearchDTO, organizationId, true, pageRequest);
        assertThat(actual1)
                .usingRecursiveComparison()
                .isEqualTo(baseTariffPage);
        var actual2 = tariffService.search(null, organizationId, true, pageRequest);
        assertThat(actual2)
                .usingRecursiveComparison()
                .isEqualTo(baseTariffPage);
    }
}