package ru.sber.transport.telemechanic.controller.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.telemechanic.TestData.EMPLOYEE_1_ID;
import static ru.sber.transport.telemechanic.TestData.ORGANIZATION_1;
import static ru.sber.transport.telemechanic.TestData.ORGANIZATION_2;
import static ru.sber.transport.telemechanic.TestData.createDepartment1;
import static ru.sber.transport.telemechanic.TestData.createDepartment2;
import static ru.sber.transport.telemechanic.TestData.createEmployee1;
import static ru.sber.transport.telemechanic.TestData.createEmployee2;
import static ru.sber.transport.telemechanic.TestData.createEmployee3;
import static ru.sber.transport.telemechanic.TestData.createOrganization1;
import static ru.sber.transport.telemechanic.TestData.createOrganization2;
import static ru.sber.transport.telemechanic.TestData.createPosition1;
import static ru.sber.transport.telemechanic.TestData.createPosition2;
import static ru.sber.transport.telemechanic.TestData.createPosition3;
import static ru.sber.transport.telemechanic.TestData.createRequest;
import static ru.sber.transport.telemechanic.enumerate.RequestStatus.DONE;
import static ru.sber.transport.telemechanic.enumerate.RequestStatus.IN_PROGRESS;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_TELEMECHANIC;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.CheckPhotoRepository;
import ru.sber.transport.telemechanic.database.dao.CheckRepository;
import ru.sber.transport.telemechanic.database.dao.DepartmentRepository;
import ru.sber.transport.telemechanic.database.dao.EmployeeRepository;
import ru.sber.transport.telemechanic.database.dao.EwbRepository;
import ru.sber.transport.telemechanic.database.dao.OrganizationRepository;
import ru.sber.transport.telemechanic.database.dao.PositionRepository;
import ru.sber.transport.telemechanic.database.dao.RequestRepository;
import ru.sber.transport.telemechanic.database.dao.TransportRepository;
import ru.sber.transport.telemechanic.database.model.Check;
import ru.sber.transport.telemechanic.database.model.CheckPhoto;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Request;
import ru.sber.transport.telemechanic.database.model.Transport;
import ru.sber.transport.telemechanic.dto.ChecksStatus;
import ru.sber.transport.telemechanic.dto.EmployeeDto;
import ru.sber.transport.telemechanic.dto.FileData;
import ru.sber.transport.telemechanic.dto.MonitorCheckTreeDto;
import ru.sber.transport.telemechanic.dto.MonitoringRequestDto;
import ru.sber.transport.telemechanic.dto.MonitoringRequestListDto;
import ru.sber.transport.telemechanic.dto.RequestSearchDto;
import ru.sber.transport.telemechanic.dto.TransportDto;
import ru.sber.transport.telemechanic.enumerate.CheckType;
import ru.sber.transport.telemechanic.enumerate.CheckTypeMonitoring;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.enumerate.FileStatus;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.enumerate.TransportStatus;
import ru.sber.transport.telemechanic.service.FileService;

@DisplayName("Проверка мониторинга заявок")
@Slf4j
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
@EmbeddedPostgres
class MonitoringControllerImplTest {

    public static final String FILE_NAME = "1111.jpg";

    private Employee employee1;

    private Employee employee3;

    private Employee employee2;

    private static final String MONITORING_URI = "/monitoring";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private CheckRepository checkRepository;

    @Autowired
    private TransportRepository transportRepository;

    @Autowired
    private CheckPhotoRepository checkPhotoRepository;

    @Autowired
    private EwbRepository ewbRepository;

    @MockitoBean
    private FileService fileService;

