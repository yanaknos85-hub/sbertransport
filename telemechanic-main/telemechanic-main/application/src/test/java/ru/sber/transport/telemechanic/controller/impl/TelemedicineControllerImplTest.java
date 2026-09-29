package ru.sber.transport.telemechanic.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.mockito.ArgumentCaptor;
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
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.client.KorusClient;
import ru.sber.transport.telemechanic.database.dao.EwbRepository;
import ru.sber.transport.telemechanic.database.dao.MedicContractorRepository;
import ru.sber.transport.telemechanic.database.model.Ewb;
import ru.sber.transport.telemechanic.database.model.MedicContractor;
import ru.sber.transport.telemechanic.database.model.MedicRequest;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.dto.PageSettingDto;
import ru.sber.transport.telemechanic.dto.ewb.KorusEwbTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.KorusTitleRequest;
import ru.sber.transport.telemechanic.dto.telemedicine.*;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.enumerate.Role;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;
import ru.sber.transport.telemechanic.exception.MedicRequestNotFoundException;
import ru.sber.transport.telemechanic.service.FileService;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.*;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.telemechanic.TestData.EMPLOYEE_1_ID;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_ADMIN_DATA_MASTER;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
class TelemedicineControllerImplTest {
    public static final LocalDate currentDate = LocalDate.of(2023, 2, 10);
    public final Clock fixedClock = Clock.fixed(currentDate.atStartOfDay().toInstant(ZoneOffset.UTC),
                                                ZoneId.of(ZoneOffset.UTC.getId()));
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected EwbRepository ewbRepository;
    @Autowired
    private MedicContractorRepository medicContractorRepository;
    @MockitoBean
    private KorusClient korusClient;
    @MockitoBean
    private FileService fileService;
    @MockitoBean
    protected Clock clock;
    @MockitoBean
    protected AuthorizationManager<?> manager;
    
