package ru.sber.transport.telemechanic.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.LoggingExtension;
import ru.sber.transport.telemechanic.database.dao.DepartmentRepository;
import ru.sber.transport.telemechanic.database.dao.OrganizationRepository;
import ru.sber.transport.telemechanic.database.model.Department;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.dto.DepartmentDto;
import ru.sber.transport.telemechanic.dto.DepartmentWithAutoparkDto;
import ru.sber.transport.telemechanic.dto.OrganizationWithAutoparkDto;
import ru.sber.transport.telemechanic.exception.AwaitingSynchronizationException;
import ru.sber.transport.telemechanic.exception.OrganizationHasNotDepartmentWithAutoparkException;
import ru.sber.transport.telemechanic.exception.OrganizationNotFoundException;
import ru.sber.transport.telemechanic.mapper.DepartmentMapper;
import ru.sber.transport.telemechanic.service.grpc.Departments;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.groups.Tuple.tuple;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса по работе с подразделениями")
class DepartmentServiceImplTest {
    
    @InjectMocks
    private DepartmentServiceImpl service;
    @Mock
    private DepartmentRepository repository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private DepartmentMapper departmentMapper;
    @Mock
    private Departments departments;
    @Captor
    private ArgumentCaptor<Department> captor;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(DepartmentServiceImpl.class);
    
    @Test
    void get() {
        var entity = Instancio.create(Department.class);
        var notExistId = UUID.randomUUID();
        doReturn(Optional.of(entity)).when(repository).findById(entity.getId());
        doReturn(Optional.empty()).when(repository).findById(notExistId);
        assertThat(service.get(entity.getId())).isEqualTo(Optional.of(entity));
        assertThat(service.get(notExistId)).isNotPresent();
    }
    
    @Test
    void delete() {
        var entity = Instancio.create(Department.class);
        doReturn(entity).when(repository).save(captor.capture());
        service.delete(entity);
        var savedEntity = captor.getValue();
        assertThat(savedEntity)
                .usingRecursiveComparison()
                .ignoringFields("active")
                .isEqualTo(entity);
        assertThat(savedEntity.isActive()).isFalse();
        verify(repository).save(any(Department.class));
    }
    
    @Test
    void save() {
        var entity = Instancio.create(Department.class);
        doReturn(entity).when(repository).save(captor.capture());
        var actual = service.save(entity);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(entity);
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(entity);
        verify(repository).save(any(Department.class));
    }
    
    @Test
    void getByOrganizationId() {
        var organizationId = UUID.randomUUID();
        var department = Instancio.create(Department.class);
        var departments = Collections.singletonList(department);
        var expected = Collections.singletonList(new DepartmentDto(
                department.getId(),
                department.getDepartmentName(),
                null
        ));
        doReturn(departments).when(repository).findAllByOrganizationIdOrderByDepartmentName(organizationId);
        doReturn(expected).when(departmentMapper).departmentListToDepartmentDtoList(departments);
        assertThat(service.getByOrganizationId(organizationId)).isEqualTo(expected);
    }
    
    @Test
    void getNotOrganizationIds() {
        var organizationId = UUID.randomUUID();
        var departmentIds = Set.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        
        doReturn(Set.of()).when(repository).findNotOrganizationIds(organizationId, departmentIds);
        
        assertThat(service.getNotOrganizationIds(organizationId, departmentIds)).isEmpty();
    }
    
    @Test
    @DisplayName("Сохраняем сущность по grpc, нет родителя")
    void saveGrpcEntityNoParent() {
        var message = Instancio.create(String.class);
        var id = UUID.randomUUID();
        var entity = Instancio.of(Department.class)
                              .set(field(Department::getParentId), null)
                              .create();
        doReturn(entity).when(departments).one(id);
        doReturn(entity).when(repository).save(captor.capture());
        service.saveGrpcEntity(message, id);
        assertThat(captor.getAllValues())
                .hasSize(1)
                .usingRecursiveComparison()
                .isEqualTo(List.of(entity));
        verify(departments).one(any(UUID.class));
        verify(repository, never()).findById(any(UUID.class));
        verify(repository).save(any(Department.class));
    }
    
    @Test
    @DisplayName("Сохраняем сущность по grpc, grpc вернула запись, у записи есть родитель в БД")
    void saveGrpcEntityParentInDb() {
        var message = Instancio.create(String.class);
        var id = UUID.randomUUID();
        var entity1 = Instancio.create(Department.class);
        var entity2 = Instancio.create(Department.class);
        doReturn(entity1).when(departments).one(id);
        doReturn(Optional.of(entity2)).when(repository).findById(entity1.getParentId());
        doReturn(entity1).when(repository).save(captor.capture());
        service.saveGrpcEntity(message, id);
        assertThat(captor.getAllValues())
                .hasSize(1)
                .usingRecursiveComparison()
                .isEqualTo(List.of(entity1));
        verify(departments).one(any(UUID.class));
        verify(repository).findById(any(UUID.class));
        verify(repository).save(any(Department.class));
    }
    
