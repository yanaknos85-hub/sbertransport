package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.sber.transport.telemechanic.database.dao.TransportRepository;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.database.model.Transport;
import ru.sber.transport.telemechanic.dto.CreateRequestDto;
import ru.sber.transport.telemechanic.dto.PageSettingDto;
import ru.sber.transport.telemechanic.dto.transport.GetTransportRequest;
import ru.sber.transport.telemechanic.dto.transport.GetTransportResponse;
import ru.sber.transport.telemechanic.enumerate.TransportStatus;
import ru.sber.transport.telemechanic.mapper.TransportMapper;
import ru.sber.transport.telemechanic.service.EmployeeService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DisplayName("Проверка сервиса по работе с транспортными средставами")
@ExtendWith(MockitoExtension.class)
class TransportServiceImplTest {
    
    @InjectMocks
    private TransportServiceImpl transportService;
    @Mock
    private TransportRepository transportRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private TransportMapper transportMapper;
    @Captor
    private ArgumentCaptor<Transport> transportArgumentCaptor;
    
    @Test
    void getTransportById() {
        var transport = Instancio.of(Transport.class)
                                 .set(field(Transport::getStatus), TransportStatus.IN_USE)
                                 .set(field(Transport::getStateNumber), "А010ЕК50")
                                 .create();
        var createRequestDto = new CreateRequestDto(UUID.randomUUID());
        when(transportRepository.findById(any(UUID.class))).thenReturn(Optional.of(transport));
        var actual1 = transportService.getTransportById(createRequestDto.transportId());
        assertEquals(transport.getId(), actual1.getId());
        assertEquals(transport.getStateNumber(), actual1.getStateNumber());
        assertEquals(transport.getBrand(), actual1.getBrand());
        assertEquals(transport.getModel(), actual1.getModel());
        assertEquals(transport.getStatus(), actual1.getStatus());
    }
    
    @Test
    void get() {
        var transport = Instancio.of(Transport.class)
                                 .set(field(Transport::getStatus), TransportStatus.IN_USE)
                                 .set(field(Transport::getStateNumber), "А010ЕК50")
                                 .create();
        var notTransportId = UUID.randomUUID();
        when(transportRepository.findById(transport.getId())).thenReturn(Optional.of(transport));
        when(transportRepository.findById(notTransportId)).thenReturn(Optional.empty());
        assertEquals(Optional.of(transport), transportService.get(transport.getId()));
        assertEquals(Optional.empty(), transportService.get(notTransportId));
    }
    
    @Test
    void deactivate() {
        var transport = Instancio.of(Transport.class)
                                 .set(field(Transport::getStatus), TransportStatus.IN_USE)
                                 .set(field(Transport::getStateNumber), "А010ЕК50")
                                 .create();
        assertEquals(TransportStatus.IN_USE, transport.getStatus());
        when(transportRepository.save(transportArgumentCaptor.capture())).thenReturn(transport);
        transportService.deactivate(transport);
        assertEquals(TransportStatus.NOT_IN_USE, transport.getStatus());
        assertEquals(TransportStatus.NOT_IN_USE, transportArgumentCaptor.getValue().getStatus());
    }
    
    @Test
    @DisplayName("Сохраняем или обновляем транспортное средство, обновление")
    void saveOrUpdateTest1() {
        var transport = Instancio.of(Transport.class)
                                 .set(field(Transport::getBrand), "ВАЗ")
                                 .set(field(Transport::getModel), "2102")
                                 .set(field(Transport::getMileage), 100000)
                                 .set(field(Transport::getStatus), TransportStatus.IN_USE)
                                 .set(field(Transport::getStateNumber), "А010ЕК50")
                                 .set(field(Transport::getFuelTankVolume), 40)
                                 .set(field(Transport::getOrganizations), Set.of(Instancio.create(Organization.class)))
                                 .create();
        when(transportRepository.findById(transport.getId())).thenReturn(Optional.of(transport));
        when(transportRepository.save(transportArgumentCaptor.capture())).thenReturn(transport);
        transportService.saveOrUpdate(transport);
        var actual = transportArgumentCaptor.getValue();
        assertEquals(transport.getId(), actual.getId());
        assertEquals(transport.getStateNumber(), actual.getStateNumber());
        assertEquals(transport.getBrand(), actual.getBrand());
        assertEquals(transport.getModel(), actual.getModel());
        assertEquals(transport.getStatus(), actual.getStatus());
        assertEquals(transport.getFuelTankVolume(), actual.getFuelTankVolume());
        assertEquals(transport.getOrganizations(), actual.getOrganizations());
    }
    
