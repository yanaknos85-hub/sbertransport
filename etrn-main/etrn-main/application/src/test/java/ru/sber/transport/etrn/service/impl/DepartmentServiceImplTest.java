package ru.sber.transport.etrn.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.etrn.database.dao.DepartmentRepository;
import ru.sber.transport.etrn.database.model.Department;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    private static final UUID DEPT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Mock
    private DepartmentRepository repository;

    @InjectMocks
    private DepartmentServiceImpl service;

    private Department department;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(DEPT_ID)
                .departmentName("Отдел разработки")
                .build();
    }

    @Test
    @DisplayName("save — сохранение department")
    void save_returnsSaved() {
        when(repository.save(department)).thenReturn(department);

        Department result = service.save(department);

        assertThat(result).isEqualTo(department);
        verify(repository).save(department);
    }

    @Test
    @DisplayName("findById — найден")
    void findById_found_returnsOptional() {
        when(repository.findById(DEPT_ID)).thenReturn(Optional.of(department));

        Optional<Department> result = service.findById(DEPT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(DEPT_ID);
    }

    @Test
    @DisplayName("findById — не найден")
    void findById_notFound_returnsEmpty() {
        when(repository.findById(DEPT_ID)).thenReturn(Optional.empty());

        Optional<Department> result = service.findById(DEPT_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("delete — удаление department")
    void delete_callsRepositoryDelete() {
        service.delete(department);
        verify(repository).delete(department);
    }

    @Test
    @DisplayName("get — получение по id")
    void get_found_returnsOptional() {
        when(repository.findById(DEPT_ID)).thenReturn(Optional.of(department));

        Optional<Department> result = service.get(DEPT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getDepartmentName()).isEqualTo("Отдел разработки");
    }

    @Test
    @DisplayName("findOrCreateById — уже существует")
    void findOrCreateById_exists_returnsExisting() {
        when(repository.findById(DEPT_ID)).thenReturn(Optional.of(department));

        Department result = service.findOrCreateById(DEPT_ID);

        assertThat(result.getId()).isEqualTo(DEPT_ID);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("findOrCreateById — не существует, создаётся новый")
    void findOrCreateById_notExists_createsNew() {
        when(repository.findById(DEPT_ID)).thenReturn(Optional.empty());
        when(repository.save(any(Department.class))).thenReturn(department);

        Department result = service.findOrCreateById(DEPT_ID);

        assertThat(result.getId()).isEqualTo(DEPT_ID);
        verify(repository).save(any(Department.class));
    }
}