    @Test
    @DisplayName("Сохраняем сущность по grpc, grpc вернула запись, у записи нет родителя в БД, есть родитель по grpc")
    void saveGrpcEntityWithParent() {
        var message = Instancio.create(String.class);
        var id = UUID.randomUUID();
        var entity1 = Instancio.create(Department.class);
        var entity2 = Instancio.of(Department.class)
                               .set(field(Department::getParentId), null)
                               .create();
        doReturn(entity1).when(departments).one(id);
        doReturn(entity2).when(departments).one(entity1.getParentId());
        doReturn(entity2, entity1).when(repository).save(captor.capture());
        doReturn(Optional.empty()).when(repository).findById(entity1.getParentId());
        service.saveGrpcEntity(message, id);
        assertThat(captor.getAllValues())
                .hasSize(2)
                .usingRecursiveComparison()
                .isEqualTo(List.of(entity2, entity1));
        verify(departments, times(2)).one(any(UUID.class));
        verify(repository).findById(any(UUID.class));
        verify(repository, times(2)).save(any(Department.class));
    }
    
    @Test
    @DisplayName("Сохраняем сущность по grpc, grpc не вернула запись")
    void saveGrpcEntityNoDepartmentByGrpc() {
        var message = Instancio.create(String.class);
        var wrongId = UUID.randomUUID();
        doReturn(null).when(departments).one(wrongId);
        assertThatExceptionOfType(AwaitingSynchronizationException.class)
                .isThrownBy(() -> service.saveGrpcEntity(message, wrongId))
                .withMessage("Awaiting an department synchronization");
        assertThat(LOGGING_EXTENSION.getEvents())
                .hasSize(1)
                .extracting(
                        ILoggingEvent::getLevel,
                        ILoggingEvent::getFormattedMessage
                           )
                .containsExactly(
                        tuple(
                                Level.INFO,
                                message
                             )
                                );
        verify(departments).one(any(UUID.class));
        verify(repository, never()).findById(any(UUID.class));
        verify(repository, never()).save(any(Department.class));
    }
    
    @Test
    @DisplayName("Сохраняем родителей по grpc, grpc вернула запись, у записи нет родителя")
    void saveGrpcParentEntitiesHaveDepartmentByGrpcNoParent() {
        var parentId = UUID.randomUUID();
        var entity = Instancio.of(Department.class)
                              .set(field(Department::getParentId), null)
                              .create();
        doReturn(entity).when(departments).one(parentId);
        doReturn(entity).when(repository).save(captor.capture());
        service.saveGrpcParentEntities(parentId);
        assertThat(captor.getAllValues())
                .hasSize(1)
                .usingRecursiveComparison()
                .isEqualTo(Collections.singletonList(entity));
        verify(departments).one(any(UUID.class));
        verify(repository, never()).findById(any(UUID.class));
        verify(repository).save(any(Department.class));
    }
    
    @Test
    @DisplayName("Сохраняем родителей по grpc, grpc не вернула запись, у записи нет родителя")
    void saveGrpcParentEntitiesNoDepartmentByGrpc() {
        var parentId = UUID.randomUUID();
        doReturn(null).when(departments).one(parentId);
        assertThatThrownBy(() -> service.saveGrpcParentEntities(parentId))
                .isInstanceOf(AwaitingSynchronizationException.class)
                .hasMessage("Awaiting a parent department synchronization, parentId:" + parentId);
        verify(departments).one(any(UUID.class));
        verify(repository, never()).findById(any(UUID.class));
        verify(repository, never()).save(any(Department.class));
    }
    
    @Test
    @DisplayName("Сохраняем родителей по grpc, grpc вернула запись, у записи есть родитель в БД")
    void saveGrpcParentEntitiesHaveDepartmentByGrpcHaveParentInDB() {
        var parentId = UUID.randomUUID();
        var entity1 = Instancio.create(Department.class);
        var entity2 = Instancio.of(Department.class)
                               .set(field(Department::getParentId), null)
                               .create();
        doReturn(entity1).when(departments).one(parentId);
        doReturn(Optional.of(entity2)).when(repository).findById(entity1.getParentId());
        doReturn(entity1, entity2).when(repository).save(captor.capture());
        service.saveGrpcParentEntities(parentId);
        assertThat(captor.getAllValues())
                .hasSize(1)
                .usingRecursiveComparison()
                .isEqualTo(Collections.singletonList(entity1));
        verify(departments).one(any(UUID.class));
        verify(repository).findById(any(UUID.class));
        verify(repository, times(1)).save(any(Department.class));
    }
    