    @Test
    @DisplayName("Сохраняем или обновляем транспортное средство, сохранение")
    void saveOrUpdateTest2() {
        var transport = Instancio.of(Transport.class)
                                 .set(field(Transport::getStatus), TransportStatus.IN_USE)
                                 .set(field(Transport::getStateNumber), "А010ЕК50")
                                 .create();
        when(transportRepository.findById(transport.getId())).thenReturn(Optional.empty());
        when(transportRepository.save(transportArgumentCaptor.capture())).thenReturn(transport);
        transportService.saveOrUpdate(transport);
        var actual = transportArgumentCaptor.getValue();
        assertEquals(transport.getId(), actual.getId());
        assertEquals(transport.getStateNumber(), actual.getStateNumber());
        assertEquals(transport.getBrand(), actual.getBrand());
        assertEquals(transport.getModel(), actual.getModel());
        assertEquals(transport.getStatus(), actual.getStatus());
        assertEquals(transport.getOrganizations(), actual.getOrganizations());
    }
    
    @Test
    void getTransportByStateNumberSelfOrganization() {
        var request1 = new GetTransportRequest("А777АА", null);
        var employeeId = UUID.randomUUID();
        
        var transport = Instancio.of(Transport.class).create();
        var mappedTransport = Instancio.of(GetTransportResponse.class).create();
        var pageRequest1 = PageRequest.of(0, 10);
        
        doReturn(Instancio.of(Employee.class).create()).when(employeeService).getByUserId(employeeId);
        doReturn(new PageImpl<>(
                List.of(transport, transport, transport),
                pageRequest1,
                3
        )).when(transportRepository).findAllByStateNumber(anyString(), any(UUID.class), any(Pageable.class));
        doReturn(mappedTransport).when(transportMapper).transportToGetTransportResponse(any(Transport.class));
        
        var actual1 = transportService.getTransportByStateNumberSelfOrganization(request1, employeeId);
        assertEquals(3, actual1.getTotalElements());
        
        var request2 = new GetTransportRequest("А777АА", new PageSettingDto(0, 1));
        var pageRequest2 = PageRequest.of(0, 1);
        
        doReturn(new PageImpl<>(
                List.of(transport),
                pageRequest2,
                1
        )).when(transportRepository).findAllByStateNumber(anyString(), any(UUID.class), any(Pageable.class));
        
        var actual2 = transportService.getTransportByStateNumberSelfOrganization(request2, employeeId);
        assertEquals(1, actual2.getTotalElements());
    }
    
    @Test
    void getTransportByStateNumberAllOrganizations() {
        var request1 = new GetTransportRequest("А777АА", null);
        var transport = Instancio.of(Transport.class).create();
        var mappedTransport = Instancio.of(GetTransportResponse.class).create();
        var pageRequest1 = PageRequest.of(0, 10);
        
        doReturn(new PageImpl<>(
                List.of(transport, transport, transport),
                pageRequest1,
                3
        )).when(transportRepository).findAllByStateNumber(anyString(), any(), any(Pageable.class));
        doReturn(mappedTransport).when(transportMapper).transportToGetTransportResponse(any(Transport.class));
        
        var actual1 = transportService.getTransportByStateNumberAllOrganizations(request1);
        assertEquals(3, actual1.getTotalElements());
        
        var request2 = new GetTransportRequest("А777АА", new PageSettingDto(0, 1));
        var pageRequest2 = PageRequest.of(0, 1);
        
        doReturn(new PageImpl<>(
                List.of(transport),
                pageRequest2,
                1
        )).when(transportRepository).findAllByStateNumber(anyString(), any(), any(Pageable.class));
        
        var actual2 = transportService.getTransportByStateNumberAllOrganizations(request2);
        assertEquals(1, actual2.getTotalElements());
    }
}