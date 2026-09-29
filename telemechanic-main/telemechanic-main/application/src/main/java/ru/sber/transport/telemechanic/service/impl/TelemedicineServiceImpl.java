package ru.sber.transport.telemechanic.service.impl;

import java.time.*;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.config.properties.TelemedicineProperties;
import ru.sber.transport.telemechanic.database.dao.EwbRepository;
import ru.sber.transport.telemechanic.database.dao.MedicRequestHistoryRepository;
import ru.sber.transport.telemechanic.database.dao.MedicRequestRepository;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.CreateRequestDto;
import ru.sber.transport.telemechanic.dto.FileData;
import ru.sber.transport.telemechanic.dto.PageSettingDto;
import ru.sber.transport.telemechanic.dto.telemedicine.*;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.enumerate.InspectionType;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;
import ru.sber.transport.telemechanic.exception.*;
import ru.sber.transport.telemechanic.helper.EwbHelper;
import ru.sber.transport.telemechanic.helper.SearchHelper;
import ru.sber.transport.telemechanic.human_readable_id.constant.Prefix;
import ru.sber.transport.telemechanic.mapper.TelemedicineMapper;
import ru.sber.transport.telemechanic.service.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static ru.sber.transport.telemechanic.enumerate.EwbStatus.TELEMECH_IN_PROGRESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemedicineServiceImpl implements TelemedicineService {
    
    private final TelemedicineMapper telemedicineMapper;
    private final MedicRequestRepository medicRequestRepository;
    private final MedicRequestHistoryRepository historyRepository;
    private final EwbRepository ewbRepository;
    private final SQGenerator sqGenerator;
    private final EmployeeService employeeService;
    private final OrganizationMedicalLicenseService organizationMedicalLicenseService;
    private final EwbTariffService ewbTariffService;
    private final DrivingLicenseService drivingLicenseService;
    private final TelemedicIntegrationService telemedicIntegrationService;
    private final EwbTitleService ewbTitleService;
    private final FileService fileService;
    private final TelemedicineProperties telemedicineProperties;
    private final MedicContractorService medicContractorService;
    private final EdfOperatorClientService edfOperatorClientService;
    private final RequestService requestService;
    private final Clock clock;
    
    @Override
    public Page<TelemedicineSearchResponse> search(TelemedicineSearchRequest request, UUID userId) {
        var result = medicRequestRepository.searchMedicRequests(userId,
                                                                request.searchText(),
                                                                request.organizationId(),
                                                                SearchHelper.getStatusSet(request.requestStatusSet()),
                                                                SearchHelper.getDateRangeStart(request.creationTime()),
                                                                SearchHelper.getDateRangeEnd(request.creationTime()),
                                                                SearchHelper.getDateRangeStart(request.medicDecision()),
                                                                SearchHelper.getDateRangeEnd(request.medicDecision()),
                                                                preparePageRequest(request.pageSetting()));
        return new PageImpl<>(
                telemedicineMapper.listTelemedicineSearchProjectionToListTelemedicineSearchResponse(result.getContent()),
                result.getPageable(),
                result.getTotalElements());
    }
    
    @Override
    @Transactional
    public void create(UUID ewbId) {
        var ewb = ewbRepository.findById(ewbId)
                               .orElseThrow(() -> new EwbNotFoundException(ewbId));
        var timeZone = ewb.getTimeZone();
        var currentDate = LocalDateTime.now(clock).atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of(timeZone)).toLocalDate();
        if (!ewb.getStartDate().isEqual(currentDate) || Objects.nonNull(ewb.getMedicRequest())) {
            throw new MedicRequestCreateException(ewbId);
        }
        var ewbContract = ewbTariffService.getActiveContractByDepartmentId(ewb.getTariffDepartmentId(), true).entrySet().stream()
                                          .filter(entry -> InspectionType.getMedicineTypes().contains(entry.getKey()))
                                          .findFirst()
                                          .map(Map.Entry::getValue)
                                          .orElseThrow(() -> new EwbTariffConflictException("У подразделения нет тарифов на оказание услуг"));
        
        if (InspectionType.TELEMEDIC.equals(ewbContract.getInspectionType())) {
            if (ewb.getDriver().getSnils() == null) {
                throw new SnilsNotFoundException(ewb.getDriver().getId());
            }
            var firstTitle = ewbTitleService.getEwbTitleByEwbIdAndType(ewb.getId(), EwbTitleType.FIRST);
            var name = firstTitle.getFileName();
            var title = getFile(firstTitle.getS3FileName());
            var signature = getFile(firstTitle.getSignatureS3FileName());
            var content = EwbHelper.zipFiles(title.stream(), signature.stream(), name);
            telemedicIntegrationService.sendTitleToContractor(name, content, ewb, telemedicineProperties.getContractorApiToken());
        }
        
        var savedMedicRequest = medicRequestRepository.save(createMedicRequest(ewb.getOrganization().getDigitId(),
                                                                               ewbContract.getOrganizationId()));
        ewb.setStatus(EwbStatus.MEDIC_IN_PROGRESS)
           .setMedicRequest(savedMedicRequest)
           .setOrganizationMedicalLicenseId(ewbContract.getOrganizationMedicalLicenseId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public GetTelemedicineDto getMedicRequest(UUID medicRequestId) {
        var ewb = ewbRepository.findByMedicRequestId(medicRequestId)
                               .orElseThrow(() -> new MedicRequestNotFoundException(medicRequestId));
        var organizationMedicalLicense = organizationMedicalLicenseService.getMedicalLicense(ewb.getTariffDepartmentId());
        var getTelemedicineDto = telemedicineMapper.ewbToTelemedicineDto(ewb);
        var drivingLicense = drivingLicenseService.get(ewb.getDriverLicenseId());
        return enrichGetTelemedicineDto(getTelemedicineDto, ewb.getDriver(), drivingLicense, organizationMedicalLicense, ewb.getMedicDecisionTime());
    }
    
    @Override
    @Transactional
    public void statusAutoUpdate() {
        var requests = medicRequestRepository.findAllByStatusAndCreationTimeBetween(TelemedicineStatus.IN_PROGRESS,
                                                                                    LocalDateTime.now(clock).minusDays(2),
                                                                                    LocalDateTime.now(clock).minusDays(1));
        if (!requests.isEmpty()) {
            var history = requests.stream()
                                  .map(request -> telemedicineMapper.medicRequestToMedicRequestHistory(
                                          request, TelemedicineStatus.EXPIRED,
                                          "Статус заявки изменен пользователем: Система (Планировщик)", null))
                                  .toList();
            medicRequestRepository.updateMedicRequestsStatus(requests.stream().map(MedicRequest::getId).toList());
            historyRepository.saveAll(history);
        }
    }
    
    @Override
    @SneakyThrows
    @Transactional
    public void addResult(MultipartFile data, MultipartFile signature, TelemedicineResultRequest request, String apiKey) {
        checkApiKey(apiKey);
        saveAndSendResult(data, signature, request);
    }
    
    @SneakyThrows
    private void saveAndSendResult(MultipartFile data, MultipartFile signature, TelemedicineResultRequest request) {
        var response = edfOperatorClientService.sendTitle(request.medicInfo().fio(), request.name(), data.getBytes(),
                                                          new String(signature.getBytes()));
        var ewb = ewbRepository.findByEwbUuid(request.ewbUuid())
                               .orElseThrow(() -> new EwbNotFoundException(request.ewbUuid()));
        var sentAt = LocalDateTime.now(clock);
        try (var dataStream = data.getInputStream(); var signatureStream = signature.getInputStream()) {
            var signatureS3FileName = ewbTitleService.saveTitleToExternalStorage(dataStream, signatureStream, request.name());
            ewbTitleService.saveEwbTitle(ewb, EwbTitleType.SECOND, request.name(), request.name(), sentAt, response, signatureS3FileName);
        }
        
        var medic = medicContractorService.saveIfNotExistsByPersonnelNumber(request.medicInfo());
        enrichEwbWithMedicAndExam(ewb, request.exam(), medic);
        if (request.exam().medicRequestStatus()) {
            var createdRequest = requestService.createEmpty(new CreateRequestDto(ewb.getTransport().getId()), ewb.getDriver().getEmployee(), true);
            ewb.setRequest(createdRequest)
               .setStatus(TELEMECH_IN_PROGRESS);
        } else {
            ewb.setStatus(EwbStatus.MEDIC_DECLINED);
        }
        ewbRepository.save(ewb);
    }
    
    private void enrichEwbWithMedicAndExam(Ewb ewb, ExamInfo exam, MedicContractor medic) {
        var medicRequest = ewb.getMedicRequest();
        enrichMedicRequest(exam, medicRequest);
        ewb.setMedicContractor(medic)
           .setMedicRequest(medicRequest)
           .setMedicDecisionTime(exam.medicDecisionDateTime());
    }
    
    @Override
    @Transactional
    public void decline(UUID medicRequestId, DeclinedTelemedicineRequest request, UUID userId) {
        var ewb = ewbRepository.findByMedicRequestId(medicRequestId)
                               .orElseThrow(() -> new MedicRequestNotFoundException(medicRequestId));
        var result = telemedicineMapper.declinedTelemedicineRequestToTelemedicineDeclineResultDto(request);
        decline(ewb, result);
        var medic = employeeService.getByUserId(userId);
        ewb.setMedic(medic);
        ewbRepository.save(ewb);
    }
    
    @Override
    @Transactional
    public void decline(UUID ewbUuid, TelemedicineResultRequest request, String apiKey) {
        checkApiKey(apiKey);
        var ewb = ewbRepository.findByEwbUuid(ewbUuid)
                .orElseThrow(() -> new EwbNotFoundException(ewbUuid));
        var result = telemedicineMapper.telemedicineResultRequestExamToTelemedicineDeclineResultDto(request.exam());
        decline(ewb, result);
        var medicContractor = medicContractorService.saveIfNotExistsByPersonnelNumber(request.medicInfo());
        ewb.setMedicContractor(medicContractor);
        ewbRepository.save(ewb);
    }
    
    void decline(Ewb ewb, TelemedicineDeclineResultDto result) {
        if (ewb.getMedicRequest().getStatus() != TelemedicineStatus.IN_PROGRESS) {
            throw new DeclinedTelemedicineException(ewb.getMedicRequest().getId(), ewb.getMedicRequest().getStatus());
        }
        ewb.getMedicRequest()
           .setStatus(TelemedicineStatus.DECLINED)
           .setSystPressure(result.systPressure())
           .setDyastPressure(result.dyastPressure())
           .setPulse(result.pulse())
           .setTemperature(result.temperature())
           .setBloodAlcohol(result.alcohol())
           .setComment(result.comment());
        ewb.setStatus(EwbStatus.MEDIC_DECLINED)
                .setMedicDecisionTime(result.decisionTime());
    }
    
    private static PageRequest preparePageRequest(PageSettingDto pageSettingDto) {
        var sorting = Sort.by(MedicRequest_.STATUS)
                          .descending().and(Sort.by(MedicRequest_.CREATION_TIME));
        return Objects.nonNull(pageSettingDto) ?
               PageRequest.of(pageSettingDto.page(), pageSettingDto.size(), sorting) :
               PageRequest.of(0, 20, sorting);
    }
    
    private MedicRequest createMedicRequest(Long digitId, UUID organizationId) {
        var medicRequest = new MedicRequest();
        var humanReadableId = sqGenerator.getNextId(Prefix.TL, digitId);
        medicRequest.setHumanReadableId(humanReadableId)
                    .setCreationTime(LocalDateTime.now(clock))
                    .setStatus(TelemedicineStatus.IN_PROGRESS)
                    .setOrganizationId(organizationId);
        return medicRequest;
    }
    
    private GetTelemedicineDto enrichGetTelemedicineDto(
            GetTelemedicineDto dto,
            Driver driver,
            DrivingLicense ewbDrivingLicense,
            OrganizationMedicalLicense organizationMedicalLicense,
            LocalDateTime medicDecisionTime
                                                       ) {
        if (dto.medic() != null && organizationMedicalLicense != null) {
            dto = dto.withMedic(dto.medic()
                                   .withSeries(organizationMedicalLicense.getSeries())
                                   .withNumber(organizationMedicalLicense.getNumber())
                                   .withIssueDate(organizationMedicalLicense.getIssueDate())
                                   .withExpiryDate(organizationMedicalLicense.getExpiryDate())
                                   .withDecisionTime(medicDecisionTime));
        }
        dto = dto.withDriver(
                dto.driver()
                   .withTin(driver.getTin())
                   .withDrivingLicenseId(ewbDrivingLicense.getId())
                   .withSeries(ewbDrivingLicense.getSeries())
                   .withNumber(ewbDrivingLicense.getNumber())
                   .withIssueDate(ewbDrivingLicense.getIssueDate()));
        return dto;
    }
    
    private void enrichMedicRequest(ExamInfo exam, MedicRequest medicRequest) {
        medicRequest.setStatus(exam.medicRequestStatus() ? TelemedicineStatus.DONE : TelemedicineStatus.DECLINED)
                    .setSystPressure(exam.systPressure())
                    .setDyastPressure(exam.dyastPressure())
                    .setPulse(exam.pulse())
                    .setComment(exam.comment())
                    .setTemperature(exam.temperature())
                    .setBloodAlcohol(exam.alcohol())
                    .setCreationTime(exam.creationDateTime());
    }
    
    private FileData getFile(String fileName) {
        try {
            return fileService.get(fileName);
        } catch (IOException e) {
            log.error("Ошибка получения файла {}", fileName, e);
            throw new TelemedicClientException("Ошибка создания заявки на медицинский осмотр! Обратитесь к диспетчеру.");
        }
    }
    
    private void checkApiKey(String apiKey) {
        if (telemedicineProperties.getToken() == null || !apiKey.equals(telemedicineProperties.getToken())) {
            throw new TokenInvalidException();
        }
    }
}