    @Test
    @DisplayName("Сохраняем родителей по grpc, grpc вернула запись, у записи нет родителя в БД, grpc вернула запись родителя")
    void saveGrpcParentEntitiesHaveDepartmentByGrpcHaveParentByGrpc() {
        var parentId = UUID.randomUUID();
        var entity1 = Instancio.create(Department.class);
        var entity2 = Instancio.of(Department.class)
                               .set(field(Department::getParentId), null)
                               .create();
        doReturn(entity1).when(departments).one(parentId);
        doReturn(entity2).when(departments).one(entity1.getParentId());
        doReturn(Optional.empty()).when(repository).findById(entity1.getParentId());
        doReturn(entity1, entity2).when(repository).save(captor.capture());
        service.saveGrpcParentEntities(parentId);
        assertThat(captor.getAllValues())
                .hasSize(2)
                .usingRecursiveComparison()
                .isEqualTo(List.of(entity2, entity1));
        verify(departments, times(2)).one(any(UUID.class));
        verify(repository).findById(any(UUID.class));
        verify(repository, times(2)).save(any(Department.class));
    }
    
    @Test
    @DisplayName("Сохраняем родителей по grpc, grpc вернула запись, у записи нет родителя в БД, grpc не вернула запись родителя")
    void saveGrpcParentEntitiesHaveDepartmentByGrpcNoParentByGrpc() {
        var parentId = UUID.randomUUID();
        var entity = Instancio.create(Department.class);
        doReturn(entity).when(departments).one(parentId);
        doReturn(null).when(departments).one(entity.getParentId());
        doReturn(Optional.empty()).when(repository).findById(entity.getParentId());
        assertThatThrownBy(() -> service.saveGrpcParentEntities(parentId))
                .isInstanceOf(AwaitingSynchronizationException.class)
                .hasMessage("Awaiting a parent department synchronization, parentId:" + parentId);
        verify(departments, times(2)).one(any(UUID.class));
        verify(repository).findById(any(UUID.class));
        verify(repository, never()).save(any(Department.class));
    }
    
    @Test
    @DisplayName("Сохраняем родителей по grpc, длинная цепочка родителей")
    void saveGrpcParentEntitiesLongTree() {
        var parentId = UUID.randomUUID();
        var entity1 = Instancio.create(Department.class);
        var entity2 = Instancio.create(Department.class);
        var entity3 = Instancio.create(Department.class);
        var entity4 = Instancio.create(Department.class);
        var entity5 = Instancio.of(Department.class)
                               .set(field(Department::getParentId), null)
                               .create();
        doReturn(entity1).when(departments).one(parentId);
        doReturn(entity2).when(departments).one(entity1.getParentId());
        doReturn(entity3).when(departments).one(entity2.getParentId());
        doReturn(entity4).when(departments).one(entity3.getParentId());
        doReturn(entity5).when(departments).one(entity4.getParentId());
        doReturn(Optional.empty()).when(repository).findById(entity1.getParentId());
        doReturn(Optional.empty()).when(repository).findById(entity2.getParentId());
        doReturn(Optional.empty()).when(repository).findById(entity3.getParentId());
        doReturn(Optional.empty()).when(repository).findById(entity4.getParentId());
        doReturn(entity1, entity2, entity3, entity4, entity5).when(repository).save(captor.capture());
        service.saveGrpcParentEntities(parentId);
        assertThat(captor.getAllValues())
                .hasSize(5)
                .usingRecursiveComparison()
                .isEqualTo(List.of(entity5, entity4, entity3, entity2, entity1));
        verify(departments, times(5)).one(any(UUID.class));
        verify(repository, times(4)).findById(any(UUID.class));
        verify(repository, times(5)).save(any(Department.class));
    }
    
    @Test
    void getByOrganizationWithDepartmentListTest() {
        var departmentList= List.of(new DepartmentWithAutoparkDto(UUID.randomUUID(), "auto park"));
        var organization = new Organization(
                UUID.randomUUID(),
                1L,
                "name",
                "msrn",
                "tin",
                UUID.randomUUID(), true, null, null,null
        );
                                            
        when(repository.findOrganiztionWithAutopark(organization.getId())).thenReturn(departmentList);
        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        
        var expected = new OrganizationWithAutoparkDto (organization.getOfficialName(), departmentList);
        assertThat(service.getAllWithInternalAutoPark(organization.getId())).isEqualTo(expected);
    }
    
    @Test
    void getThrowByOrganizationWithDepartmentListTest() {
        var organizationId = UUID.randomUUID();
        when(repository.findOrganiztionWithAutopark(any())).thenReturn(List.of());
        
        assertThrows(
                OrganizationHasNotDepartmentWithAutoparkException.class,
                () -> service.getAllWithInternalAutoPark(organizationId)
        );
        
        verify(repository, times(1)).findOrganiztionWithAutopark(any());
        verify(organizationRepository, never()).findById(any());
    }
    
    @Test
    void getThrowByOrganizationTest() {
        var organizationId = UUID.randomUUID();
        var departmentList= List.of(new DepartmentWithAutoparkDto(UUID.randomUUID(), "auto park"));
        
        
        when(repository.findOrganiztionWithAutopark(organizationId)).thenReturn(departmentList);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.empty());
        
        assertThrows(
            OrganizationNotFoundException.class,
            () -> service.getAllWithInternalAutoPark(organizationId)
        );
    }
}