    @BeforeEach
    protected void mockAuthorization() {
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_DATA_MASTER.name());
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void search() {
        mockMvc.perform(post("/telemedicine/search")
                                .with(jwt().jwt(builder -> builder.jti("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(new TelemedicineSearchRequest(null,
                                                                                                       null,
                                                                                                       null,
                                                                                                       null,
                                                                                                       null,
                                                                                                       new PageSettingDto(0, 10)))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.totalElements").value(6))
               .andExpect(jsonPath("$.content[0].id").value("6c157938-3604-42a0-bc86-f16526031d5b"))
               .andExpect(jsonPath("$.content[1].id").value("b5a6b2cf-06d3-41f9-9571-43e86ffffa71"))
               .andExpect(jsonPath("$.content[2].id").value("75542a8a-b815-43d6-9a33-63046b19f030"))
               .andExpect(jsonPath("$.content[3].id").value("5456bbff-1f45-4f9a-90d6-85304513d54c"))
               .andExpect(jsonPath("$.content[4].id").value("8ad5e9c6-604f-4baa-861f-d7d4c320eb72"));
        mockMvc.perform(post("/telemedicine/search")
                                .with(jwt().jwt(builder -> builder.jti("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(new TelemedicineSearchRequest(
                                        "юнков",
                                        UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"),
                                        Set.of(TelemedicineStatus.DONE, TelemedicineStatus.IN_PROGRESS, TelemedicineStatus.DECLINED),
                                        new DateRange(LocalDateTime.of(2024, 6, 1, 10, 0), LocalDateTime.of(2024, 8, 1, 10, 0)),
                                        new DateRange(LocalDateTime.of(2024, 6, 1, 10, 0), LocalDateTime.of(2024, 8, 1, 10, 0)),
                                        new PageSettingDto(0, 10)))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.totalElements").value(2))
               .andExpect(jsonPath("$.content[0].id").value("8ad5e9c6-604f-4baa-861f-d7d4c320eb72"));
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/ewb_contract.sql"
    })
    void create() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        ewbRepository.findById(UUID.fromString("140e4734-4909-4bf3-b98f-f24c90d8005f"))
                     .ifPresent(ewb -> {
                         ewb.setMedicRequest(null);
                         ewbRepository.save(ewb);
                     });
        mockMvc.perform(post("/telemedicine/140e4734-4909-4bf3-b98f-f24c90d8005f")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name()))))
               .andExpect(status().isOk());
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/ewb_contract.sql"
    })
    void getMedicRequest() {
        mockMvc.perform(get("/telemedicine/75542a8a-b815-43d6-9a33-63046b19f030")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name()))))
               .andExpect(status().isOk());
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/ewb_contract.sql"
    })
    void declined() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        var request = new DeclinedTelemedicineRequest(120, 80, 85, new BigDecimal("36.6"), new BigDecimal("0.5"), "DRUNKED");
        mockMvc.perform(patch("/telemedicine/6c157938-3604-42a0-bc86-f16526031d5b/declined")
                                .with(jwt().jwt(builder -> builder.jti("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk());
        
        var ewb = ewbRepository.findByMedicRequestId(UUID.fromString("6c157938-3604-42a0-bc86-f16526031d5b"))
                               .orElseThrow(() -> new MedicRequestNotFoundException(UUID.fromString("6c157938-3604-42a0-bc86-f16526031d5b")));
        assertNotNull(ewb);
        assertEquals(TelemedicineStatus.DECLINED, ewb.getMedicRequest().getStatus());
        assertEquals(request.systPressure(), ewb.getMedicRequest().getSystPressure());
        assertEquals(request.dyastPressure(), ewb.getMedicRequest().getDyastPressure());
        assertEquals(request.pulse(), ewb.getMedicRequest().getPulse());
        assertEquals(request.temperature(), ewb.getMedicRequest().getTemperature());
        assertEquals(request.bloodAlcohol(), ewb.getMedicRequest().getBloodAlcohol());
        assertEquals(request.comment(), ewb.getMedicRequest().getComment());
        assertEquals("95c2bd4f-3a79-4b30-8d7e-9308e751b40a", ewb.getMedic().getId().toString());
        
        mockMvc.perform(patch("/telemedicine/75542a8a-b815-43d6-9a33-63046b19f030/declined")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isConflict());
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/ewb_contract.sql"
    })
    void addResult() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        var korusId = UUID.randomUUID();
        var chainId = UUID.randomUUID();
        var inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
        doReturn(new KorusEwbTitleResponse(korusId, chainId)).when(korusClient).sendTitle(any(KorusTitleRequest.class));
        doNothing().when(fileService).upload(inputStreamCaptor.capture(), anyString(), anyString());
        
        var title = new MockMultipartFile("data", "second_title.xml", MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                          this.getClass().getClassLoader().getResource("ewb/titles/first/title.xml").openStream()
                                                            .readAllBytes());
        var signature = new MockMultipartFile("signature", "signature.bin", MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                              this.getClass().getClassLoader().getResource("ewb/titles/first/signature.bin").openStream()
                                                  .readAllBytes());
        var request = new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE,
                                            """
                                            {
                                                "name": "second_title_name",
                                                "ewbUuid": "140e4734-4909-4bf3-b98f-f24c90d8005f",
                                                "medicInfo": {
                                                    "fio": "СКАКУН АЛЕКСЕЙ",
                                                    "personalNumber": "1234",
                                                    "organization": "ООО 123",
                                                    "department": "Отдел 123",
                                                    "position": "Должность 123",
                                                    "serialNumber": "12345",
                                                    "serialEndDateTime": "2020-01-01T01:11:59+03:00"
                                                },
                                                "exam": {
                                                    "medicRequestStatus": true,
                                                    "creationDateTime": "2020-01-28T00:00:00+03:00",
                                                    "medicDecisionDateTime": "2020-01-01T00:00:00+03:00",
                                                    "systPressure": 120,
                                                    "dyastPressure": 80,
                                                    "pulse": 85,
                                                    "temperature": 36.6,
                                                    "alcohol": 0.0,
                                                    "comment": ""
                                                }
                                            }
                                            """.getBytes());
        
        
        mockMvc.perform(multipart("/telemedicine/result")
                                .file(title)
                                .file(signature)
                                .file(request)
                                .header("X-Api-Key", "c2JlclRyYW5zcG9ydC1EaW1lY29UZWxlbWVkaWNpbmUwMzExMjAyNTExMDBSZXF1aXJlZFRvT2J0YWluRGF0YU9uVGhlRHJpdmVyc1JlbW90ZU1lZGljYWxFeGFtaW5hdGlvbkJlZm9yZUdvaW5nT25UaGVSb2Fk")
                                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                       )
               .andExpect(status().isOk());
        
        var ewb = ewbRepository.findByEwbUuid(UUID.fromString("140e4734-4909-4bf3-b98f-f24c90d8005f"))
                               .orElseThrow(() -> new JUnitException("ewb not found"));
        var medicContractor = medicContractorRepository.findByPersonnelNumber("1234")
                                                       .orElseThrow(() -> new JUnitException("medic contractor not found"));
        assertThat(medicContractor)
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
                        "СКАКУН АЛЕКСЕЙ",
                        "1234",
                        "ООО 123",
                        "Отдел 123",
                        "Должность 123",
                        "12345",
                        LocalDateTime.of(2020, 1, 1, 1, 11, 59)
                                );
        assertThat(ewb)
                .extracting(
                        Ewb::getMedicDecisionTime,
                        Ewb::getStatus
                           )
                .containsExactly(
                        LocalDateTime.of(2020, 1, 1, 0, 0),
                        EwbStatus.TELEMECH_IN_PROGRESS
                                );
        assertThat(ewb.getMedicContractor())
                .usingRecursiveComparison()
                .isEqualTo(medicContractor);
        
        assertThat(ewb.getMedicRequest())
                .extracting(
                        MedicRequest::getCreationTime,
                        MedicRequest::getStatus,
                        MedicRequest::getSystPressure,
                        MedicRequest::getDyastPressure,
                        MedicRequest::getPulse,
                        MedicRequest::getComment,
                        MedicRequest::getTemperature,
                        MedicRequest::getBloodAlcohol
                           )
                .containsExactly(
                        LocalDateTime.of(2020, 1, 28, 0, 0, 0),
                        TelemedicineStatus.DONE,
                        120,
                        80,
                        85,
                        "",
                        BigDecimal.valueOf(36.6),
                        BigDecimal.valueOf(0.0)
                                );
        
        assertThat(ewb.getRequest())
                .isNotNull();
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/ewb_contract.sql"
    })
    void addResult_decline() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        var korusId = UUID.randomUUID();
        var chainId = UUID.randomUUID();
        var inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
        doReturn(new KorusEwbTitleResponse(korusId, chainId)).when(korusClient).sendTitle(any(KorusTitleRequest.class));
        doNothing().when(fileService).upload(inputStreamCaptor.capture(), anyString(), anyString());
        
        var title = new MockMultipartFile("data", "second_title.xml", MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                          this.getClass().getClassLoader().getResource("ewb/titles/first/title.xml").openStream()
                                              .readAllBytes());
        var signature = new MockMultipartFile("signature", "signature.bin", MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                              this.getClass().getClassLoader().getResource("ewb/titles/first/signature.bin").openStream()
                                                  .readAllBytes());
        var request = new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE,
                                            """
                                            {
                                                "name": "second_title_name",
                                                "ewbUuid": "140e4734-4909-4bf3-b98f-f24c90d8005f",
                                                "medicInfo": {
                                                    "fio": "СКАКУН АЛЕКСЕЙ",
                                                    "personalNumber": "1234",
                                                    "organization": "ООО 123",
                                                    "department": "Отдел 123",
                                                    "position": "Должность 123",
                                                    "serialNumber": "12345",
                                                    "serialEndDateTime": "2020-01-01T01:11:59+03:00"
                                                },
                                                "exam": {
                                                    "medicRequestStatus": false,
                                                    "creationDateTime": "2020-01-28T00:00:00+03:00",
                                                    "medicDecisionDateTime": "2020-01-01T00:00:00+03:00",
                                                    "systPressure": 120,
                                                    "dyastPressure": 80,
                                                    "pulse": 85,
                                                    "temperature": 36.6,
                                                    "alcohol": 0.5,
                                                    "comment": "ПЬЯНЬ"
                                                }
                                            }
                                            """.getBytes());
        
        
        mockMvc.perform(multipart("/telemedicine/result")
                                .file(title)
                                .file(signature)
                                .file(request)
                                .header("X-Api-Key", "c2JlclRyYW5zcG9ydC1EaW1lY29UZWxlbWVkaWNpbmUwMzExMjAyNTExMDBSZXF1aXJlZFRvT2J0YWluRGF0YU9uVGhlRHJpdmVyc1JlbW90ZU1lZGljYWxFeGFtaW5hdGlvbkJlZm9yZUdvaW5nT25UaGVSb2Fk")
                                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                       )
               .andExpect(status().isOk());
        
        var ewb = ewbRepository.findByEwbUuid(UUID.fromString("140e4734-4909-4bf3-b98f-f24c90d8005f"))
                               .orElseThrow(() -> new JUnitException("ewb not found"));
        var medicContractor = medicContractorRepository.findByPersonnelNumber("1234")
                                                       .orElseThrow(() -> new JUnitException("medic contractor not found"));
        assertThat(medicContractor)
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
                        "СКАКУН АЛЕКСЕЙ",
                        "1234",
                        "ООО 123",
                        "Отдел 123",
                        "Должность 123",
                        "12345",
                        LocalDateTime.of(2020, 1, 1, 1, 11, 59)
                                );
        assertThat(ewb)
                .extracting(
                        Ewb::getMedicDecisionTime,
                        Ewb::getStatus
                           )
                .containsExactly(
                        LocalDateTime.of(2020, 1, 1, 0, 0),
                        EwbStatus.MEDIC_DECLINED
                                );
        assertThat(ewb.getMedicContractor())
                .usingRecursiveComparison()
                .isEqualTo(medicContractor);
        
        assertThat(ewb.getMedicRequest())
                .extracting(
                        MedicRequest::getCreationTime,
                        MedicRequest::getStatus,
                        MedicRequest::getSystPressure,
                        MedicRequest::getDyastPressure,
                        MedicRequest::getPulse,
                        MedicRequest::getComment,
                        MedicRequest::getTemperature,
                        MedicRequest::getBloodAlcohol
                           )
                .containsExactly(
                        LocalDateTime.of(2020, 1, 28, 0, 0, 0),
                        TelemedicineStatus.DECLINED,
                        120,
                        80,
                        85,
                        "ПЬЯНЬ",
                        BigDecimal.valueOf(36.6),
                        BigDecimal.valueOf(0.5)
                                );
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/ewb_contract.sql"
    })
    void decline() {
        var preTestEwb = ewbRepository.findByEwbUuid(UUID.fromString("340e4734-4909-4bf3-b98f-f24c90d8005f"))
                               .orElseThrow(() -> new JUnitException("ewb not found"));
        preTestEwb.setMedicDecisionTime(null)
                .setMedic(null);
        ewbRepository.saveAndFlush(preTestEwb);
        
        var request = new TelemedicineResultRequest(
                null,
                UUID.fromString("340e4734-4909-4bf3-b98f-f24c90d8005f"),
                new MedicInfo(
                        "СКАКУН АЛЕКСЕЙ",
                        "1234",
                        "ООО 123",
                        "Отдел 123",
                        "Должность 123",
                        "12345",
                        LocalDateTime.of(3000, 1, 1, 0, 0, 0)),
                new ExamInfo(
                        false,
                        LocalDateTime.of(2020, 1, 1, 0, 0, 0),
                        LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                        120,
                        80,
                        85,
                        BigDecimal.valueOf(36.6),
                        BigDecimal.valueOf(0.5),
                        "ПЬЯНЬ"
                )
        );
        var requestJson = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/telemedicine/340e4734-4909-4bf3-b98f-f24c90d8005f/decline")
                                .header("X-Api-Key",
                                        "c2JlclRyYW5zcG9ydC1EaW1lY29UZWxlbWVkaWNpbmUwMzExMjAyNTExMDBSZXF1aXJlZFRvT2J0YWluRGF0YU9uVGhlRHJpdmVyc1JlbW90ZU1lZGljYWxFeGFtaW5hdGlvbkJlZm9yZUdvaW5nT25UaGVSb2Fk")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(requestJson)
                       )
               .andExpect(status().isOk());
        
        var ewb = ewbRepository.findByEwbUuid(UUID.fromString("340e4734-4909-4bf3-b98f-f24c90d8005f"))
                               .orElseThrow(() -> new JUnitException("ewb not found"));
        assertThat(ewb)
                .isNotNull()
                .extracting(
                        Ewb::getStatus,
                        Ewb::getMedicDecisionTime,
                        Ewb::getMedic
                           )
                .containsExactly(
                        EwbStatus.MEDIC_DECLINED,
                        request.exam().medicDecisionDateTime(),
                        null
                                );
        assertThat(ewb.getMedicContractor())
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
                        request.medicInfo().fio(),
                        request.medicInfo().personalNumber(),
                        request.medicInfo().organization(),
                        request.medicInfo().department(),
                        request.medicInfo().position(),
                        request.medicInfo().serialNumber(),
                        request.medicInfo().serialEndDateTime()
                                );
        assertThat(ewb.getMedicRequest())
                .extracting(
                        MedicRequest::getStatus,
                        MedicRequest::getSystPressure,
                        MedicRequest::getDyastPressure,
                        MedicRequest::getPulse,
                        MedicRequest::getTemperature,
                        MedicRequest::getBloodAlcohol,
                        MedicRequest::getComment
                           )
                .containsExactly(
                        TelemedicineStatus.DECLINED,
                        request.exam().systPressure(),
                        request.exam().dyastPressure(),
                        request.exam().pulse(),
                        request.exam().temperature(),
                        request.exam().alcohol(),
                        request.exam().comment()
                                );
        
    }
}