package ru.sber.transport.telemechanic.service.impl;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ContentType;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.telemechanic.client.KorusClient;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.config.properties.KorusProperties;
import ru.sber.transport.telemechanic.database.dao.*;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.CheckResponse;
import ru.sber.transport.telemechanic.dto.CreateRequestDto;
import ru.sber.transport.telemechanic.dto.DeclinedTelemechRequest;
import ru.sber.transport.telemechanic.dto.SecondTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.*;
import ru.sber.transport.telemechanic.dto.ewb.fifth_title.FifthTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.fifth_title.FifthTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.first_title.FirstTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchAllOrganizationsRequestDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchResponseDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchSelfOrganizationRequestDto;
import ru.sber.transport.telemechanic.dto.ewb.second_title.SecondTitleForm;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.SendAndSaveTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechOutTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechOutTitleSendResponse;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechOutTitlesRequest;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistryAllOrganizationsRequest;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistryResponse;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistrySelfOrganizationRequest;
import ru.sber.transport.telemechanic.enumerate.*;
import ru.sber.transport.telemechanic.exception.*;
import ru.sber.transport.telemechanic.helper.CentralOrganizationHelper;
import ru.sber.transport.telemechanic.helper.CheckHelper;
import ru.sber.transport.telemechanic.helper.EwbHelper;
import ru.sber.transport.telemechanic.helper.EwbSearchSpecHelper;
import ru.sber.transport.telemechanic.human_readable_id.constant.Prefix;
import ru.sber.transport.telemechanic.mapper.*;
import ru.sber.transport.telemechanic.messaging.sender.EwbClosedSender;
import ru.sber.transport.telemechanic.messaging.sender.OdometerSender;
import ru.sber.transport.telemechanic.messaging.sender.message.OdometerHistoryValueMessage;
import ru.sber.transport.telemechanic.model.MedicalCheckUpModel;
import ru.sber.transport.telemechanic.model.Person;
import ru.sber.transport.telemechanic.service.*;

