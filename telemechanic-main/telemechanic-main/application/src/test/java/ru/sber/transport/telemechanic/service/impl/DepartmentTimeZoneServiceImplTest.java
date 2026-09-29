package ru.sber.transport.telemechanic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.database.dao.DepartmentTimeZoneRepository;
import ru.sber.transport.telemechanic.exception.DepartmentTimeZoneException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DepartmentTimeZoneServiceImplTest {
    
    @Mock
    private DepartmentTimeZoneRepository departmentTimeZoneRepository;
    @InjectMocks
    private DepartmentTimeZoneServiceImpl service;
    
    @Test
    void getTimeZoneByDepartmentId_Success() {
        var departmentId = UUID.randomUUID();
        var timeZone = "UTC+03:00";
        doReturn(List.of(timeZone)).when(departmentTimeZoneRepository).findByDepartmentIdWithHierarchy(departmentId);
        
        var actual = service.getTimeZoneByDepartmentId(departmentId);
        
        assertThat(actual).isEqualTo(timeZone);
        verify(departmentTimeZoneRepository).findByDepartmentIdWithHierarchy(departmentId);
    }
    
    @Test
    void getTimeZoneByDepartmentId_WhenDepartmentIdIsNull_ThrowsException() {
        assertThatExceptionOfType(DepartmentTimeZoneException.class)
                .isThrownBy(() -> service.getTimeZoneByDepartmentId(null))
                .withMessage("departmentId не может быть null");
    }
    
    @Test
    void getTimeZoneByDepartmentId_WhenNoTimeZoneFound_ThrowsException() {
        var departmentId = UUID.randomUUID();
        
        doReturn(Collections.emptyList()).when(departmentTimeZoneRepository).findByDepartmentIdWithHierarchy(departmentId);
        
        assertThatExceptionOfType(DepartmentTimeZoneException.class)
                .isThrownBy(() -> service.getTimeZoneByDepartmentId(departmentId))
                .withMessage("Часовой пояс не найден для подразделения с id: " + departmentId);
        verify(departmentTimeZoneRepository).findByDepartmentIdWithHierarchy(departmentId);
    }
    
    @Test
    void getTimeZoneByDepartmentId_WhenTimeZoneIsNull_ThrowsException() {
        var departmentId = UUID.randomUUID();
        doReturn(List.of("test UTC")).when(departmentTimeZoneRepository).findByDepartmentIdWithHierarchy(departmentId);
        
        assertThatExceptionOfType(DepartmentTimeZoneException.class)
                .isThrownBy(() -> service.getTimeZoneByDepartmentId(departmentId))
                .withMessage("Часовой пояс для подразделения с id: %s не соответствует шаблону UTC+HH:mm".formatted(departmentId));
        verify(departmentTimeZoneRepository).findByDepartmentIdWithHierarchy(departmentId);
    }
    
    @Test
    void validateTimeZonePattern() {
        var departmentId = UUID.randomUUID();
        assertThatExceptionOfType(DepartmentTimeZoneException.class)
                .isThrownBy(() -> service.validateTimeZonePattern("UTC+3:03", departmentId));
        assertThatExceptionOfType(DepartmentTimeZoneException.class)
                .isThrownBy(() -> service.validateTimeZonePattern("UTC+25:03", departmentId));
        assertThatExceptionOfType(DepartmentTimeZoneException.class)
                .isThrownBy(() -> service.validateTimeZonePattern("UTC+03:60", departmentId));
        assertThatExceptionOfType(DepartmentTimeZoneException.class)
                .isThrownBy(() -> service.validateTimeZonePattern("UTC+03:3", departmentId));
        assertThatExceptionOfType(DepartmentTimeZoneException.class)
                .isThrownBy(() -> service.validateTimeZonePattern("UTC+03:03:03", departmentId));
        assertThatExceptionOfType(DepartmentTimeZoneException.class)
                .isThrownBy(() -> service.validateTimeZonePattern("GMT+03:03", departmentId));
        assertThatExceptionOfType(DepartmentTimeZoneException.class)
                .isThrownBy(() -> service.validateTimeZonePattern("UTC=03:03", departmentId));
    }
}