    @MockitoBean
    private AuthorizationManager<?> manager;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(manager, ROLE_TELEMECHANIC.name());
    }

    @BeforeEach
    public void setUp() {
        var organization1 = createOrganization1();
        var organization2 = createOrganization2();
        var department1 = createDepartment1(organization1, null);
        var department2 = createDepartment2(organization2, null);
        var position1 = createPosition1(organization1);
        var position2 = createPosition2(organization1);
        var position3 = createPosition3(organization2);

        employee1 = createEmployee1(department1, position1);
        employee2 = createEmployee2(department1, position2);
        employee3 = createEmployee3(department2, position3);

        organizationRepository.saveAndFlush(organization1);
        organizationRepository.saveAndFlush(organization2);
        departmentRepository.saveAndFlush(department1);
        departmentRepository.saveAndFlush(department2);
        positionRepository.saveAndFlush(position1);
        positionRepository.saveAndFlush(position2);
        positionRepository.saveAndFlush(position3);
        employee1 = employeeRepository.saveAndFlush(employee1);
        employee2 = employeeRepository.saveAndFlush(employee2);
        employee3 = employeeRepository.saveAndFlush(employee3);
    }

    @Test
    @DisplayName("Получение заявок")
    void searchRequests() throws Exception {
        var transport1 = transportRepository.save(new Transport(UUID.randomUUID(),
            "А010ЕК50",
            "ВАЗ",
            "2101",
            100000,
            TransportStatus.IN_USE,
            "-",
            "-",
            40,
            new HashSet<>(Set.of(createOrganization1())),
            null, null, null));
        var transport2 = transportRepository.save(new Transport(UUID.randomUUID(),
            "А199ЕК799",
            "Москвич",
            "408",
            200000,
            TransportStatus.IN_USE,
            "-",
            "-",
            40,
            new HashSet<>(Set.of(createOrganization1())),
            null, null, null));
        var transport3 = transportRepository.save(new Transport(UUID.randomUUID(),
            "А010ЕК51",
            "Волга",
            "Сайбер",
            150000,
            TransportStatus.IN_USE,
            "-",
            "-",
            40,
            new HashSet<>(Set.of(createOrganization1())),
            null, null, null));

        var request1 = createRequest(employee1, transport1, ORGANIZATION_1.getId());
        request1.setStatus(DONE);
        request1.setHumanReadableId("HRU-00001-00002");

        var request2 = createRequest(employee2, transport2, ORGANIZATION_1.getId());
        request2.setStatus(RequestStatus.WARNING);
        request2.setHumanReadableId("HRU-00002-00001");

        var request3 = createRequest(employee1, transport1, ORGANIZATION_1.getId());
        request3.setStatus(RequestStatus.DECLINED);
        request3.setHumanReadableId("HRU-00001-00001");

        var request4 = createRequest(employee1, transport1, ORGANIZATION_1.getId());
        request4.setStatus(RequestStatus.CANCELED);
        request4.setHumanReadableId("HRU-00002-00002");

        var request5 = createRequest(employee3, transport3, ORGANIZATION_2.getId());
        request5.setStatus(IN_PROGRESS);
        request5.setHumanReadableId("HRU-10001-00002");

        var request6 = createRequest(employee3, transport3, ORGANIZATION_2.getId());
        request6.setStatus(RequestStatus.EXPIRED);
        request6.setHumanReadableId("HRU-10001-00003");

        var expected1 = requestRepository.save(request1);
        var expected2 = requestRepository.save(request2);
        var expected3 = requestRepository.save(request3);
        var expected4 = requestRepository.save(request4);
        var expected5 = requestRepository.save(request5);
        var expected6 = requestRepository.save(request6);

        var response = mockMvc.perform(
                post(MONITORING_URI)
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(buildSearchDtoString()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

        Map<String, Object> content =
            objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                });

        byte[] bytes = objectMapper.writeValueAsBytes(content.get("content"));
        List<MonitoringRequestListDto> actualList = objectMapper.readValue(bytes, new TypeReference<>() {
        });

        assertNotNull(actualList);
        assertEquals(4, actualList.size());
        checkRequestEqualityCommon(expected1, actualList.get(0), true);
        checkRequestEqualityCommon(expected2, actualList.get(1), true);
        checkRequestEqualityCommon(expected3, actualList.get(2), true);
        checkRequestEqualityCommon(expected4, actualList.get(3), true);
    }

    @Test
    @DisplayName("Получение файла")
    @SneakyThrows
    void downloadWorkOrder() {
        var file = new MockMultipartFile("file", FILE_NAME,
            MediaType.APPLICATION_PDF_VALUE,
            getClass().getClassLoader().getResourceAsStream("load/" + FILE_NAME));
        var fileDto = new FileData(MediaType.APPLICATION_PDF_VALUE, file.getBytes());
        var checkPhoto = CheckPhoto.builder()
            .checkId(UUID.randomUUID())
            .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
            .fileStatus(FileStatus.UPLOADED)
            .build();
        var checkPhotoInDB = checkPhotoRepository.save(checkPhoto);
        when(fileService.get(checkPhotoInDB.getId().toString())).thenReturn(fileDto);
        var response = mockMvc.perform(
                get(MONITORING_URI + "/photo/" + checkPhotoInDB.getId())
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(buildSearchDtoString()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsByteArray();
        assertNotNull(response);
        assertEquals(fileDto.stream().length, response.length);
    }

    @Test
    @DisplayName("Получение файла. Файл не найден")
    void downloadWorkOrderNoFile() throws Exception {
        var checkPhoto = CheckPhoto.builder()
            .checkId(UUID.randomUUID())
            .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
            .fileStatus(FileStatus.UPLOADED)
            .build();
        var checkPhotoInDB = checkPhotoRepository.save(checkPhoto);
        mockMvc.perform(
                get(MONITORING_URI + "/photo/" + checkPhotoInDB.getId())
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(buildSearchDtoString()))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Получение файла. Файл не найден в s3")
    void downloadWorkOrderNoFileS3() throws Exception {
        var checkPhoto = CheckPhoto.builder()
            .checkId(UUID.randomUUID())
            .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
            .fileStatus(FileStatus.UPLOADED)
            .build();
        var checkPhotoInDB = checkPhotoRepository.save(checkPhoto);
        when(fileService.get(any())).thenThrow(FileNotFoundException.class);
        mockMvc.perform(
                get(MONITORING_URI + "/photo/" + checkPhotoInDB.getId())
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(buildSearchDtoString()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Получение файла заказ-наряда. Не найдена сущность в базе данных")
    void downloadWorkOrderNoFileInDb() throws Exception {
        var checkPhoto = CheckPhoto.builder()
            .checkId(UUID.randomUUID())
            .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
            .fileStatus(FileStatus.UPLOADED)
            .build();
        var checkPhotoInDB = checkPhotoRepository.save(checkPhoto);
        when(fileService.get(any())).thenThrow(IOException.class);
        mockMvc.perform(
                get(MONITORING_URI + "/photo/" + checkPhotoInDB.getId())
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(buildSearchDtoString()))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Получение файла проверки. Нет в s3.")
    @SneakyThrows
    void downloadCheckPhotoNoFileInS3() {
        var logWatcher = new ListAppender<ILoggingEvent>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(MonitoringControllerImpl.class)).addAppender(logWatcher);
        var file = new MockMultipartFile("file", FILE_NAME,
            MediaType.APPLICATION_PDF_VALUE,
            getClass().getClassLoader().getResourceAsStream("load/" + FILE_NAME));
        var fileDto = new FileData(null, file.getBytes());
        var photoId = UUID.randomUUID();
        when(fileService.get(photoId.toString())).thenReturn(fileDto);
        mockMvc.perform(
                get(MONITORING_URI + "/photo/" + photoId)
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(buildSearchDtoString()))
            .andExpect(status().isNoContent());
        assertEquals(1, logWatcher.list.size());
        assertEquals(String.format("Фото не было загружено в хранилище s3. PhotoId=%s", photoId),
            logWatcher.list.get(0).getFormattedMessage());
    }

    @Test
    @DisplayName("Получение файла проверки. Без mimeType.")
    @SneakyThrows
    void downloadCheckPhotoWithoutMimeType() {
        var logWatcher = new ListAppender<ILoggingEvent>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(MonitoringControllerImpl.class)).addAppender(logWatcher);
        var file = new MockMultipartFile("file", FILE_NAME,
            null,
            getClass().getClassLoader().getResourceAsStream("load/" + FILE_NAME));
        var checkPhoto = CheckPhoto.builder()
            .checkId(UUID.randomUUID())
            .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
            .fileStatus(FileStatus.UPLOADED)
            .build();
        var checkPhotoInDB = checkPhotoRepository.save(checkPhoto);
        var fileDto = new FileData(null, file.getBytes());
        var photoId = checkPhotoInDB.getId();
        when(fileService.get(photoId.toString())).thenReturn(fileDto);
        mockMvc.perform(
                get(MONITORING_URI + "/photo/" + photoId)
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(buildSearchDtoString()))
            .andExpect(status().isNoContent());
        assertEquals(1, logWatcher.list.size());
        assertEquals(String.format("Не удалось получить расширение файла из хранилища s3. PhotoId=%s", photoId),
            logWatcher.list.get(0).getFormattedMessage());
    }

    @Test
    @DisplayName("Обновление заявки")
    void editRequestTest() throws Exception {
        var comment = "Nice";
        var checkComment = "Nice check";
        var transport1 = transportRepository.save(new Transport(UUID.randomUUID(),
            "А010ЕК50",
            "ВАЗ",
            "2101",
            100000,
            TransportStatus.IN_USE,
            "-",
            "-",
            40,
            new HashSet<>(Set.of(createOrganization1())),
            null, null, null));
        var request = createRequest(employee1, transport1, ORGANIZATION_1.getId());
        request.setStatus(DONE);
        request = requestRepository.save(request);
        var vehicleCheck = checkRepository.findAllByRequestId(request.getId()).stream()
            .filter(check -> check.getCheckType().equals(CheckType.VEHICLE_NUMBER))
            .findFirst().orElseThrow();
        var requestId = request.getId();
        var employeeDto = new EmployeeDto(employee1.getId(),
            employee1.getFirstName(),
            employee1.getLastName(),
            employee1.getPatronymic(),
            employee1.getPersonnelNumber(),
            employee1.getOrganization().getId(),
            employee1.getOrganization().getOfficialName());
        var updatedDto = new MonitoringRequestDto(request.getId(),
            request.getHumanReadableId(),
            request.getCreationTime(),
            RequestStatus.ON_THE_LINE,
            null,
            employeeDto,
            new TransportDto(transport1.getId(),
                transport1.getStateNumber(),
                transport1.getBrand(),
                transport1.getModel(),
                null,
                null,
                null),
            new ChecksStatus(21, 0, 0, 21),
            Collections.singletonList(new MonitorCheckTreeDto(vehicleCheck.getId(),
                CheckTypeMonitoring.valueOf(
                    vehicleCheck.getCheckType().name()),
                vehicleCheck.getCheckStatus(),
                vehicleCheck.getAttempt(),
                vehicleCheck.getCheckType().getMaxAttempt(),
                Collections.emptyList(),
                checkComment,
                Collections.emptyList())),
            comment
        );
        var updatedRequestString = objectMapper.writeValueAsString(updatedDto);
        mockMvc.perform(patch(MONITORING_URI + "/" + requestId)
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(updatedRequestString))
            .andExpect(status().isOk())
            .andReturn();
        var actual = requestRepository
            .findById(request.getId())
            .orElseThrow(() -> new EntityNotFoundException(Request.class, requestId));
        assertEquals(updatedDto.humanReadableId(), actual.getHumanReadableId());
        assertEquals(updatedDto.requestStatus(), actual.getStatus());
        assertEquals(checkComment, actual.getChecks().stream()
            .filter(value -> value.getCheckType().equals(CheckType.VEHICLE_NUMBER))
            .findFirst().orElseThrow().getComment()
        );
        assertEquals(comment, actual.getComment());

        var doneRequest = createRequest(employee1, transport1, ORGANIZATION_1.getId());
        doneRequest.setStatus(DONE);
        doneRequest = requestRepository.save(doneRequest);
        var inProgressDto = new MonitoringRequestDto(doneRequest.getId(),
            doneRequest.getHumanReadableId(),
            doneRequest.getCreationTime(),
            IN_PROGRESS,
            null,
            employeeDto,
            new TransportDto(transport1.getId(),
                transport1.getStateNumber(),
                transport1.getBrand(),
                transport1.getModel(),
                null, null, null),
            new ChecksStatus(21, 0, 0, 21),
            Collections.emptyList(),
            comment
        );
        var updatedDoneRequest = objectMapper.writeValueAsString(inProgressDto);
        mockMvc.perform(patch(MONITORING_URI + "/" + doneRequest.getId())
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(updatedDoneRequest))
            .andExpect(status().isOk());

        var inProgressRequest = createRequest(employee1, transport1, ORGANIZATION_1.getId());
        inProgressRequest.setStatus(IN_PROGRESS);
        inProgressRequest = requestRepository.save(inProgressRequest);
        var onTheLineDto = new MonitoringRequestDto(inProgressRequest.getId(),
            inProgressRequest.getHumanReadableId(),
            inProgressRequest.getCreationTime(),
            RequestStatus.ON_THE_LINE,
            null,
            employeeDto,
            new TransportDto(transport1.getId(),
                transport1.getStateNumber(),
                transport1.getBrand(),
                transport1.getModel(),
                null, null, null),
            new ChecksStatus(21, 0, 0, 21),
            Collections.emptyList(),
            comment
        );
        var updatedInProgressRequest = objectMapper.writeValueAsString(onTheLineDto);
        mockMvc.perform(patch(MONITORING_URI + "/" + inProgressRequest.getId())
                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                    .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(updatedInProgressRequest))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Получение несуществующей заявки по идентификатору")
    void getNonExistentTest() throws Exception {
        mockMvc.perform(
                get(MONITORING_URI + "/" + UUID.randomUUID())
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение заявки по идентификатору")
    void getRequestTest() throws Exception {
        var vehicle1 = transportRepository.save(new Transport(UUID.randomUUID(),
            "А010ЕК50",
            "ВАЗ",
            "2101",
            100000,
            TransportStatus.IN_USE,
            "-",
            "-",
            40,
            new HashSet<>(Set.of(createOrganization1())),
            null, null, null));
        var request = createRequest(employee1, vehicle1, ORGANIZATION_1.getId());
        request.setStatus(DONE);
        var expected = requestRepository.save(request);
        var response = mockMvc.perform(
                get(MONITORING_URI + "/" + expected.getId())
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
        var actual = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8),
            MonitoringRequestDto.class);
        checkRequestEqualityCommon(expected, actual, true);
    }

    @Test
    @DisplayName("Получение заявок, поиск по номеру заявки")
    void searchRequestsByNumber() throws Exception {
        var transport1 = transportRepository.save(new Transport(UUID.randomUUID(),
            "А010ЕК50",
            "ВАЗ",
            "2101",
            100000,
            TransportStatus.IN_USE,
            "-",
            "-",
            40,
            new HashSet<>(Set.of(createOrganization1())),
            null, null, null));
        var transport2 = transportRepository.save(new Transport(UUID.randomUUID(),
            "А199ЕК799",
            "Москвич",
            "408",
            200000,
            TransportStatus.IN_USE,
            "-",
            "-",
            40,
            new HashSet<>(Set.of(createOrganization1())),
            null, null, null));
        var transport3 = transportRepository.save(new Transport(UUID.randomUUID(),
            "А010ЕК51",
            "Волга",
            "Сайбер",
            150000,
            TransportStatus.IN_USE,
            "-",
            "-",
            40,
            new HashSet<>(Set.of(createOrganization1())),
            null, null, null));
        var request1 = createRequest(employee1, transport1, ORGANIZATION_1.getId());
        request1.setStatus(DONE);
        request1.setHumanReadableId("HRU-00001-00002");
        var request2 = createRequest(employee2, transport2, ORGANIZATION_1.getId());
        request2.setStatus(DONE);
        request2.setHumanReadableId("HRO-00002-00001");
        var request3 = createRequest(employee3, transport3, ORGANIZATION_2.getId());
        request3.setStatus(DONE);
        request3.setHumanReadableId("HRU-00001-00001");
        var request4 = createRequest(employee1,
            transportRepository.save(new Transport(UUID.randomUUID(),
                "А170ЕК729",
                "ВАЗ",
                "2101",
                100000,
                TransportStatus.IN_USE,
                "-",
                "-",
                40,
                new HashSet<>(Set.of(ORGANIZATION_1)),
                null, null, null
            )),
            ORGANIZATION_1.getId());
        request4.setStatus(RequestStatus.CANCELED);
        var request5 = createRequest(employee1,
            transportRepository.save(new Transport(UUID.randomUUID(),
                "А170ЕК739",
                "ВАЗ",
                "2101",
                100000,
                TransportStatus.IN_USE,
                "-",
                "-",
                40,
                new HashSet<>(Set.of(ORGANIZATION_1)),
                null, null, null
            )),
            ORGANIZATION_1.getId());
        request5.setStatus(RequestStatus.DECLINED);
        request5.setHumanReadableId("HRU-00001-00001");
        var request6 = createRequest(employee1,
            transportRepository.save(new Transport(UUID.randomUUID(),
                "А170ЕК749",
                "ВАЗ",
                "2101",
                100000,
                TransportStatus.IN_USE,
                "-",
                "-",
                40,
                new HashSet<>(Set.of(ORGANIZATION_1)),
                null, null, null
            )),
            ORGANIZATION_1.getId());
        request6.setStatus(RequestStatus.FINISHED);
        request6.setHumanReadableId("HRU-00002-00002");
        var request7 = createRequest(employee1,
            transportRepository.save(new Transport(UUID.randomUUID(),
                "А170ЕК759",
                "ВАЗ",
                "2101",
                100000,
                TransportStatus.IN_USE,
                "-",
                "-",
                40,
                new HashSet<>(Set.of(ORGANIZATION_1)),
                null, null, null
            )),
            ORGANIZATION_1.getId());
        request7.setStatus(RequestStatus.EXPIRED);
        request7.setHumanReadableId("HRU-10001-00002");
        var request8 = createRequest(employee1,
            transportRepository.save(new Transport(UUID.randomUUID(),
                "А170ЕК769",
                "ВАЗ",
                "2101",
                100000,
                TransportStatus.IN_USE,
                "-",
                "-",
                40,
                new HashSet<>(Set.of(ORGANIZATION_1)),
                null, null, null
            )),
            ORGANIZATION_1.getId());
        request8.setStatus(IN_PROGRESS);
        request8.setHumanReadableId("HRU-10001-00003");
        var request9 = createRequest(employee1,
            transportRepository.save(new Transport(UUID.randomUUID(),
                "А170ЕК779",
                "ВАЗ",
                "2101",
                100000,
                TransportStatus.IN_USE,
                "-",
                "-",
                40,
                new HashSet<>(Set.of(ORGANIZATION_1)),
                null, null, null
            )),
            ORGANIZATION_1.getId());
        request9.setStatus(RequestStatus.ON_THE_LINE);
        request9.setHumanReadableId("HRU-00007-00002");
        var request10 = createRequest(employee3,
            transportRepository.save(new Transport(UUID.randomUUID(),
                "А170ЕК789",
                "ВАЗ",
                "2101",
                100000,
                TransportStatus.IN_USE,
                "-",
                "-",
                40,
                new HashSet<>(Set.of(ORGANIZATION_1)),
                null, null, null
            )),
            ORGANIZATION_2.getId());
        request10.setHumanReadableId("HRU-00007-00003");
        request10.setStatus(RequestStatus.WARNING);
        var request11 = createRequest(employee1,
            transportRepository.save(new Transport(UUID.randomUUID(),
                "А170ЕК799",
                "ВАЗ",
                "2101",
                100000,
                TransportStatus.IN_USE,
                "-",
                "-",
                40,
                new HashSet<>(Set.of(ORGANIZATION_1)),
                null, null, null
            )),
            ORGANIZATION_1.getId());
        request11.setHumanReadableId("HRU-00170-00004");
        request11.setStatus(DONE);
        var expected1 = requestRepository.save(request1);
        var expected2 = requestRepository.save(request2);
        var expected3 = requestRepository.save(request3);
        var expected4 = requestRepository.save(request4);
        var expected5 = requestRepository.save(request5);
        var expected6 = requestRepository.save(request6);
        var expected7 = requestRepository.save(request7);
        var expected8 = requestRepository.save(request8);
        var expected9 = requestRepository.save(request9);
        var expected10 = requestRepository.save(request10);
        var expected11 = requestRepository.save(request11);

        var response1 = mockMvc.perform(
                post(MONITORING_URI)
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(new RequestSearchDto(
                        null,
                        null,
                        "00001",
                        null,
                        new RequestSearchDto.PageSetting(0, 10)))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
        Map<String, Object> content1 =
            objectMapper.readValue(response1.getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                });
        List<MonitoringRequestListDto> actualList1 =
            objectMapper.readValue(objectMapper.writeValueAsBytes(content1.get("content")), new TypeReference<>() {
            });

        assertEquals(3, actualList1.size());
        checkRequestEqualityCommon(expected1, actualList1.get(0), true);
        checkRequestEqualityCommon(expected2, actualList1.get(1), true);
        checkRequestEqualityCommon(expected5, actualList1.get(2), true);

        var response2 = mockMvc.perform(
                post(MONITORING_URI)
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(new RequestSearchDto(
                        null,
                        null,
                        "сни",
                        null,
                        new RequestSearchDto.PageSetting(0, 10)))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
        Map<String, Object> content2 =
            objectMapper.readValue(response2.getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                });
        List<MonitoringRequestListDto> actualList2 =
            objectMapper.readValue(objectMapper.writeValueAsBytes(content2.get("content")), new TypeReference<>() {
            });

        assertEquals(0, actualList2.size());

        var response3 = mockMvc.perform(
                post(MONITORING_URI)
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(new RequestSearchDto(
                        null,
                        null,
                        "170",
                        Set.of(DONE),
                        new RequestSearchDto.PageSetting(0, 10)))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
        Map<String, Object> content3 =
            objectMapper.readValue(response3.getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                });
        List<MonitoringRequestListDto> actualList3 =
            objectMapper.readValue(objectMapper.writeValueAsBytes(content3.get("content")), new TypeReference<>() {
            });

        assertEquals(1, actualList3.size());
        checkRequestEqualityCommon(expected11, actualList3.get(0), true);

        var response4 = mockMvc.perform(
                post(MONITORING_URI)
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("""
                        {
                            "organizationId": "%s",
                            "pageSetting": {
                                "page": 0,
                                "size": 7
                            }
                        }
                        """.formatted(employee3.getOrganization().getId())))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
        Map<String, Object> content4 =
            objectMapper.readValue(response4.getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                });
        List<MonitoringRequestListDto> actualList4 = objectMapper.readValue(
            objectMapper.writeValueAsBytes(content4.get("content")),
            new TypeReference<>() {
            });

        assertEquals(0, actualList4.size());

        mockMvc.perform(
                post(MONITORING_URI)
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("""
                        {
                            "departmentIds": ["%s"],
                            "pageSetting": {
                                "page": 0,
                                "size": 7
                            }
                        }
                        """.formatted(employee3.getOrganization().getId())))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
        Map<String, Object> content5 =
            objectMapper.readValue(response4.getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                });
        List<MonitoringRequestListDto> actualList5 = objectMapper.readValue(
            objectMapper.writeValueAsBytes(content5.get("content")),
            new TypeReference<>() {
            });

        assertEquals(0, actualList5.size());
    }

    @Test
    void downloadPhotoByPhotoIdThatNotInDB() throws Exception {
        var logWatcher = new ListAppender<ILoggingEvent>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(MonitoringControllerImpl.class)).addAppender(logWatcher);
        var id = UUID.randomUUID();
        mockMvc.perform(
                get(MONITORING_URI + "/photo/" + id)
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(buildSearchDtoString()))
            .andExpect(status().isNoContent());
        assertEquals(1, logWatcher.list.size());
        assertEquals(String.format("Фото не было загружено в хранилище s3. PhotoId=%s", id),
            logWatcher.list.get(0).getFormattedMessage());
    }

    @Test
    void downloadPhotoNotUploadedStatus() throws Exception {
        var logWatcher = new ListAppender<ILoggingEvent>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(MonitoringControllerImpl.class)).addAppender(logWatcher);
        var checkPhoto = CheckPhoto.builder()
            .checkId(UUID.randomUUID())
            .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
            .fileStatus(FileStatus.NOT_UPLOADED)
            .build();
        var checkPhotoInDB = checkPhotoRepository.save(checkPhoto);
        mockMvc.perform(
                get(MONITORING_URI + "/photo/" + checkPhotoInDB.getId())
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(buildSearchDtoString()))
            .andExpect(status().isNoContent());
        assertEquals(1, logWatcher.list.size());
        assertEquals(String.format("Фото не было загружено в хранилище s3. PhotoId=%s", checkPhotoInDB.getId()),
            logWatcher.list.get(0).getFormattedMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    @Sql(scripts = {
        "/scripts/basic_corp_structure.sql",
        "/scripts/ewb_integration_test.sql"
    })
    void shouldDeclineEwbWithRequest() {
        var request = """
            {
              "comment": "request comment",
              "checks": [
                  {
                      "id": "d9d50a1e-1fc4-4458-ade6-f5297324a399",
                      "comment": "OIL_LEVEL comment"
                  },
                  {
                      "id": "d9d51a1e-1fc4-4458-ade6-f5297334a389",
                      "comment": "ODOMETER comment"
                  }
              ]
            }
            """;
        mockMvc.perform(
                patch(MONITORING_URI + "/d9d51a1e-1fc4-4458-ade6-f5297304a389/ewb")
                    .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                        .authorities(new SimpleGrantedAuthority(ROLE_TELEMECHANIC.name())))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(request))
            .andExpect(status().isOk());

        var ewb = ewbRepository.findEwbByRequestId(UUID.fromString("d9d51a1e-1fc4-4458-ade6-f5297304a389"))
            .orElseThrow(() -> new JUnitException("ewb not found"));
        assertEquals(EwbStatus.TELEMECH_DECLINED, ewb.getStatus());
        assertEquals(RequestStatus.DECLINED, ewb.getRequest().getStatus());
        assertEquals("request comment", ewb.getRequest().getComment());
        var checks = ewb.getRequest().getChecks();
        assertEquals("OIL_LEVEL comment", checks.stream()
            .filter(check -> check.getCheckType().equals(CheckType.OIL_LEVEL))
            .map(Check::getComment)
            .findAny()
            .orElseThrow(() -> new JUnitException("check comment not found")));
        assertEquals("ODOMETER comment", checks.stream()
            .filter(check -> check.getCheckType().equals(CheckType.ODOMETER))
            .map(Check::getComment)
            .findAny()
            .orElseThrow(() -> new JUnitException("check comment not found")));
    }

    private String buildSearchDtoString() {
        return """
            {
                "dispatcherRequest": true,
                "sortSetting": {
                    "property": "CREATION_DATE",
                    "directionAsc": false
                },
                "pageSetting": {
                    "page": 0,
                    "size": 7
                }
            }""";
    }

    private void checkRequestEqualityCommon(Request expected, MonitoringRequestListDto actual, boolean checkIds) {
        if (checkIds) {
            assertEquals(expected.getId(), actual.id());
            assertEquals(expected.getHumanReadableId(), actual.humanReadableId());
        }
        assertEquals(expected.getStatus(), actual.requestStatus());
        assertEquals(expected.getTransport().getStateNumber(), actual.stateNumber());
        assertEquals(expected.getAuthor().getOrganization().getOfficialName(), actual.organizationName());
    }

    private void checkRequestEqualityCommon(Request expected, MonitoringRequestDto actual, boolean checkIds) {
        if (checkIds) {
            assertEquals(expected.getId(), actual.id());
            assertEquals(expected.getHumanReadableId(), actual.humanReadableId());
        }
        checkEmployeeEquality(expected.getAuthor(), actual.author());
        checkVehicleEquality(expected.getTransport(), actual.transport());
        assertEquals(expected.getStatus(), actual.requestStatus());
    }

    private void checkVehicleEquality(Transport expected, TransportDto actual) {
        assertEquals(expected.getBrand(), actual.brand());
        assertEquals(expected.getModel(), actual.model());
        assertEquals(expected.getStateNumber(), actual.stateNumber());
    }

    private void checkEmployeeEquality(Employee expected, EmployeeDto actual) {
        if ((Objects.isNull(expected) && Objects.nonNull(actual)) || (Objects.nonNull(expected) && Objects.isNull(
            actual))) {
            fail();
        }
        if (Objects.nonNull(expected)) {
            assertEquals(expected.getId(), actual.id());
            assertEquals(expected.getFirstName(), actual.firstName());
            assertEquals(expected.getLastName(), actual.lastName());
            assertEquals(expected.getPatronymic(), actual.patronymic());
            assertEquals(expected.getDepartment().getOrganization().getId(), actual.organizationId());
            assertEquals(expected.getOrganization().getId(), actual.organizationId());
            assertEquals(expected.getOrganization().getOfficialName(), actual.organizationOfficialName());
        }
    }

}
