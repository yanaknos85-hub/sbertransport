package ru.sberbank.ditsib.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;
import ru.sberbank.ditsib.config.LeadExcelConfig;
import ru.sberbank.ditsib.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.database.dao.LeadRepository;
import ru.sberbank.ditsib.database.model.*;
import ru.sberbank.ditsib.dto.lead.LeadRequestDto;
import ru.sberbank.ditsib.dto.lead.LeadResponseDto;
import ru.sberbank.ditsib.dto.point.PointLeadRequestDto;
import ru.sberbank.ditsib.enumerate.LeadStatus;
import ru.sberbank.ditsib.enumerate.PointType;
import ru.sberbank.ditsib.exception.LeadDepartureTimeException;
import ru.sberbank.ditsib.exception.excel.FileNoNameException;
import ru.sberbank.ditsib.exception.excel.FileUnsupportedExtensionException;
import ru.sberbank.ditsib.exception.excel.MaxFileSizeException;
import ru.sberbank.ditsib.mappers.LeadMapper;
import ru.sberbank.ditsib.mappers.PointLeadMapper;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса по работе с пользовательскими заявками")
class LeadServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private LeadRepository leadRepository;
    @Mock
    private LeadMapper leadMapper;
    @Mock
    private PointLeadMapper pointLeadMapper;
    @Mock
    private LeadExcelConfig leadExcelConfig;
    @InjectMocks
    private LeadServiceImpl leadService;
    private Employee employee1;

    @BeforeEach
    void setUp() {
        var organization1 = Organization.builder()
                .id(UUID.randomUUID())
                .officialName("officialName1")
                .digitId(1L)
                .build();
        var department1 = Department.builder()
                .id(UUID.randomUUID())
                .humanReadableId("DT-0001-00000001")
                .organization(organization1)
                .departmentName("departmentName1")
                .parent(null)
                .build();
        var position1 = Position.builder()
                .id(UUID.randomUUID())
                .positionName("positionName1")
                .organization(organization1)
                .build();
        var userId = UUID.randomUUID();
        employee1 = Employee.builder()
                .id(userId)
                .personnelNumber("0000001")
                .patronymic("Олегович")
                .lastName("Горбач")
                .firstName("Константин")
                .mobilePhone("+792356812")
                .organization(organization1)
                .department(department1)
                .userId(userId)
                .humanReadableId("US-0001-00000001")
                .position(position1)
                .build();
    }

    @Test
    void createLeadWhenDepartureTimeExpired() {
        doReturn(3).when(leadExcelConfig).departureDelay();

        var dto = Instancio.of(LeadRequestDto.class)
                .set(field(LeadRequestDto::departureTime), LocalDateTime.now().minusHours(1))
                .create();
        var employeeId = UUID.randomUUID();
        assertThatThrownBy(() -> leadService.createLead(dto, employeeId))
                .isInstanceOf(LeadDepartureTimeException.class)
                .hasMessage("Дата и время заказа должны быть с учетом задержки в 3 часа от текущего времени");
    }

    @Test
    void createLeadWhenEmployeeNotFound() {
        doReturn(3).when(leadExcelConfig).departureDelay();
        doReturn(Optional.empty()).when(employeeRepository).findById(any(UUID.class));

        var dto = Instancio.of(LeadRequestDto.class)
                .set(field(LeadRequestDto::departureTime), LocalDateTime.now().plusHours(5))
                .create();
        var employeeId = UUID.randomUUID();
        assertThatThrownBy(() -> leadService.createLead(dto, employeeId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Сотрудник не найден, табельный номер: " + employeeId);
    }

    @Test
    void createAndSaveLead() {
        var lead1 = Instancio.of(Lead.class)
                .set(field(Lead::getEmployee), employee1)
                .set(field(Lead::getStatus), LeadStatus.PROCESSING)
                .create();
        doReturn(3).when(leadExcelConfig).departureDelay();
        doReturn(Optional.of(employee1)).when(employeeRepository).findById(any(UUID.class));
        doReturn(lead1).when(leadMapper).toEntity(any(LeadRequestDto.class), any(Employee.class));

        var pointDto = Instancio.of(PointLeadRequestDto.class)
                .set(field(PointLeadRequestDto::typePoint), PointType.START)
                .create();
        var pointDto2 = Instancio.of(PointLeadRequestDto.class)
                .set(field(PointLeadRequestDto::typePoint), PointType.END)
                .create();

        var requestDto = Instancio.of(LeadRequestDto.class)
                .set(field(LeadRequestDto::points), List.of(pointDto, pointDto2))
                .set(field(LeadRequestDto::departureTime), LocalDateTime.now().plusHours(5))
                .create();

        var pointLead = Instancio.of(PointLead.class)
                .set(field(PointLead::getTypePoint), PointType.START)
                .set(field(PointLead::getPointNumber), 1)
                .set(field(PointLead::getLead), lead1)
                .create();

        var response = Instancio.of(LeadResponseDto.class)
                .set(field(LeadResponseDto::id), lead1.getId())
                .create();

        doReturn(pointLead).when(pointLeadMapper).toEntity(any(PointLeadRequestDto.class));
        doReturn(lead1).when(leadRepository).save(any(Lead.class));
        doReturn(response).when(leadMapper).toDto(any(Lead.class));
        var res = leadService.createLead(requestDto, employee1.getId());
        assertThat(res).isEqualTo(response);
    }

    @Test
    @SneakyThrows
    void validateLeadsWhenSizeTooLarge() {
        var data = new byte[110];
        new java.util.Random().nextBytes(data);
        var mockFile = new ByteArrayInputStream(data);

        var file = new MockMultipartFile("file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                mockFile);

        doReturn(DataSize.ofBytes(100)).when(leadExcelConfig).maxFileSize();

        assertThatThrownBy(() -> leadService.validateLeads(file)).isInstanceOf(MaxFileSizeException.class)
                .hasMessage("Превышен максимальный размер файла 100B");

    }

    @Test
    @SneakyThrows
    void validateLeadsWhenNoFileName() {
        var data = new byte[90];
        new java.util.Random().nextBytes(data);
        var mockFile = new ByteArrayInputStream(data);

        var file = new MockMultipartFile(" ",
                " ",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                mockFile);

        doReturn(DataSize.ofBytes(100)).when(leadExcelConfig).maxFileSize();

        assertThatThrownBy(() -> leadService.validateLeads(file)).isInstanceOf(FileNoNameException.class)
                .hasMessage("Не указано имя файла");
    }

    @Test
    @SneakyThrows
    void validateLeadsWhenWrongExtension() {
        var data = new byte[90];
        new java.util.Random().nextBytes(data);
        var mockFile = new ByteArrayInputStream(data);

        var file = new MockMultipartFile("file",
                "file.txt",
                "text/plain",
                mockFile);

        doReturn(DataSize.ofBytes(100)).when(leadExcelConfig).maxFileSize();

        assertThatThrownBy(() -> leadService.validateLeads(file)).isInstanceOf(FileUnsupportedExtensionException.class)
                .hasMessage("Ожидается файл формата xlsx, xls");
    }
}