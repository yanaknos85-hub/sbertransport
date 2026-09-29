package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.database.dao.MedicContractorRepository;
import ru.sber.transport.telemechanic.database.model.MedicContractor;
import ru.sber.transport.telemechanic.dto.telemedicine.MedicInfo;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MedicContractorServiceImplTest {
    
    @Mock
    private MedicContractorRepository medicContractorRepository;
    @InjectMocks
    private MedicContractorServiceImpl medicContractorService;
    
    @Test
    void saveIfNotExistsByPersonnelNumber() {
        var medicInfo = Instancio.create(MedicInfo.class);
        var medicContractor = Instancio.create(MedicContractor.class);
        doReturn(Optional.of(medicContractor)).when(medicContractorRepository).findByPersonnelNumber(medicInfo.personalNumber());
        var result1 = medicContractorService.saveIfNotExistsByPersonnelNumber(medicInfo);
        assertThat(result1)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(medicContractor);
        
        doReturn(Optional.empty()).when(medicContractorRepository).findByPersonnelNumber(medicInfo.personalNumber());
        doReturn(medicContractor).when(medicContractorRepository).saveAndFlush(any(MedicContractor.class));
        var result2 = medicContractorService.saveIfNotExistsByPersonnelNumber(medicInfo);
        var medicContractorArgumentCaptor = ArgumentCaptor.forClass(MedicContractor.class);
        verify(medicContractorRepository).saveAndFlush(medicContractorArgumentCaptor.capture());
        var capturedMedicContractor = medicContractorArgumentCaptor.getValue();
        assertThat(result2)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(medicContractor);
        assertThat(capturedMedicContractor)
                .isNotNull()
                .extracting(
                        MedicContractor::getFullName,
                        MedicContractor::getPersonnelNumber,
                        MedicContractor::getOrganization,
                        MedicContractor::getDepartment,
                        MedicContractor::getPosition,
                        MedicContractor::getSignKeyNumber,
                        MedicContractor::getSignKeyEndDateTime
                           )
                .containsExactly(
                        medicInfo.fio(),
                        medicInfo.personalNumber(),
                        medicInfo.organization(),
                        medicInfo.department(),
                        medicInfo.position(),
                        medicInfo.serialNumber(),
                        medicInfo.serialEndDateTime()
                                );
    }
}