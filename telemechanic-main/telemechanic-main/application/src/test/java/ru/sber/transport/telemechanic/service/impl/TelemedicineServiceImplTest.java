package ru.sber.transport.telemechanic.service.impl;

import lombok.SneakyThrows;
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
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.config.properties.TelemedicineProperties;
import ru.sber.transport.telemechanic.database.dao.EwbRepository;
import ru.sber.transport.telemechanic.database.dao.MedicRequestHistoryRepository;
import ru.sber.transport.telemechanic.database.dao.MedicRequestRepository;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.database.projection.TelemedicineSearchProjection;
import ru.sber.transport.telemechanic.dto.FileData;
import ru.sber.transport.telemechanic.dto.PageSettingDto;
import ru.sber.transport.telemechanic.dto.ewb.KorusEwbTitleResponse;
import ru.sber.transport.telemechanic.dto.telemedicine.*;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.enumerate.InspectionType;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;
import ru.sber.transport.telemechanic.exception.*;
import ru.sber.transport.telemechanic.human_readable_id.constant.Prefix;
import ru.sber.transport.telemechanic.mapper.TelemedicineMapper;
import ru.sber.transport.telemechanic.service.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.sber.transport.telemechanic.enumerate.TelemedicineStatus.IN_PROGRESS;

@ExtendWith(MockitoExtension.class)
class TelemedicineServiceImplTest {
    
    @Mock
    private TelemedicineMapper mapper;
    @Mock
    private MedicRequestRepository medicRequestRepository;
    @Mock
    private MedicRequestHistoryRepository historyRepository;
    @Mock
    private SQGenerator sqGenerator;
    @Mock
    private EwbRepository ewbRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private OrganizationMedicalLicenseService organizationMedicalLicenseService;
    @Mock
    private EwbTariffService ewbTariffService;
    @Mock
    private DrivingLicenseService drivingLicenseService;
    @Mock
    private EwbTitleService ewbTitleService;
    @Mock
    private FileService fileService;
    @Mock
    private TelemedicIntegrationService telemedicIntegrationService;
    @Mock
    private TelemedicineProperties telemedicineProperties;
    @Mock
    private MedicContractorService medicContractorService;
    @Mock
    private EdfOperatorClientService edfOperatorClientService;
    @Mock
    private RequestService requestService;
    @Captor
    private ArgumentCaptor<MedicRequest> medicRequestArgumentCaptor;
    @Captor
    private ArgumentCaptor<Ewb> ewbArgumentCaptor;
    @Mock
    protected Clock clock;
    @InjectMocks
    private TelemedicineServiceImpl service;
    public static final LocalDate CURRENT_DATE = LocalDate.of(2023, 2, 10);
    public final Clock fixedClock = Clock.fixed(CURRENT_DATE.atStartOfDay().toInstant(ZoneOffset.UTC),
                                                ZoneId.of(ZoneOffset.UTC.getId()));
    
