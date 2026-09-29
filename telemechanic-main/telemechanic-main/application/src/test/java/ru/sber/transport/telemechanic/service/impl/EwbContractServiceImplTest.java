package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.database.dao.EwbContractRepository;
import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.enumerate.InspectionType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class EwbContractServiceImplTest {
    
    @InjectMocks
    private EwbContractServiceImpl ewbContractService;
    @Mock
    private EwbContractRepository ewbContractRepository;
    
    @Test
    void save() {
        var ewbContract = Instancio.create(EwbContract.class);
        doReturn(ewbContract).when(ewbContractRepository).save(ewbContract);
        assertThat(ewbContractService.save(ewbContract))
                .usingRecursiveComparison()
                .isEqualTo(ewbContract);
    }
    
    @Test
    void getAllByInspectionTypeAndOrganizationId() {
        var organizationId = UUID.randomUUID();
        var ewbContracts = Instancio.createList(EwbContract.class);
        doReturn(ewbContracts).when(ewbContractRepository).findAllByInspectionTypeInAndOrganizationId(InspectionType.getMedicineTypes(),
                                                                                                    organizationId);
        assertThat(ewbContractService.getAllByInspectionTypeAndOrganizationId(InspectionType.getMedicineTypes(), organizationId))
                .usingRecursiveComparison()
                .isEqualTo(ewbContracts);
    }
}