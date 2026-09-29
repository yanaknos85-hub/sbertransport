package ru.sberbank.ditsib.transport.vehicle.service.corp.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.vehicle.database.dao.DepartmentRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {
    
    @InjectMocks
    private DepartmentServiceImpl departmentService;
    @Mock
    private DepartmentRepository departmentRepository;
    
    @Test
    void getParentDepartments() {
        var rootId = UUID.randomUUID();
        var actualId1 = UUID.randomUUID();
        var actualId2 = UUID.randomUUID();
        when(departmentRepository.getParentDepartments(rootId)).thenReturn(List.of(actualId1, actualId2));
        var result = departmentService.getParentDepartments(rootId);
        assertEquals(2, result.size());
        assertTrue(result.contains(actualId1));
        assertTrue(result.contains(actualId2));
    }
}