    @Test
    void search() {
        var userId = UUID.randomUUID();
        var request = new TelemedicineSearchRequest(
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                null,
                null,
                null,
                new PageSettingDto(0, 10)
        );
        var expected1 = Instancio.create(TelemedicineSearchResponse.class);
        var expected2 = Instancio.create(TelemedicineSearchResponse.class);
        var pageRequest = PageRequest.of(0, 10);
        var expected = new PageImpl<>(List.of(expected1, expected2), pageRequest, 2);
        var projection1 = createTelemedicineSearchProjection(expected1);
        var projection2 = createTelemedicineSearchProjection(expected2);
        var projectionPageable = new PageImpl<>(List.of(projection1, projection2), pageRequest, 2);
        doReturn(projectionPageable).when(medicRequestRepository).searchMedicRequests(any(UUID.class),
                                                                                      anyString(),
                                                                                      any(UUID.class),
                                                                                      any(),
                                                                                      any(),
                                                                                      any(),
                                                                                      any(),
                                                                                      any(),
                                                                                      any(PageRequest.class));
        doReturn(List.of(expected1, expected2))
                .when(mapper).listTelemedicineSearchProjectionToListTelemedicineSearchResponse(List.of(projection1, projection2));
        var actual = service.search(request, userId);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
    
    @Test
    void create() {
        var humanReadableId = "TL-00000-000";
        var ewb1 = Instancio.of(Ewb.class)
                            .set(field(Ewb::getStartDate), CURRENT_DATE)
                            .set(field(Ewb::getMedicRequest), null)
                            .set(field(Ewb::getTimeZone), "UTC+03:00")
                            .create();
        var ewb2 = Instancio.of(Ewb.class)
                            .set(field(Ewb::getStartDate), CURRENT_DATE.plusDays(1))
                            .set(field(Ewb::getMedicRequest), null)
                            .set(field(Ewb::getTimeZone), "UTC+03:00")
                            .create();
        var ewb3 = Instancio.of(Ewb.class)
                            .set(field(Ewb::getStartDate), CURRENT_DATE)
                            .set(field(Ewb::getTimeZone), "UTC+03:00")
                            .create();
        var ewb4 = Instancio.of(Ewb.class)
                            .set(field(Ewb::getStartDate), CURRENT_DATE)
                            .set(field(Ewb::getMedicRequest), null)
                            .set(field(Ewb::getTimeZone), "UTC+03:00")
                            .create();
        var wrongId = UUID.randomUUID();
        var ewb1Id = ewb1.getId();
        var ewb2Id = ewb2.getId();
        var ewb3Id = ewb3.getId();
        var ewb4Id = ewb4.getId();
        var technicContract = Instancio.of(EwbContract.class)
                                       .set(field(EwbContract::getInspectionType), InspectionType.TECHNIC)
                                       .create();
        var medicContract = Instancio.of(EwbContract.class)
                                     .set(field(EwbContract::getInspectionType), InspectionType.MEDIC)
                                     .create();
        var medicRequest = Instancio.create(MedicRequest.class);
        var expected = new MedicRequest()
                .setHumanReadableId(humanReadableId)
                .setCreationTime(CURRENT_DATE.atStartOfDay())
                .setStatus(IN_PROGRESS)
                .setOrganizationId(medicContract.getOrganizationId());
        doReturn(Optional.empty()).when(ewbRepository).findById(wrongId);
        doReturn(Optional.of(ewb1)).when(ewbRepository).findById(ewb1Id);
        doReturn(Optional.of(ewb2)).when(ewbRepository).findById(ewb2Id);
        doReturn(Optional.of(ewb3)).when(ewbRepository).findById(ewb3Id);
        doReturn(Optional.of(ewb4)).when(ewbRepository).findById(ewb4Id);
        doReturn(Map.of(
                InspectionType.TECHNIC, technicContract,
                InspectionType.MEDIC, medicContract
                       ))
                .when(ewbTariffService).getActiveContractByDepartmentId(ewb1.getTariffDepartmentId(), true);
        doReturn(Collections.emptyMap()).when(ewbTariffService).getActiveContractByDepartmentId(ewb4.getTariffDepartmentId(), true);
        doReturn(humanReadableId).when(sqGenerator).getNextId(Prefix.TL, ewb1.getOrganization().getDigitId());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(medicRequest).when(medicRequestRepository).save(medicRequestArgumentCaptor.capture());
        service.create(ewb1Id);
        assertThat(ewb1.getStatus()).isEqualTo(EwbStatus.MEDIC_IN_PROGRESS);
        assertThat(ewb1.getMedicRequest())
                .usingRecursiveComparison()
                .isEqualTo(medicRequest);
        assertThat(ewb1.getOrganizationMedicalLicenseId()).isEqualTo(medicContract.getOrganizationMedicalLicenseId());
        var actual = medicRequestArgumentCaptor.getValue();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        assertThatExceptionOfType(EwbNotFoundException.class)
                .isThrownBy(() -> service.create(wrongId))
                .withMessage("ЭПЛ с идентификатором id=%s не найден!", wrongId);
        assertThatExceptionOfType(MedicRequestCreateException.class)
                .isThrownBy(() -> service.create(ewb2Id))
                .withMessage("Невозможно создать заявку телемедицины для ЭПЛ с идентификатором id=%s", ewb2Id);
        assertThatExceptionOfType(MedicRequestCreateException.class)
                .isThrownBy(() -> service.create(ewb3Id))
                .withMessage("Невозможно создать заявку телемедицины для ЭПЛ с идентификатором id=%s", ewb3Id);
        assertThatExceptionOfType(EwbTariffConflictException.class)
                .isThrownBy(() -> service.create(ewb4Id))
                .withMessage("У подразделения нет тарифов на оказание услуг");
    }
    
    @Test
    void getMedicRequest() {
        var medicRequestId1 = UUID.randomUUID();
        var medicRequestId2 = UUID.randomUUID();
        var ewb1 = Instancio.create(Ewb.class);
        var ewb2 = Instancio.create(Ewb.class);
        var drivingLicense = Instancio.create(DrivingLicense.class);
        var organizationMedicalLicense = Instancio.create(OrganizationMedicalLicense.class);
        var expected1 = Instancio.of(GetTelemedicineDto.class)
                                 .set(field(GetTelemedicineDto.Medic::series), organizationMedicalLicense.getSeries())
                                 .set(field(GetTelemedicineDto.Medic::number), organizationMedicalLicense.getNumber())
                                 .set(field(GetTelemedicineDto.Medic::issueDate), organizationMedicalLicense.getIssueDate())
                                 .set(field(GetTelemedicineDto.Medic::expiryDate), organizationMedicalLicense.getExpiryDate())
                                 .set(field(GetTelemedicineDto.Medic::decisionTime), ewb1.getMedicDecisionTime())
                                 .set(field(GetTelemedicineDto.Driver::tin), ewb1.getDriver().getTin())
                                 .set(field(GetTelemedicineDto.Driver::drivingLicenseId), drivingLicense.getId())
                                 .set(field(GetTelemedicineDto.Driver::series), drivingLicense.getSeries())
                                 .set(field(GetTelemedicineDto.Driver::number), drivingLicense.getNumber())
                                 .set(field(GetTelemedicineDto.Driver::issueDate), drivingLicense.getIssueDate())
                                 .create();
        var expected2 = Instancio.of(GetTelemedicineDto.class)
                                 .set(field(GetTelemedicineDto.Driver::tin), ewb2.getDriver().getTin())
                                 .set(field(GetTelemedicineDto.Driver::drivingLicenseId), drivingLicense.getId())
                                 .set(field(GetTelemedicineDto.Driver::series), drivingLicense.getSeries())
                                 .set(field(GetTelemedicineDto.Driver::number), drivingLicense.getNumber())
                                 .set(field(GetTelemedicineDto.Driver::issueDate), drivingLicense.getIssueDate())
                                 .create();
        doReturn(Optional.of(ewb1)).when(ewbRepository).findByMedicRequestId(medicRequestId1);
        doReturn(Optional.of(ewb2)).when(ewbRepository).findByMedicRequestId(medicRequestId2);
        doReturn(drivingLicense).when(drivingLicenseService).get(ewb1.getDriverLicenseId());
        doReturn(drivingLicense).when(drivingLicenseService).get(ewb2.getDriverLicenseId());
        doReturn(organizationMedicalLicense).when(organizationMedicalLicenseService).getMedicalLicense(ewb1.getTariffDepartmentId());
        doReturn(null).when(organizationMedicalLicenseService).getMedicalLicense(ewb2.getTariffDepartmentId());
        doReturn(expected1).when(mapper).ewbToTelemedicineDto(ewb1);
        doReturn(expected2).when(mapper).ewbToTelemedicineDto(ewb2);
        var actual1 = service.getMedicRequest(medicRequestId1);
        assertThat(actual1)
                .usingRecursiveComparison()
                .isEqualTo(expected1);
        var actual2 = service.getMedicRequest(medicRequestId2);
        assertThat(actual2)
                .usingRecursiveComparison()
                .isEqualTo(expected2);
    }
    
    @Test
    void decline() {
        var declinedTelemedicineRequest = Instancio.create(DeclinedTelemedicineRequest.class);
        var userId = UUID.randomUUID();
        var ewb1 = Instancio.of(Ewb.class)
                            .set(field(Ewb::getMedicRequest), Instancio.of(MedicRequest.class)
                                                                       .set(field(MedicRequest::getStatus), IN_PROGRESS)
                                                                       .create())
                            .create();
        var ewb2 = Instancio.of(Ewb.class)
                            .set(field(Ewb::getMedicRequest), Instancio.of(MedicRequest.class)
                                                                       .set(field(MedicRequest::getStatus), TelemedicineStatus.DECLINED)
                                                                       .create())
                            .create();
        var wrongId = UUID.randomUUID();
        var medicRequestId1 = ewb1.getMedicRequest().getId();
        var medicRequestId2 = ewb2.getMedicRequest().getId();
        var medic = Instancio.create(Employee.class);
        doReturn(Optional.empty()).when(ewbRepository).findByMedicRequestId(wrongId);
        doReturn(Optional.of(ewb1)).when(ewbRepository).findByMedicRequestId(medicRequestId1);
        doReturn(Optional.of(ewb2)).when(ewbRepository).findByMedicRequestId(medicRequestId2);
        doReturn(medic).when(employeeService).getByUserId(userId);
        doReturn(ewb1).when(ewbRepository).save(ewbArgumentCaptor.capture());
        doReturn(new TelemedicineDeclineResultDto(
                declinedTelemedicineRequest.systPressure(),
                declinedTelemedicineRequest.dyastPressure(),
                declinedTelemedicineRequest.pulse(),
                declinedTelemedicineRequest.temperature(),
                declinedTelemedicineRequest.bloodAlcohol(),
                declinedTelemedicineRequest.comment(),
                CURRENT_DATE.atStartOfDay()
        )).when(mapper).declinedTelemedicineRequestToTelemedicineDeclineResultDto(any(DeclinedTelemedicineRequest.class));
        service.decline(medicRequestId1, declinedTelemedicineRequest, userId);
        var actual = ewbArgumentCaptor.getValue();
        assertThat(actual.getMedicRequest().getStatus()).isEqualTo(TelemedicineStatus.DECLINED);
        assertThat(actual.getMedicRequest().getSystPressure()).isEqualTo(declinedTelemedicineRequest.systPressure());
        assertThat(actual.getMedicRequest().getDyastPressure()).isEqualTo(declinedTelemedicineRequest.dyastPressure());
        assertThat(actual.getMedicRequest().getPulse()).isEqualTo(declinedTelemedicineRequest.pulse());
        assertThat(actual.getMedicRequest().getTemperature()).isEqualTo(declinedTelemedicineRequest.temperature());
        assertThat(actual.getMedicRequest().getBloodAlcohol()).isEqualTo(declinedTelemedicineRequest.bloodAlcohol());
        assertThat(actual.getMedicRequest().getComment()).isEqualTo(declinedTelemedicineRequest.comment());
        assertThat(actual.getStatus()).isEqualTo(EwbStatus.MEDIC_DECLINED);
        assertThat(actual.getMedic()).isEqualTo(medic);
        assertThat(actual.getMedicDecisionTime()).isEqualTo(CURRENT_DATE.atStartOfDay());
        assertThatExceptionOfType(MedicRequestNotFoundException.class)
                .isThrownBy(() -> service.decline(wrongId, declinedTelemedicineRequest, userId))
                .withMessage("Заявка медика с идентификатором id=%s не найдена!", wrongId);
        assertThatExceptionOfType(DeclinedTelemedicineException.class)
                .isThrownBy(() -> service.decline(medicRequestId2, declinedTelemedicineRequest, userId))
                .withMessage("""
                             Заявка с идентификатором id=%s находится в статусе "%s", и не может быть отменена.
                             """,
                             medicRequestId2,
                             ewb2.getMedicRequest().getStatus());
    }
    
    @Test
    @DisplayName("Авто обновление статуса мед заявок: заявки не найдены")
    void statusAutoUpdateWhenNotFound() {
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(List.of()).when(medicRequestRepository)
                           .findAllByStatusAndCreationTimeBetween(IN_PROGRESS,
                                                                  LocalDateTime.now(fixedClock).minusDays(2),
                                                                  LocalDateTime.now(fixedClock).minusDays(1));
        service.statusAutoUpdate();
        
        verify(medicRequestRepository, never()).saveAll(anyList());
        verify(historyRepository, never()).saveAll(anyList());
    }
    
    @Test
    @DisplayName("Авто обновление статуса мед заявок: успех")
    void statusAutoUpdateSuccess() {
        var historyCaptor = ArgumentCaptor.forClass(List.class);
        var requestCaptor = ArgumentCaptor.forClass(List.class);
        var comment = "Статус заявки изменен пользователем: Система (Планировщик)";
        var request = Instancio.of(MedicRequest.class)
                               .set(field(MedicRequest::getStatus), IN_PROGRESS)
                               .set(field(MedicRequest::getCreationTime), LocalDateTime.now(fixedClock).minusDays(1))
                               .create();
        var requestHistory = Instancio.of(MedicRequestHistory.class)
                                      .set(field(MedicRequestHistory::getMedicRequestId), request.getId())
                                      .set(field(MedicRequestHistory::getOldStatus), IN_PROGRESS)
                                      .set(field(MedicRequestHistory::getStatus), TelemedicineStatus.EXPIRED)
                                      .set(field(MedicRequestHistory::getComment), comment)
                                      .set(field(MedicRequestHistory::getInitiator), null)
                                      .create();
        
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(List.of(request)).when(medicRequestRepository)
                                  .findAllByStatusAndCreationTimeBetween(IN_PROGRESS,
                                                                         LocalDateTime.now(fixedClock).minusDays(2),
                                                                         LocalDateTime.now(fixedClock).minusDays(1));
        
        doReturn(requestHistory).when(mapper)
                                .medicRequestToMedicRequestHistory(request, TelemedicineStatus.EXPIRED, comment, null);
        service.statusAutoUpdate();
        
        verify(medicRequestRepository, times(1)).updateMedicRequestsStatus(requestCaptor.capture());
        verify(historyRepository, times(1)).saveAll(historyCaptor.capture());
        
        var list = requestCaptor.getValue();
        assertThat(list).hasSize(1);
        var requestCapture = (UUID) list.get(0);
        assertThat(requestCapture).isNotNull();
        assertThat(requestCapture).isEqualTo(request.getId());
        
        var historyList = historyCaptor.getValue();
        assertThat(historyList).hasSize(1);
        var history = (MedicRequestHistory) historyList.get(0);
        assertThat(history).isNotNull();
        assertThat(history.getStatus()).isEqualTo(TelemedicineStatus.EXPIRED);
        assertThat(history.getInitiator()).isNull();
        assertThat(history.getChangeTime()).isNotNull();
        assertThat(history.getMedicRequestId()).isEqualTo(request.getId());
        assertThat(history.getComment()).isEqualTo(comment);
        assertThat(history.getOldStatus()).isEqualTo(TelemedicineStatus.IN_PROGRESS);
    }
    
    private TelemedicineSearchProjection createTelemedicineSearchProjection(TelemedicineSearchResponse source) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(TelemedicineSearchProjection.class);
        projection.setId(source.id());
        projection.setHumanReadableId(source.humanReadableId());
        projection.setEwbId(source.ewbId());
        projection.setEwbHumanReadableId(source.ewbHumanReadableId());
        projection.setOrganizationName(source.organizationName());
        projection.setStatus(source.status());
        projection.setCreationTime(source.creationTime());
        projection.setDriverFullName(source.driverFullName());
        return projection;
    }
    
