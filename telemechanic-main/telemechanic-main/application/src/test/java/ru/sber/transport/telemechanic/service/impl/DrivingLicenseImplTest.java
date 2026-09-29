package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.database.dao.DrivingLicenseRepository;
import ru.sber.transport.telemechanic.database.model.DrivingLicense;
import ru.sber.transport.telemechanic.exception.DrivingLicenseNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrivingLicenseImplTest {
    
    @Mock
    private DrivingLicenseRepository drivingLicenseRepository;
    
    @InjectMocks
    private DrivingLicenseServiceImpl drivingLicenseService;
    
    private final ArgumentCaptor<DrivingLicense> drivingLicenseArgumentCaptor = ArgumentCaptor.forClass(DrivingLicense.class);
    
    @Test
    void saveOrUpdate() {
        var drivingLicense = Instancio.create(DrivingLicense.class);
        
        doReturn(Optional.of(drivingLicense)).when(drivingLicenseRepository).findById(any(UUID.class));
        doReturn(drivingLicense).when(drivingLicenseRepository).save(drivingLicenseArgumentCaptor.capture());
        drivingLicenseService.saveOrUpdate(drivingLicense);
        
        verify(drivingLicenseRepository, times(1)).save(any(DrivingLicense.class));
        assertThat(drivingLicenseArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(drivingLicense);
        doReturn(Optional.empty()).when(drivingLicenseRepository).findById(any(UUID.class));
        
        drivingLicenseService.saveOrUpdate(drivingLicense);
        verify(drivingLicenseRepository, times(2)).save(any(DrivingLicense.class));
        assertThat(drivingLicenseArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(drivingLicense);
    }
    
    @Test
    void get() {
        var id = UUID.randomUUID();
        var wrongId = UUID.randomUUID();
        var drivingLicense = Instancio.create(DrivingLicense.class);
        doReturn(Optional.of(drivingLicense)).when(drivingLicenseRepository).findById(id);
        doReturn(Optional.empty()).when(drivingLicenseRepository).findById(wrongId);
        assertThat(drivingLicenseService.get(id))
                .usingRecursiveComparison()
                .isEqualTo(drivingLicense);
        assertThatExceptionOfType(DrivingLicenseNotFoundException.class)
                .isThrownBy(() -> drivingLicenseService.get(wrongId))
                .withMessage("Водительское удостоверение с идентификатором %s не найдено".formatted(wrongId));
    }
}