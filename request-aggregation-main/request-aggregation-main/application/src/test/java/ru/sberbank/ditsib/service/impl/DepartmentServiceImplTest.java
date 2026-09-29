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
import ru.sberbank.ditsib.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.database.model.Department;
import ru.sberbank.ditsib.database.model.Organization;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса по работе с подразделениями")
class DepartmentServiceImplTest {
    
    @InjectMocks
    private DepartmentServiceImpl service;
    private Department department1;
    @Mock
    private DepartmentRepository repository;
    @Captor
    private ArgumentCaptor<Department> captor;
    
    @BeforeEach
    void setup() {
        var organization1 = Organization.builder()
                .id(UUID.randomUUID())
                .officialName("officialName1")
                .digitId(1L)
                .build();
        department1 = Department.builder()
                .id(UUID.randomUUID())
                .humanReadableId("DT-0001-00000001")
                .organization(organization1)
                .departmentName("departmentName1")
                .parent(null)
                .build();
    }
    
    @Test
    void get() {
        var notExistId = UUID.randomUUID();
        when(repository.findById(department1.getId())).thenReturn(Optional.of(department1));
        when(repository.findById(notExistId)).thenReturn(Optional.empty());
        assertThat(service.get(department1.getId()).orElse(null)).isEqualTo(department1);
        assertThat(service.get(notExistId)).isEmpty();
    }
    
    @Test
    void delete() {
        var expected = department1.toBuilder()
                                  .active(false)
                                  .build();
        when(repository.save(captor.capture())).thenReturn(expected);
        service.delete(department1);
        verify(repository).save(any(Department.class));
        assertThat(captor.getValue()).isEqualTo(expected);
    }
    
    @Test
    void save() {
        when(repository.save(captor.capture())).thenReturn(department1);
        var actual = service.save(department1);
        assertThat(actual).isEqualTo(department1);
    }
}