    @Test
    @SneakyThrows
    void createTelemedicContractType() {
        var ewbId = UUID.randomUUID();
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getMedicRequest), null)
                           .set(field(Ewb::getStartDate), CURRENT_DATE)
                .set(field(Ewb::getTimeZone), "UTC+03:00")
                           .create();
        var contract = Instancio.of(EwbContract.class)
                                .set(field(EwbContract::getInspectionType), InspectionType.TELEMEDIC)
                                .create();
        var ewbTitle = Instancio.of(EwbTitle.class)
                                .set(field(EwbTitle::getType), EwbTitleType.FIRST)
                                .create();
        var fileData = Instancio.create(FileData.class);
        
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(Optional.of(ewb)).when(ewbRepository).findById(ewbId);
        doReturn(Map.of(InspectionType.TELEMEDIC, contract)).when(ewbTariffService)
                                                            .getActiveContractByDepartmentId(ewb.getTariffDepartmentId(), true);
        doReturn(ewbTitle).when(ewbTitleService).getEwbTitleByEwbIdAndType(ewb.getId(), EwbTitleType.FIRST);
        doReturn(fileData).when(fileService).get(anyString());
        doReturn("1234").when(telemedicineProperties).getContractorApiToken();
        doNothing().when(telemedicIntegrationService).sendTitleToContractor(anyString(), anyString(), any(Ewb.class), anyString());
        
        service.create(ewbId);
        
        verify(telemedicIntegrationService).sendTitleToContractor(anyString(), anyString(), any(Ewb.class), anyString());
        verify(medicRequestRepository).save(medicRequestArgumentCaptor.capture());
        
        assertThat(medicRequestArgumentCaptor.getValue())
                .extracting(MedicRequest::getCreationTime,
                            MedicRequest::getStatus,
                            MedicRequest::getOrganizationId)
                .containsExactly(CURRENT_DATE.atStartOfDay(),
                                 IN_PROGRESS,
                                 contract.getOrganizationId());
        
        doThrow(IOException.class).when(fileService).get(anyString());
        
        assertThatExceptionOfType(TelemedicClientException.class)
                .isThrownBy(() -> service.create(ewbId))
                .withMessage("Ошибка создания заявки на медицинский осмотр! Обратитесь к диспетчеру.");
        
        ewb.getDriver().setSnils(null);
        assertThatExceptionOfType(SnilsNotFoundException.class)
                .isThrownBy(() -> service.create(ewbId))
                .withMessage("У водителя id=%s не найден СНИЛС. Обратитесь к диспетчеру.".formatted(ewb.getDriver().getId()));
    }
    
    @Test
    @SneakyThrows
    void addResult() {
        var title = new MockMultipartFile(
                "data",
                "title.xml",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "test".getBytes()
        );
        var signature = new MockMultipartFile(
                "signature",
                "signature.bin",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "signature".getBytes()
        );
        var exam = Instancio.of(ExamInfo.class)
                            .set(field(ExamInfo::medicRequestStatus), true)
                            .create();
        var request = Instancio.of(TelemedicineResultRequest.class)
                               .set(field(TelemedicineResultRequest::exam), exam)
                               .create();
        var apiKey = "apiKey";
        doReturn(null).when(telemedicineProperties).getToken();
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(() -> service.addResult(title, signature, request, apiKey))
                .withMessage("Token is invalid");
        
        doReturn("some_different_token").when(telemedicineProperties).getToken();
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(() -> service.addResult(title, signature, request, apiKey))
                .withMessage("Token is invalid");
        
        doReturn("apiKey").when(telemedicineProperties).getToken();
        var korusId = UUID.randomUUID();
        var chainId = UUID.randomUUID();
        var response = new KorusEwbTitleResponse(korusId, chainId);
        doReturn(response).when(edfOperatorClientService).sendTitle(
                request.medicInfo().fio(),
                request.name(),
                title.getBytes(),
                new String(signature.getBytes())
                                                                   );
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        var signatureS3FileName = UUID.randomUUID().toString();
        doReturn(signatureS3FileName).when(ewbTitleService).saveTitleToExternalStorage(any(InputStream.class), any(InputStream.class), anyString());
        doReturn(Optional.empty()).when(ewbRepository).findByEwbUuid(request.ewbUuid());
        assertThatExceptionOfType(EwbNotFoundException.class)
                .isThrownBy(() -> service.addResult(title, signature, request, apiKey))
                .withMessage("ЭПЛ с идентификатором id=%s не найден!".formatted(request.ewbUuid()));
        var ewb = Instancio.of(Ewb.class).create();
        doReturn(Optional.of(ewb)).when(ewbRepository).findByEwbUuid(request.ewbUuid());
        doNothing().when(ewbTitleService).saveEwbTitle(ewb, EwbTitleType.SECOND, request.name(), request.name(), LocalDateTime.now(fixedClock),
                                                       response, signatureS3FileName);
        var medicContractor = Instancio.of(MedicContractor.class).create();
        doReturn(medicContractor).when(medicContractorService).saveIfNotExistsByPersonnelNumber(request.medicInfo());
        doReturn(Instancio.create(Request.class)).when(requestService).createEmpty(any(), any(), anyBoolean());
        service.addResult(title, signature, request, apiKey);
        
        verify(ewbRepository).save(ewbArgumentCaptor.capture());
        var savedEwb = ewbArgumentCaptor.getValue();
        assertThat(savedEwb)
                .extracting(
                        Ewb::getMedicContractor,
                        Ewb::getMedicDecisionTime,
                        Ewb::getStatus
                           )
                .containsExactly(
                        medicContractor,
                        request.exam().medicDecisionDateTime(),
                        EwbStatus.TELEMECH_IN_PROGRESS
                                );
        assertThat(savedEwb.getMedicRequest())
                .extracting(
                        MedicRequest::getStatus,
                        MedicRequest::getSystPressure,
                        MedicRequest::getDyastPressure,
                        MedicRequest::getPulse,
                        MedicRequest::getComment,
                        MedicRequest::getTemperature,
                        MedicRequest::getBloodAlcohol
                           )
                .containsExactly(
                        request.exam().medicRequestStatus() ? TelemedicineStatus.DONE : TelemedicineStatus.DECLINED,
                        request.exam().systPressure(),
                        request.exam().dyastPressure(),
                        request.exam().pulse(),
                        request.exam().comment(),
                        request.exam().temperature(),
                        request.exam().alcohol()
                                );
    }
    
}