import java.io.ByteArrayInputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static ru.sber.transport.telemechanic.enumerate.ContactType.PHONE;
import static ru.sber.transport.telemechanic.enumerate.EwbStatus.*;
import static ru.sber.transport.telemechanic.exception.EwbNotFoundException.EWB_ON_THE_LINE_FOR_USER_ID_MSG_FORMAT;
import static ru.sber.transport.telemechanic.exception.EwbNotFoundException.EWB_REQUEST_MSG_FORMAT;
import static ru.sber.transport.telemechanic.service.EwbValidationService.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class EwbServiceImpl implements EwbService {
    
    private static final List<EwbStatus> HAVE_ACTIVE_EWB_CHECK_STATUSES =
            List.of(EWB_CREATED, MEDIC_IN_PROGRESS, TELEMECH_IN_PROGRESS, ON_THE_LINE, IN_GARAGE);
    private static final Set<EwbStatus> ACCESS_STATUSES = Set.of(EWB_CREATED, MEDIC_IN_PROGRESS, TELEMECH_IN_PROGRESS);
    private static final Set<EwbStatus> DECLINED_STATUSES = Set.of(MEDIC_DECLINED, TELEMECH_DECLINED, KORUS_DECLINED);
    
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final String BAD_DECISION_TIME_MESSAGE = "Дата принятия решения должна совпадать с датой начала рейса ЭПЛ!";
    
    private final Clock clock;
    private final EwbMapper ewbMapper;
    private final TelemedicineMapper telemedicineMapper;
    private final RequestMapper requestMapper;
    private final EwbFirstTitleMapper ewbFirstTitleMapper;
    private final SQGenerator sqGenerator;
    private final DepartmentService departmentService;
    private final EwbRepository ewbRepository;
    private final KorusClient korusClient;
    private final KorusProperties korusProperties;
    private final FileService fileService;
    private final EwbTitleRepository ewbTitleRepository;
    private final TransactionTemplate transactionTemplate;
    private final CentralOrganizationHelper centralOrganizationHelper;
    private final EwbValidationService ewbValidationService;
    
    private final TransportService transportService;
    private final EmployeeService employeeService;
    private final SignatureVerifier signatureVerifier;
    private final OdometerSender odometerSender;
    private final RequestService requestService;
    
    private final CheckService checkService;
    private final CheckPhotoService checkPhotoService;
    private final CheckHelper checkHelper;
    private final FifthTitleMapper fifthTitleMapper;
    
    private final EwbRegistryDynamicRepository ewbRegistryDynamicRepository;
    private final OrganizationMedicalLicenseService organizationMedicalLicenseService;
    private final EwbFilenameService ewbFilenameService;
    private final EwbTariffService ewbTariffService;
    private final FleetOwnerOrganizationService fleetOwnerOrganizationService;
    private final DispatcherService dispatcherService;
    private final DriverService driverService;
    private final OrganizationAddressService organizationAddressService;
    private final DrivingLicenseService drivingLicenseService;
    private final EwbClosedSender ewbClosedSender;
    
    private final RequestHistoryRepository requestHistoryRepository;
    private final MedicRequestHistoryRepository medicRequestHistoryRepository;
    private final EwbHistoryRepository ewbHistoryRepository;
    
    private final DepartmentTimeZoneService departmentTimeZoneService;
    
    @Override
    public TokenDto auth() {
        return korusClient.auth(new AuthRequest(korusProperties.login(), korusProperties.password()));
    }
    
    @Override
    public UuidDto getUUID() {
        var response = korusClient.createNewQr();
        if (HttpStatus.OK != response.getStatusCode()) {
            throw new QrUnavailableException();
        }
        return response.getBody();
    }
    
    @Override
    @Transactional
    public FirstTitleResponse generateFirstTitle(FirstTitleRequest request, UUID userId) {
        var dispatcher = dispatcherService.getByEmployeeIdAndActive(userId);
        fleetOwnerOrganizationService.validateFleetOwnerOrganization(dispatcher.getEmployee().getOrganization().getId());
        
        var dispatcherOrganization = dispatcherService.getSelfOrganizationInfo(userId);
        var driver = driverService.getActiveDriverById(request.driverId());
        var transport = transportService.getTransportInUseById(request.transportId());
        validateDates(request.startDate(), request.finishDate());
        var ewbContractDetails = ewbTariffService.validateEwbTariff(request.tariffDepartmentId());
        
        validateEwbDates(request.startDate(), request.finishDate(), ewbContractDetails);
        ewbValidationService.validateDriver(request.driverId(), request.startDate(), request.finishDate());
        
        var organizationDigitId = employeeService.getDigitIdByUserId(userId);
        var humanReadableId = sqGenerator.getNextId(Prefix.PL, organizationDigitId);
        var fileName = ewbFilenameService.generateFilename(EwbTitleType.FIRST,
                                                           dispatcherOrganization.organization().id(),
                                                           request.tariffDepartmentId());
        var creationTime = LocalDateTime.now(clock);
        var organizationAddress = organizationAddressService.get(dispatcher.getEmployee().getOrganization().getId());
        var file = ewbFirstTitleMapper.firstTitleRequestToFile(humanReadableId,
                fileName,
                request,
                dispatcherOrganization,
                driver,
                transport,
                organizationAddress,
                dispatcher,
                creationTime);
        var xml = EwbHelper.generateXml(file);
        var xmlAsByteArray = xml.toByteArray();
        return new FirstTitleResponse(humanReadableId, file.getIdFile(), xmlAsByteArray, creationTime);
    }
    
    @Override
    @SneakyThrows
    public void sendAndSaveFirstTitle(FirstTitleDto request, UUID userId) {
        var driver = driverService.getActiveDriverById(request.firstTitleForm().driverId());
        var transport = transportService.getTransportInUseById(request.firstTitleForm().transportId());
        ewbValidationService.validateEwb(request.firstTitleForm(), driver);
        var employeeFullName = extractUserFullName(userId);
        var response = sendTitle(employeeFullName, request.fileName(), request.content().getBytes(), request.signature());
        
        saveFileToExternalStorage(request.content().getBytes(), request.fileName());
        var ewb = createEwb(request, userId, driver, transport);
        saveTitleFileInfo(ewb.getId(), EwbTitleType.FIRST, request.fileName(), request.fileName(), response,
                          request.creationTime());
        saveSignature(request.signature(), ewb.getId(), EwbTitleType.FIRST);
    }
    
    @Override
    @Transactional
    public SecondTitleResponse generateSecondTitle(SecondTitleForm titleForm, UUID userId) {
        var ewb = ewbRepository.findById(titleForm.ewbId())
                               .orElseThrow(() -> new EwbNotFoundException(titleForm.ewbId()));
        var employee = employeeService.getByUserId(userId);
        var organizationMedicalLicense = organizationMedicalLicenseService.getMedicalLicense(ewb.getTariffDepartmentId());
        if (ewbTitleRepository.findByEwbIdAndType(ewb.getId(), EwbTitleType.SECOND).isPresent()) {
            throw new EwbTitleAlreadyExistsException(ewb.getId(), EwbTitleType.SECOND.getName());
        }
        
        var decisionTime = LocalDateTime.now(clock);
        var decisionTimeWithZoneOffset = decisionTime.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of(ewb.getTimeZone())).toLocalDate();
        if (!decisionTimeWithZoneOffset.equals(ewb.getStartDate())) {
            throw new BadRequestException(BAD_DECISION_TIME_MESSAGE);
        }
        
        var title = ewbTitleRepository.findByEwbIdAndType(ewb.getId(), EwbTitleType.FIRST)
                                      .orElseThrow(EwbGenerateException::new);
        var signature = getFile(title.getSignatureS3FileName());
        var drivingLicense = drivingLicenseService.get(ewb.getDriverLicenseId());
        var file = ewbMapper.secondTitleFormToFile(
                ewb.getEwbUuid(),
                decisionTime,
                organizationMedicalLicense,
                ewb.getDriver(),
                drivingLicense,
                title,
                signature,
                employee);
        file.setIdFile(ewbFilenameService.generateFilename(EwbTitleType.SECOND, ewb.getOrganization().getId(), ewb.getTariffDepartmentId()));
        
        var xml = EwbHelper.generateXml(file);
        var xmlAsByteArray = xml.toByteArray();
        
        saveTelemedicine(ewb, titleForm, employee, organizationMedicalLicense.getId(), decisionTime);
        return new SecondTitleResponse(file.getIdFile(), xmlAsByteArray,
                                       new SecondTitleResponse.MedicInfo(
                                               employee.getFirstName(),
                                               employee.getLastName(),
                                               employee.getPatronymic(),
                                               employee.getPosition().getPositionName(),
                                               employee.getOrganization().getOfficialName()
                                       ),
                                       new SecondTitleResponse.MedicalLicenseInfo(
                                               organizationMedicalLicense.getSeries(),
                                               organizationMedicalLicense.getNumber(),
                                               organizationMedicalLicense.getIssueDate(),
                                               organizationMedicalLicense.getExpiryDate()
                                       ));
    }
    
    @Override
    @Transactional
    public void sendAndSaveSecondTitle(SendAndSaveTitleRequest request, UUID userId) {
        var employeeFullName = extractUserFullName(userId);
        var response = sendTitle(employeeFullName, request.fileName(), request.file().getBytes(), request.signature());
        var ewb = ewbRepository.findById(request.ewbId())
                               .orElseThrow(() -> new EntityNotFoundException(Ewb.class, request.ewbId()));
        ewbValidationService.validateDriverForSecondTitle(ewb.getDriver(), ewb.getOrganization());
        saveFileToExternalStorage(request.file().getBytes(), request.fileName());
        saveTitleFileInfo(request.ewbId(), EwbTitleType.SECOND, request.fileName(), request.fileName(), response, ewb.getMedicDecisionTime());
        saveSignature(request.signature(), request.ewbId(), EwbTitleType.SECOND);
        var requestAuthor = employeeService.getByUserId(ewb.getDriver().getEmployee().getId());
        var createdRequest = requestService.createEmpty(new CreateRequestDto(ewb.getTransport().getId()), requestAuthor, true);
        ewb.setRequest(createdRequest)
           .setStatus(TELEMECH_IN_PROGRESS);
        ewb.getMedicRequest()
           .setStatus(TelemedicineStatus.DONE);
    }
    
    @Override
    @Transactional
    public TelemechOutTitleResponse generateTelemechOutTitles(EwbTitleType titleType, TelemechOutTitlesRequest request, UUID userId) {
        var ewb = ewbRepository.findByRequestId(request.requestId())
                               .orElseThrow(() -> new EwbNotFoundException(request.requestId(), EWB_REQUEST_MSG_FORMAT));
        var employee = employeeService.getByUserId(userId);
        return switch (titleType) {
            case THIRD -> generateThirdTitle(ewb, employee, request);
            case FOURTH -> generateFourthTitle(ewb, employee, request);
            default -> throw new BadRequestException("%s не может быть сформирован!".formatted(titleType.getName()));
        };
    }
    
    private TelemechOutTitleResponse generateThirdTitle(Ewb ewb, Employee employee, TelemechOutTitlesRequest request) {
        if (ewbTitleRepository.findByEwbIdAndType(ewb.getId(), EwbTitleType.THIRD).isPresent()) {
            throw new EwbTitleAlreadyExistsException(ewb.getId(), EwbTitleType.THIRD.getName());
        }
        var decisionTimeWithZoneOffset = request.decisionTime().atZone(ZoneOffset.UTC)
                                                .withZoneSameInstant(ZoneId.of(ewb.getTimeZone())).toLocalDate();
        if (!decisionTimeWithZoneOffset.equals(ewb.getStartDate())) {
            throw new BadRequestException(BAD_DECISION_TIME_MESSAGE);
        }
        var firstTitle = ewbTitleRepository.findByEwbIdAndType(ewb.getId(), EwbTitleType.FIRST)
                                           .orElseThrow(EwbGenerateException::new);
        var signature = getFile(firstTitle.getSignatureS3FileName());
        ewb.setTelemechOut(employee);
        ewb.setTelemechDecisionOut(request.decisionTime());
        
        var fileCreationTime = LocalDateTime.now(clock);
        var ewbInfo = new EwbInfo(ewb, firstTitle, signature);
        
        var file = ewbMapper.ewbInfoToFile(ewbInfo, fileCreationTime);
        file.setIdFile(ewbFilenameService.generateFilename(EwbTitleType.THIRD, ewb.getOrganization().getId(), ewb.getTariffDepartmentId()));
        var xml = EwbHelper.generateXml(file);
        
        return new TelemechOutTitleResponse(file.getIdFile(), xml.toByteArray(), fileCreationTime);
    }
    
    private TelemechOutTitleResponse generateFourthTitle(Ewb ewb, Employee employee, TelemechOutTitlesRequest request) {
        if (ewbTitleRepository.findByEwbIdAndType(ewb.getId(), EwbTitleType.FOURTH).isPresent()) {
            throw new EwbTitleAlreadyExistsException(ewb.getId(), EwbTitleType.FOURTH.getName());
        }
        var decisionTimeWithZoneOffset = request.decisionTime().atZone(ZoneOffset.UTC)
                                                .withZoneSameInstant(ZoneId.of(ewb.getTimeZone())).toLocalDate();
        if (!decisionTimeWithZoneOffset.equals(ewb.getStartDate())) {
            throw new BadRequestException(BAD_DECISION_TIME_MESSAGE);
        }
        if (!employee.getId().equals(ewb.getTelemechOut().getId())) {
            throw new BadRequestException("ЭПЛ не может быть сформирован! Сотрудник ответственный за выпуск на линию не совподает!");
        }
        var thirdTitle = ewbTitleRepository.findByEwbIdAndType(ewb.getId(), EwbTitleType.THIRD)
                                           .orElseThrow(EwbGenerateException::new);
        var signature = getFile(thirdTitle.getSignatureS3FileName());
        ewb.setTelemechDecisionOut(request.decisionTime());
        var ewbInfo = new EwbInfo(ewb, thirdTitle, signature);
        
        var file = ewbMapper.ewbInfoToFourthTitleFile(ewbInfo);
        file.setIdFile(ewbFilenameService.generateFilename(EwbTitleType.FOURTH, ewb.getOrganization().getId(), ewb.getTariffDepartmentId()));
        var xml = EwbHelper.generateXml(file);
        
        return new TelemechOutTitleResponse(file.getIdFile(), xml.toByteArray(), LocalDateTime.now(clock));
    }
    
    @Override
    @Transactional
    public FifthTitleResponse generateFifthTitle(FifthTitleRequest request, UUID userId) {
        var ewb = ewbRepository.findById(request.id())
                               .orElseThrow(() -> new EwbNotFoundException(request.id()));
        var employee = employeeService.getByUserId(userId);
        if (ewbTitleRepository.findByEwbIdAndType(ewb.getId(), EwbTitleType.FIFTH).isPresent()) {
            throw new EwbTitleAlreadyExistsException(ewb.getId(), EwbTitleType.FIFTH.getName());
        }
        if (!ewb.getStatus().equals(IN_GARAGE)) {
            throw new BadRequestException("Невозможно сформировать пятый титул для ЭПЛ в статусе %s!".formatted(ewb.getStatus()));
        }
        
        var fourthTitle = ewbTitleRepository.findByEwbIdAndType(ewb.getId(), EwbTitleType.FOURTH)
                                            .orElseThrow(EwbGenerateException::new);
        var signature = getFile(fourthTitle.getSignatureS3FileName());
        ewb.setTelemechIn(employee);
        ewb.setTelemechDecisionIn(request.decisionTime());
        var ewbInfo = new EwbInfo(ewb, fourthTitle, signature);
        var file = fifthTitleMapper.ewbInfoToFifthTitleFile(ewbInfo);
        file.setIdFile(ewbFilenameService.generateFilename(EwbTitleType.FIFTH, ewb.getOrganization().getId(), ewb.getTariffDepartmentId()));
        var xml = EwbHelper.generateXml(file);
        
        return new FifthTitleResponse(file.getIdFile(), xml.toByteArray(), LocalDateTime.now(clock));
    }
    
    private @NotNull Ewb createEwb(FirstTitleDto firstTitleDto, UUID userId, Driver driver, Transport transport) {
        var savedEwb = transactionTemplate.execute(status -> {
            var dispatcher = dispatcherService.getByEmployeeIdAndActive(userId);
            var department = departmentService.get(firstTitleDto.firstTitleForm().tariffDepartmentId())
                                              .orElseThrow(
                                                      () -> new EntityNotFoundException(Department.class,
                                                                                        firstTitleDto.firstTitleForm().tariffDepartmentId()));
            var timeZone = departmentTimeZoneService.getTimeZoneByDepartmentId(department.getId());
            
            return ewbRepository.save(new Ewb()
                                              .setStatus(EWB_CREATED)
                                              .setEwbUuid(firstTitleDto.firstTitleForm().ewbUuid())
                                              .setAuthor(dispatcher.getEmployee())
                                              .setCreationTime(firstTitleDto.creationTime())
                                              .setHumanReadableId(firstTitleDto.humanReadableId())
                                              .setStartDate(firstTitleDto.firstTitleForm().startDate())
                                              .setFinishDate(firstTitleDto.firstTitleForm().finishDate())
                                              .setOrganization(dispatcher.getEmployee().getOrganization())
                                              .setTransport(transport)
                                              .setDriver(driver)
                                              .setDriverLicenseId(driver.getDrivingLicense().getId())
                                              .setTariffDepartmentId(department.getId())
                                              .setAttorneyOutId(dispatcher.getAttorney())
                                              .setTimeZone(timeZone));
        });
        if (Objects.isNull(savedEwb)) {
            throw new EwbNotSavedException("Ewb with uuid %s wasn't saved".formatted(firstTitleDto.firstTitleForm().ewbUuid()));
        }
        return savedEwb;
    }
    
    private void saveTitleFileInfo(
            UUID ewbId,
            EwbTitleType titleType,
            String s3FileName,
            String fileName,
            KorusEwbTitleResponse response,
            LocalDateTime creationTime
                                  ) {
        transactionTemplate.executeWithoutResult(txState -> {
            var ewb = ewbRepository.findById(ewbId)
                                   .orElseThrow(() -> new EntityNotFoundException(Ewb.class, ewbId));
            var titleFile = ewbTitleRepository.findByEwbIdAndType(ewbId, titleType)
                                              .orElseGet(() -> EwbTitle.builder()
                                                                       .type(titleType)
                                                                       .fileName(fileName)
                                                                       .ewb(ewb)
                                                                       .build());
            titleFile.setS3FileName(s3FileName);
            titleFile.setChainId(response.chainId());
            titleFile.setKorusId(response.id());
            titleFile.setCreatedAt(creationTime);
            titleFile.setSentAt(LocalDateTime.now(clock));
            ewbTitleRepository.save(titleFile);
        });
    }
    
    @Override
    public Page<EwbSearchResponseDto> searchSelfOrganization(EwbSearchSelfOrganizationRequestDto request, UUID userId) {
        var userOrganizationId = employeeService.getByUserId(userId).getOrganization().getId();
        var specification = EwbSearchSpecHelper.getSpecification(request.searchText(),
                                                                 request.stateNumber(),
                                                                 userOrganizationId,
                                                                 request.requestStatusSet(),
                                                                 request.creationTime(),
                                                                 request.finishTime(),
                                                                 null,
                                                                 null);
        return search(request.preparePageRequest(), specification);
    }
    
    @Override
    public Page<EwbSearchResponseDto> searchAllOrganizations(EwbSearchAllOrganizationsRequestDto request) {
        var specification = EwbSearchSpecHelper.getSpecification(request.searchText(),
                                                                 request.stateNumber(),
                                                                 request.organizationId(),
                                                                 request.requestStatusSet(),
                                                                 request.creationTime(),
                                                                 request.finishTime(),
                                                                 null,
                                                                 null);
        return search(request.preparePageRequest(), specification);
    }
    
    @Override
    public Page<EwbSearchResponseDto> search(EwbSearchDto request) {
        var specification = EwbSearchSpecHelper.getSpecification(request.searchText(),
                                                                 request.stateNumber(),
                                                                 null,
                                                                 request.requestStatusSet(),
                                                                 request.creationTime(),
                                                                 request.finishTime(),
                                                                 request.contractorIds(),
                                                                 departmentService.getDepartmentIdsByAutoparkIds(request.autoparkIds()));
        return search(request.preparePageRequest(), specification);
    }
    
    @Override
    @Transactional
    public GetEwbDto getEwb(UUID id) {
        var ewb = ewbRepository.findById(id)
                               .orElseThrow(() -> new EntityNotFoundException(Ewb.class, id));
        var result = ewbMapper.ewbToGetEwbDto(ewb);
        enrichGetEwbResponse(result, ewb);
        
        return result;
    }
    
    @Override
    @Transactional
    public Ewb getEwbByRequestId(UUID requestId) {
        return ewbRepository.findByRequestId(requestId)
                            .orElseThrow(() -> new EwbNotFoundException(requestId, EWB_REQUEST_MSG_FORMAT));
    }
    
    @Override
    @Transactional
    public GetEwbRequestDto getEwbRequest(UUID userId) {
        var employee = employeeService.getByUserId(userId);
        var departmentId = employee.getDepartment().getId();
        var timeZone = departmentTimeZoneService.getTimeZoneByDepartmentId(departmentId);
        var localDateInTimeZone = LocalDateTime.now(clock).atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of(timeZone)).toLocalDate();
        var ewbs = ewbRepository.findByDriverIdAndStatusIn(userId, localDateInTimeZone, ACCESS_STATUSES, DECLINED_STATUSES);
        if (ewbs.isEmpty()) {
            return null;
        }
        var lastEwb = ewbs.stream().max(Comparator.comparing(Ewb::getCreationTime)).orElseThrow();
        var mappedEwbRequest = ewbMapper.ewbToGetEwbRequestDto(lastEwb);
        if (Objects.isNull(mappedEwbRequest.telemechanic())) {
            return mappedEwbRequest;
        }
        var checksTree = checkHelper.createChecksTree(lastEwb.getRequest().getChecks());
        return mappedEwbRequest
                .withTelemechanic(mappedEwbRequest.telemechanic().withChecks(checksTree))
                .withCallTelemech(CheckHelper.isCallTelemech(lastEwb.getRequest().getChecks()));
    }
    
    private Page<EwbSearchResponseDto> search(PageRequest pageRequest, Specification<Ewb> specification) {
        var ewbs = ewbRepository.findAll(specification, pageRequest);
        return new PageImpl<>(ewbs.stream()
                                  .map(ewb -> centralOrganizationHelper.map(
                                          ewb.getOrganization().getOrganizationGroupId(),
                                          () -> ewbMapper.ewbToEwbSearchResponseDtoWithOrganizationName(ewb, ewb.getOrganization().getOfficialName()),
                                          () -> ewbMapper.ewbToEwbSearchResponseDtoWithOrganizationName(ewb,
                                                                                                        centralOrganizationHelper.getCentralName())))
                                  .toList(),
                              ewbs.getPageable(), ewbs.getTotalElements());
    }
    
    private void enrichGetEwbResponse(GetEwbDto ewbDto, Ewb ewb) {
        ewbDto.setPhoneNumber(findAnyOrganizationPhoneFromEwb(ewb));
        if (ewb.getMedic() != null) {
            ewbDto.setMedic(new GetEwbDto.Medic(
                    ewb.getMedic().getFirstName(),
                    ewb.getMedic().getLastName(),
                    ewb.getMedic().getPatronymic(),
                    ewb.getMedic().getPosition().getPositionName(),
                    ewb.getStatus().isMedicSuccess(),
                    ewb.getMedicDecisionTime().atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of(ewb.getTimeZone())).toLocalDateTime()
            ));
        } else if (ewb.getMedicContractor() != null) {
            var person = new Person(ewb.getMedicContractor().getFullName());
            ewbDto.setMedic(new GetEwbDto.Medic(
                    person.getFirstName(),
                    person.getLastName(),
                    person.getPatronymic(),
                    ewb.getMedicContractor().getPosition(),
                    ewb.getStatus().isMedicSuccess(),
                    ewb.getMedicDecisionTime().atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of(ewb.getTimeZone())).toLocalDateTime()
            ));
        }
        if (ewb.getTelemechOut() != null) {
            ewbDto.getTelemechOut().setFirstName(ewb.getTelemechOut().getFirstName());
            ewbDto.getTelemechOut().setLastName(ewb.getTelemechOut().getLastName());
            ewbDto.getTelemechOut().setPatronymic(ewb.getTelemechOut().getPatronymic());
            ewbDto.getTelemechOut().setPosition(ewb.getTelemechOut().getPosition().getPositionName());
            ewbDto.getTelemechOut().setTelemechSuccess(ewb.getStatus().isTelemechSuccess());
            ewbDto.getTelemechOut().setDecisionTime(
                    ewb.getTelemechDecisionOut().atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of(ewb.getTimeZone())).toLocalDateTime()
                                                   );
            ewbDto.getTelemechOut().setMileage(ewb.getOdometerOut());
        }
        if (ewb.getTelemechIn() != null) {
            ewbDto.getTelemechIn().setFirstName(ewb.getTelemechIn().getFirstName());
            ewbDto.getTelemechIn().setLastName(ewb.getTelemechIn().getLastName());
            ewbDto.getTelemechIn().setPatronymic(ewb.getTelemechIn().getPatronymic());
            ewbDto.getTelemechIn().setPosition(ewb.getTelemechIn().getPosition().getPositionName());
            ewbDto.getTelemechIn().setTelemechSuccess(ewb.getStatus().equals(IN_GARAGE) || ewb.getStatus().equals(EWB_CLOSED));
            ewbDto.getTelemechIn().setDecisionTime(
                    ewb.getTelemechDecisionIn().atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of(ewb.getTimeZone())).toLocalDateTime()
                                                  );
            ewbDto.getTelemechIn().setMileage(ewb.getOdometerIn());
        }
        if (centralOrganizationHelper.isCentral(ewb.getOrganization().getOrganizationGroupId())) {
            ewbDto.setMsrn(centralOrganizationHelper.getCentralMsrn());
            ewbDto.setTin(centralOrganizationHelper.getCentralTin());
            ewbDto.setPhoneNumber(centralOrganizationHelper.getCentralPhone());
            ewbDto.setOrganizationName(centralOrganizationHelper.getCentralName());
        }
    }
    
    @Nullable
    private static String findAnyOrganizationPhoneFromEwb(Ewb ewb) {
        return ewb.getOrganization().getContacts().stream()
                  .filter(contact -> contact.getType().equals(PHONE))
                  .findFirst().map(Contact::getValue)
                  .orElse(null);
    }
    
    @Override
    @Transactional
    public TelemechOutTitleSendResponse sendAndSaveTelemechOutTitle(
            SendAndSaveTitleRequest request,
            EwbTitleType titleType,
            UUID userId
                                                                   ) {
        if (ewbTitleRepository.findByEwbIdAndType(request.ewbId(), titleType).isPresent()) {
            throw new EwbTitleAlreadyExistsException(request.ewbId(), titleType.getName());
        }
        var employeeFullName = extractUserFullName(userId);
        var response = sendTitle(employeeFullName, request.fileName(), request.file().getBytes(), request.signature());
        saveFileToExternalStorage(request.file().getBytes(), request.fileName());
        saveTitleFileInfo(request.ewbId(), titleType, request.fileName(), request.fileName(), response, request.creationTime());
        saveSignature(request.signature(), request.ewbId(), titleType);
        var ewb = ewbRepository.findById(request.ewbId())
                               .orElseThrow(() -> new EwbNotFoundException(request.ewbId()));
        if (titleType.equals(EwbTitleType.THIRD)) {
            requestService.updateRequestThirdTitleSent(ewb.getRequest(), userId);
        }
        if (titleType.equals(EwbTitleType.FOURTH)) {
            ewbAndRequestOnTheLine(ewb);
        }
        return new TelemechOutTitleSendResponse(EwbTitleType.getNextTitleType(titleType));
    }
    
    @Override
    @Transactional
    public void sendAndSaveFifthTitle(SendAndSaveTitleRequest request, UUID userId) {
        if (ewbTitleRepository.findByEwbIdAndType(request.ewbId(), EwbTitleType.FIFTH).isPresent()) {
            throw new EwbTitleAlreadyExistsException(request.ewbId(), EwbTitleType.FIFTH.getName());
        }
        var employeeFullName = extractUserFullName(userId);
        var response = sendTitle(employeeFullName, request.fileName(), request.file().getBytes(), request.signature());
        var ewb = ewbRepository.findById(request.ewbId())
                               .orElseThrow(() -> new EwbNotFoundException(request.ewbId()));
        saveFileToExternalStorage(request.file().getBytes(), request.fileName());
        saveTitleFileInfo(request.ewbId(), EwbTitleType.FIFTH, request.fileName(), request.fileName(), response, ewb.getTelemechDecisionIn());
        saveSignature(request.signature(), request.ewbId(), EwbTitleType.FIFTH);
        
        ewb.setStatus(EWB_CLOSED)
           .getRequest()
           .setStatus(RequestStatus.FINISHED);
        ewbRepository.saveAndFlush(ewb);
        ewbClosedSender.send(ewbMapper.ewbToEwbClosedMessage(ewb));
    }
    
    private KorusEwbTitleResponse sendTitle(String employeeFullName, String fileName, byte[] base64Content, String signature) {
        var content = Base64.getDecoder().decode(base64Content);
        signatureVerifier.verify(content, signature, employeeFullName);
        var archiveForSending = EwbHelper.zipFiles(content, signature.getBytes(), fileName);
        var response = korusClient.sendTitle(KorusTitleRequest.builder()
                                                              .name("%s.zip".formatted(fileName))
                                                              .content(archiveForSending)
                                                              .build());
        log.info("Response from Korus {}", response);
        return response;
    }
    
    @Override
    @Transactional
    public void closeEwb(EwbCloseRequest request, UUID userId) {
        var ewb = getEwbById(request.id());
        validateOdometerInValue(request.value(), ewb);
        validateLitreageInValue(request.fuelLitreage(), ewb);
        ewb.setOdometerIn(request.value())
           .setStatus(IN_GARAGE)
           .setFuelLitreageIn(request.fuelLitreage())
           .getTransport().setFuelLitreage(request.fuelLitreage());
        ewb.getRequest().setStatus(RequestStatus.IN_GARAGE);
        publishOdometerValueChanged(request.value(), userId, ewb, "IN");
        ewbClosedSender.send(ewbMapper.ewbToEwbClosedMessage(ewb));
    }
    
    @Override
    @Transactional
    public CheckResponse addOdometerValue(OdometerValue request, UUID userId) {
        var ewb = getEwbById(request.id());
        checkPhotoService.validateCheck(ewb.getRequest().getId(), CheckType.ODOMETER, userId);
        validateOdometerOutValue(request, ewb);
        ewb.setOdometerOut(request.value());
        publishOdometerValueChanged(request.value(), userId, ewb, "OUT");
        var check = checkService.changeCheckStatus(ewb.getRequest().getId(), CheckType.ODOMETER, CheckStatus.DONE);
        return checkService.getNextCheck(ewb.getRequest().getId(), check.getCheckStatus(), true);
    }
    
    @Override
    @Transactional
    public void declineEwb(UUID requestId, DeclinedTelemechRequest request, UUID userId) {
        var ewb = ewbRepository.findEwbByRequestId(requestId)
                               .orElseThrow(() -> new EwbNotFoundException(requestId, EWB_REQUEST_MSG_FORMAT));
        validateRequestStatus(ewb.getRequest().getStatus());
        var employee = employeeService.getByUserId(userId);
        ewb.setStatus(TELEMECH_DECLINED)
           .setTelemechOut(employee)
           .setTelemechDecisionOut(LocalDateTime.now(clock));
        ewb.getRequest().setStatus(RequestStatus.DECLINED)
           .setInspector(employee)
           .setInspectionTime(LocalDateTime.now(clock))
           .setComment(request.comment());
        
        request.checks().forEach(checkDto -> ewb.getRequest().getChecks().stream()
                                                .filter(check -> check.getId().equals(checkDto.id()))
                                                .findAny()
                                                .ifPresent(check -> check.setComment(checkDto.comment())));
    }
    
    @Override
    @Transactional
    public GetQrCodeResponse getQrCode(UUID ewbId, UUID userId) {
        ewbValidationService.validateEwbQrCode(ewbId, userId);
        var ewbFourthTitle = ewbTitleRepository.findByEwbIdAndType(ewbId, EwbTitleType.FOURTH)
                                               .orElseThrow(() -> new EwbTitleNotFoundException(ewbId, EwbTitleType.FOURTH));
        var chainDocs = korusClient.getChainDocs(ewbFourthTitle.getChainId());
        
        if (Objects.isNull(chainDocs) || chainDocs.documents().isEmpty()) {
            throw new QrCodeNotExistsException();
        }
        
        var qrCodeUuid = chainDocs.documents().stream()
                                  .filter(doc -> doc.type().equals("ON_PTLAPERAK_QRCODE")
                                                 && doc.chainId().equals(ewbFourthTitle.getChainId()))
                                  .map(ChainDocsDto.DocsDto::id)
                                  .findAny()
                                  .orElseThrow(QrCodeNotExistsException::new);
        
        var archive = korusClient.getDocArchive(qrCodeUuid);
        var extractedXml = EwbHelper.extractDocumentFromArchive(archive);
        return new GetQrCodeResponse(EwbHelper.unmarshallQrCode(extractedXml));
    }
    
    
    @Override
    @Transactional
    public Page<EwbRegistryResponse> searchRegistryForAllOrganizations(EwbRegistryAllOrganizationsRequest request) {
        var pageRequest = request.preparePageRequest();
        var ewbs = ewbRegistryDynamicRepository.findEwbRegistry(pageRequest,
                                                                request.fieldSet(),
                                                                request.searchText(),
                                                                request.humanReadableId(),
                                                                request.organizationId(),
                                                                request.departmentIds(),
                                                                request.period()
                                                               );
        return new PageImpl<>(ewbs.ewbRegistrySearchResult(),
                              pageRequest,
                              ewbs.totalElements());
    }
    
    @Override
    @Transactional
    public Page<EwbRegistryResponse> searchRegistryForSelfOrganization(EwbRegistrySelfOrganizationRequest request, UUID userId) {
        var user = employeeService.getByUserId(userId);
        var pageRequest = request.preparePageRequest();
        var ewbs = ewbRegistryDynamicRepository.findEwbRegistry(pageRequest,
                                                                request.fieldSet(),
                                                                request.searchText(),
                                                                request.humanReadableId(),
                                                                user.getOrganization().getId(),
                                                                null,
                                                                request.period()
                                                               );
        return new PageImpl<>(ewbs.ewbRegistrySearchResult(),
                              pageRequest,
                              ewbs.totalElements());
    }
    
    @Override
    public boolean haveActiveEwb(List<UUID> departmentIds, LocalDate checkStartDate) {
        return ewbRepository.ewbExistsByTariffDepartmentsAndStatuses(departmentIds, HAVE_ACTIVE_EWB_CHECK_STATUSES, checkStartDate);
    }
    
    @Override
    public GetEwbDetailedDto getEwbDetailed(UUID userId) {
        var driver = driverService.getByEmployeeId(userId);
        var timeZone = departmentTimeZoneService.getTimeZoneByDepartmentId(driver.getEmployee().getDepartment().getId());
        var localDateInTimeZone = LocalDateTime.now(clock).atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of(timeZone)).toLocalDate();
        var ewb = ewbRepository.findCurrentOnTheLineByDriverId(driver.getId(), localDateInTimeZone)
                               .orElseThrow(() -> new EwbNotFoundException(userId, EWB_ON_THE_LINE_FOR_USER_ID_MSG_FORMAT));
        return centralOrganizationHelper.map(ewb.getOrganization().getOrganizationGroupId(),
                                             () -> ewbMapper.ewbToGetEwbDetailedDto(ewb, driver,
                                                                                    ewb.getOrganization().getOfficialName(),
                                                                                    ewb.getOrganization().getTin(),
                                                                                    ewb.getOrganization().getMsrn(),
                                                                                    findAnyOrganizationPhoneFromEwb(ewb)),
                                             () -> ewbMapper.ewbToGetEwbDetailedDto(ewb, driver,
                                                                                    centralOrganizationHelper.getCentralName(),
                                                                                    centralOrganizationHelper.getCentralTin(),
                                                                                    centralOrganizationHelper.getCentralMsrn(),
                                                                                    centralOrganizationHelper.getCentralPhone()));
    }
    
    @Override
    @Transactional
    public void cancelEwb(UUID id, EwbCancelRequestDto dto, UUID userId) {
        var ewb = ewbRepository.findById(id).orElseThrow(() -> new EwbNotFoundException(id));
        var initiator = employeeService.getByUserId(userId);
        validateEwbStatusForCancelling(ewb.getStatus());
        var ewbHistory = ewbMapper.ewbToEwbHistory(ewb, EWB_CANCELLED, dto.comment(), initiator);
        RequestHistory requestHistory = null;
        MedicRequestHistory medicRequestHistory = null;
        if (ewb.getMedicRequest() != null) {
            medicRequestHistory = telemedicineMapper.medicRequestToMedicRequestHistory(ewb.getMedicRequest(), TelemedicineStatus.EXPIRED,
                                                                                       dto.comment(), initiator);
            ewb.getMedicRequest().setStatus(TelemedicineStatus.EXPIRED);
        }
        if (ewb.getRequest() != null) {
            requestHistory = requestMapper.requestToRequestHistory(ewb.getRequest(), RequestStatus.EXPIRED, dto.comment(), initiator);
            ewb.getRequest().setStatus(RequestStatus.EXPIRED);
        }
        
        ewb.setStatus(EWB_CANCELLED);
        ewbRepository.save(ewb);
        ewbClosedSender.send(ewbMapper.ewbToEwbClosedMessage(ewb));
        
        saveHistory(ewbHistory, requestHistory, medicRequestHistory);
    }
    
    @Override
    @Transactional
    public void statusAutoUpdate() {
        var ewbs = ewbRepository.findAllByStatusInAndFinishDate(ACCESS_STATUSES, LocalDate.now(clock).minusDays(1));
        if (!ewbs.isEmpty()) {
            var ewbHistory = ewbs.stream()
                                 .map(ewb -> ewbMapper.ewbToEwbHistory(ewb, EXPIRED,
                                                                       "Статус заявки изменен пользователем: Система (Планировщик)",
                                                                       null))
                                 .toList();
            ewbRepository.setEwbListExpired(ewbs.stream().map(Ewb::getId).toList());
            ewbHistoryRepository.saveAll(ewbHistory);
        }
    }
    
    @Override
    @Transactional
    public CheckResponse litreageOut(EwbLitreageOutRequest request, UUID userId) {
        var ewb = getEwbById(request.id());
        var litreage = request.value();
        checkPhotoService.validateCheck(ewb.getRequest().getId(), CheckType.LITREAGE, userId);
        validateLitreageOutValue(request.value(), ewb, userId);
        ewb.setFuelLitreageOut(litreage);
        ewb.getTransport().setFuelLitreage(litreage);
        var check = checkService.changeCheckStatus(ewb.getRequest().getId(), CheckType.LITREAGE, CheckStatus.DONE);
        return checkService.getNextCheck(ewb.getRequest().getId(), check.getCheckStatus(), true);
    }
    
    @Override
    @Transactional
    public MedicalCheckUpModel getStraightSecondTitle(UUID titleUUID) {
        var title = ewbTitleRepository.findById(titleUUID)
                          .orElseThrow(() -> new EwbTitleNotFoundException(titleUUID));
        var builder = MedicalCheckUpModel.builder()
                           .name(title.getFileName())
                           .ewbUuid(title.getEwb().getId())
                           .medicInfo(new MedicalCheckUpModel.MedicInfo(
                                   title.getEwb().getMedicContractor().getFullName(),
                                   title.getEwb().getMedicContractor().getPersonnelNumber(),
                                   title.getEwb().getMedicContractor().getOrganization(),
                                   title.getEwb().getMedicContractor().getDepartment(),
                                   title.getEwb().getMedicContractor().getPosition(),
                                   title.getEwb().getMedicContractor().getSignKeyNumber(),
                                   title.getEwb().getMedicContractor().getSignKeyEndDateTime()
                           ));
        
        if (title.getEwb().getMedicRequest() != null) {
            var signature = getFile(title.getSignatureS3FileName());
            var file = getFile(title.getS3FileName());
            builder.withExamInfo(true);
            builder.signature(signature);
            builder.data(file);
            builder.exam(new MedicalCheckUpModel.ExamInfo(
                    TelemedicineStatus.DONE.equals(title.getEwb().getMedicRequest().getStatus()),
                    title.getEwb().getCreationTime(),
                    title.getEwb().getMedicDecisionTime(),
                    title.getEwb().getMedicRequest().getSystPressure(),
                    title.getEwb().getMedicRequest().getDyastPressure(),
                    title.getEwb().getMedicRequest().getPulse(),
                    title.getEwb().getMedicRequest().getTemperature(),
                    title.getEwb().getMedicRequest().getBloodAlcohol(),
                    title.getEwb().getMedicRequest().getComment()
            ));
        }
        return builder.build();
    }
    
    private void saveHistory(EwbHistory ewbHistory, RequestHistory requestHistory, MedicRequestHistory medicRequestHistory) {
        ewbHistoryRepository.save(ewbHistory);
        if (medicRequestHistory != null) {
            medicRequestHistoryRepository.save(medicRequestHistory);
        }
        if (requestHistory != null) {
            requestHistoryRepository.save(requestHistory);
        }
    }
    
    private Ewb getEwbById(UUID ewbId) {
        return ewbRepository.findById(ewbId)
                            .orElseThrow(() -> new EwbNotFoundException(ewbId));
    }
    
    private void publishOdometerValueChanged(int value, UUID userId, Ewb ewb, String direction) {
        odometerSender.send(new OdometerHistoryValueMessage(ewb.getTransport().getId(),
                                                            value,
                                                            userId,
                                                            LocalDateTime.now(clock),
                                                            Map.of("EWB_ID", ewb.getId(), "DIRECTION", direction)));
    }
    
    @NotNull
    private String extractUserFullName(UUID userId) {
        var employee = employeeService.getByUserId(userId);
        return employee.getLastName() +
               employee.getFirstName() +
               Optional.ofNullable(employee.getPatronymic()).orElse("");
    }
    
    @NotNull
    private void saveSignature(String signature, UUID ewbId, EwbTitleType type) {
        var signatureS3Filename = UUID.randomUUID().toString();
        saveFileToExternalStorage(signature.getBytes(), signatureS3Filename);
        saveEwbTitleSignatureFileName(ewbId, signatureS3Filename, type);
    }
    
    private void saveEwbTitleSignatureFileName(UUID ewbId, String signatureS3Filename, EwbTitleType type) {
        transactionTemplate.executeWithoutResult(status ->
                                                         ewbTitleRepository.findByEwbIdAndType(ewbId, type)
                                                                           .ifPresent(title -> title.setSignatureS3FileName(signatureS3Filename)));
    }
    
    @SneakyThrows
    private void saveFileToExternalStorage(byte[] file, String fileName) {
        try (var inputStream = new ByteArrayInputStream(file)) {
            fileService.upload(inputStream, fileName, ContentType.APPLICATION_OCTET_STREAM.getMimeType());
        }
    }
    
    @SneakyThrows
    private String getFile(String fileNameS3) {
        return Arrays.toString(fileService.get(fileNameS3).stream());
    }
    
    private void saveTelemedicine(
            Ewb ewb,
            SecondTitleForm secondTitleForm,
            Employee employee,
            UUID organizationMedicalLicenseId,
            LocalDateTime decisionTime
                                 ) {
        transactionTemplate.executeWithoutResult(status -> {
            ewb.getMedicRequest()
               .setSystPressure(secondTitleForm.request().systPressure())
               .setDyastPressure(secondTitleForm.request().dyastPressure())
               .setPulse(secondTitleForm.request().pulse())
               .setTemperature(secondTitleForm.request().temperature())
               .setBloodAlcohol(secondTitleForm.request().bloodAlcohol())
               .setComment(secondTitleForm.request().comment());
            ewb.setMedic(employee)
               .setOrganizationMedicalLicenseId(organizationMedicalLicenseId)
               .setMedicDecisionTime(decisionTime);
        });
    }
    
    private void ewbAndRequestOnTheLine(Ewb ewb) {
        ewb.setStatus(ON_THE_LINE)
           .setQrCode(true)
           .getRequest()
           .setStatus(RequestStatus.ON_THE_LINE);
        ewbRepository.save(ewb);
        ewbClosedSender.send(ewbMapper.ewbToEwbClosedMessage(ewb));
    }
}
