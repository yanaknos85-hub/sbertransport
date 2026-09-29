package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.database.dao.EwbRepository;
import ru.sber.transport.telemechanic.database.dao.EwbTitleRepository;
import ru.sber.transport.telemechanic.database.dao.RequestHistoryRepository;
import ru.sber.transport.telemechanic.database.dao.RequestRepository;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.*;
import ru.sber.transport.telemechanic.dto.ewb.ChecksTreeDto;
import ru.sber.transport.telemechanic.dto.ewb.MonitoringEwbDto;
import ru.sber.transport.telemechanic.dto.request.ActiveResponse;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.exception.*;
import ru.sber.transport.telemechanic.helper.CheckHelper;
import ru.sber.transport.telemechanic.helper.ReportSearchSpecHelper;
import ru.sber.transport.telemechanic.helper.RequestSearchSpecHelper;
import ru.sber.transport.telemechanic.human_readable_id.constant.Prefix;
import ru.sber.transport.telemechanic.mapper.RequestMapper;
import ru.sber.transport.telemechanic.service.*;
import ru.sberbank.ditsib.transport.exceptions.IllegalCallerResponseException;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;

import java.time.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static ru.sber.transport.telemechanic.enumerate.EwbStatus.*;
import static ru.sber.transport.telemechanic.enumerate.EwbStatus.IN_GARAGE;
import static ru.sber.transport.telemechanic.enumerate.RequestStatus.*;
import static ru.sber.transport.telemechanic.enumerate.RequestStatus.EXPIRED;
import static ru.sber.transport.telemechanic.enumerate.RequestStatus.ON_THE_LINE;
import static ru.sber.transport.telemechanic.exception.CloseRequestException.MSG_AUTHOR;
import static ru.sber.transport.telemechanic.exception.CloseRequestException.MSG_STATUS;

@RequiredArgsConstructor
@Service
@Transactional
public class RequestServiceImpl implements RequestService {
    public static final List<RequestStatus> CHECKS_COMPLETED_STATUSES = List.of(DONE, WARNING);
    private static final List<RequestStatus> FINISH_STATUSES = List.of(ON_THE_LINE, DONE, WARNING);
    private static final List<EwbStatus> EWB_FINISH_STATUSES =
            List.of(KORUS_DECLINED, MEDIC_DECLINED, TELEMECH_DECLINED, DRIVER_CANCELED, IN_GARAGE, EWB_CLOSED, EwbStatus.EXPIRED, EWB_CANCELLED);
    private final RequestRepository requestRepository;
    private final RequestHistoryRepository historyRepository;
    private final DepartmentService departmentService;
    private final TransportService transportService;
    private final EmployeeService employeeService;
    private final Clock clock;
    private final SQGenerator sqGenerator;
    private final RequestMapper requestMapper;
    private final EwbRepository ewbRepository;
    private final CheckHelper checkHelper;
    private final EwbTitleRepository ewbTitleRepository;
    private final EwbTariffService ewbTariffService;
    private final EwbPathService ewbPathService;
    private final DepartmentTimeZoneService departmentTimeZoneService;
    
    @Override
    public Request createEmpty(CreateRequestDto createRequestDto, Employee author, boolean newPath) {
        var organizationDigitId = departmentService.get(author.getDepartment().getId())
                                                   .map(Department::getOrganization)
                                                   .map(Organization::getDigitId)
                                                   .orElse(null);
        var humanReadableId = sqGenerator.getNextId(Prefix.TM, organizationDigitId);
        var transport = transportService.getTransportById(createRequestDto.transportId());
        var request = Request.builder()
                             .author(author)
                             .status(IN_PROGRESS)
                             .humanReadableId(humanReadableId)
                             .creationTime(LocalDateTime.now(clock))
                             .transport(transport)
                             .build();
        var checkList = Arrays.stream(CheckType.values())
                              .filter(checkType -> Objects.isNull(checkType.getInEwbPath()) ||
                                                   checkType.getInEwbPath().equals(newPath))
                              .map(checkType ->
                                           Check.builder()
                                                .checkType(checkType)
                                                .checkStatus(CheckStatus.IN_PROGRESS)
                                                .attempt(0)
                                                .request(request)
                                                .build())
                              .toList();
        request.getChecks().addAll(checkList);
        var contractorOrganizationId = ewbTariffService.getContractorOrganizationId(author.getDepartment().getId());
        if (contractorOrganizationId == null) {
            throw new TechnicContractNotFoundException();
        }
        request.setOrganizationId(contractorOrganizationId);
        requestRepository.save(request);
        saveStatusChangeHistory(request, request.getStatus(), author);
        return request;
    }
    
    @Override
    public Request get(UUID requestId) {
        return requestRepository.findById(requestId)
                                .orElseThrow(() -> new RequestNotFoundException(requestId));
    }
    
    @Override
    public ChecksTreeDto getChecksTreeByRequestId(UUID requestId) {
        var request = get(requestId);
        return new ChecksTreeDto()
                .withStateNumber(request.getTransport().getStateNumber())
                .withMileage(request.getTransport().getMileage())
                .withCallTelemech(CheckHelper.isCallTelemech(request.getChecks()))
                .withChecks(checkHelper.createChecksTree(request.getChecks()));
    }
    
    @Override
    public Request changeStatus(UUID requestId, RequestStatus newStatus, Employee authenticatedEmployee) {
        var request = requestRepository.findById(requestId)
                                       .orElseThrow(() -> new RequestNotFoundException(requestId));
        if (newStatus.equals(RequestStatus.CANCELED)) {
            validateCancelAuthor(authenticatedEmployee, request.getAuthor());
            validateCancelStatus(request.getStatus());
        }
        if (CHECKS_COMPLETED_STATUSES.contains(newStatus)) {
            request.setChecksFinishedTime(LocalDateTime.now(clock));
        }
        request.setStatus(newStatus);
        saveStatusChangeHistory(request, newStatus, authenticatedEmployee);
        return request;
    }
    
    @Override
    @Transactional
    public void changeStatusForCallTelemechanic(UUID requestId, UUID userId) {
        var request = get(requestId);
        if (request.getStatus() != IN_PROGRESS) {
            throw new BadRequestException("Невозможно перевести заявку из статуса %s".formatted(request.getStatus().name()));
        }
        var newStatus = request.getChecks().stream()
                               .allMatch(check -> check.getCheckStatus().equals(CheckStatus.DONE))
                        ? DONE
                        : WARNING;
        request.setStatus(newStatus);
        request.setChecksFinishedTime(LocalDateTime.now(clock));
        saveStatusChangeHistory(request, newStatus, employeeService.getByUserId(userId));
        requestRepository.save(request);
    }
    
    @Override
    @Transactional
    public void setChecksStartedTime(UUID requestId) {
        var request = get(requestId);
        if (request.getChecksStartedTime() == null) {
            request.setChecksStartedTime(LocalDateTime.now(clock));
        }
    }
    
    @Override
    public List<Request> getInStatusByAuthor(Employee employee, List<RequestStatus> statuses) {
        return requestRepository.findAllByAuthorAndStatusIn(employee, statuses);
    }
    
    @Override
    @Transactional
    public RequestOnTheLineDto getOnTheLineRequest(Employee employee) {
        if (ewbPathService.calculatingClientPath(employee)) {
            return onTheLineWithEwbPath(employee);
        } else {
            return onTheLineWithOldPath(employee);
        }
    }
    
    private RequestOnTheLineDto onTheLineWithEwbPath(Employee employee) {
        var departmentId = employee.getDepartment().getId();
        var timeZone = departmentTimeZoneService.getTimeZoneByDepartmentId(departmentId);
        var localDateInTimeZone = LocalDateTime.now(clock).atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of(timeZone)).toLocalDate();
        var ewb = ewbRepository.findEwbForRequestOnTheLine(employee.getId(), localDateInTimeZone, EWB_FINISH_STATUSES)
                               .orElseThrow(() -> new EwbNotFoundException(employee.getId(), LocalDate.now(clock)));
        var request = ewb.getRequest();
        if (Objects.nonNull(request)) {
            return createResponseOnTheLineDto(true, ewb.getId(), request, ewb.getOdometerOut(), ewb.getFuelLitreageOut(), ewb.isQrCode());
        }
        return createResponseOnTheLineDto(true, ewb.getId(), null, ewb.getOdometerOut(), ewb.getFuelLitreageOut(), ewb.isQrCode());
    }
    
    private RequestOnTheLineDto onTheLineWithOldPath(Employee employee) {
        var requestList = getInStatusByAuthor(employee, FINISH_STATUSES);
        if (requestList.isEmpty()) {
            return createResponseOnTheLineDto(false, null, null, null, null, false);
        } else {
            var request = requestList.stream()
                                     .max(Comparator.comparing(Request::getCreationTime))
                                     .orElseThrow(RequestNotFoundByAuthorException::new);
            return createResponseOnTheLineDto(false, null, request, null, null, false);
        }
    }
    
    @Override
    public ActiveResponse getInProgressRequest(UUID userId) {
        var employee = employeeService.getByUserId(userId);
        var requestList = getInStatusByAuthor(employee, Collections.singletonList(IN_PROGRESS));
        if (requestList.isEmpty()) {
            return null;
        } else if (requestList.size() == 1) {
            var request = requestList.stream()
                                     .findFirst()
                                     .orElseThrow(RequestNotFoundByAuthorException::new);
            return requestMapper.requestToActiveResponse(request);
        } else {
            var request = requestList.stream()
                                     .max(Comparator.comparing(Request::getCreationTime))
                                     .orElseThrow(RequestNotFoundByAuthorException::new);
            return requestMapper.requestToActiveResponse(request);
        }
    }
    
    @Override
    public void cancel(UUID requestId, Employee authenticatedEmployee) {
        this.changeStatus(requestId, RequestStatus.CANCELED, authenticatedEmployee);
    }
    
    @Override
    public Page<MonitoringRequestListDto> search(RequestSearchDto requestSearchDTO, UUID userId) {
        var searchDto = Optional.ofNullable(requestSearchDTO)
                                .orElse(new RequestSearchDto(null,
                                                             null,
                                                             null,
                                                             null,
                                                             new RequestSearchDto.PageSetting()));
        var employee = employeeService.getByUserId(userId);
        var result = doSearch(employee.getOrganization().getId(), searchDto, RequestSearchDto.getPageRequest(searchDto));
        return new PageImpl<>(
                result.stream()
                      .map(requestMapper::requestToMonitoringRequestListDto)
                      .toList(),
                result.getPageable(),
                result.getTotalElements());
    }
    
    @Override
    public PatchMonitoringResponse update(UUID requestId, MonitoringRequestDto newData, UUID userId) {
        //TODO add role check
        var employee = employeeService.getByUserId(userId);
        var request = this.get(requestId);
        validateEditStatus(request.getStatus());
        validateEditNewStatus(newData.requestStatus());
        enrichUpdatedRequest(request, newData);
        request.setInspector(employee);
        request.setInspectionTime(LocalDateTime.now(clock));
        saveStatusChangeHistory(request, newData.requestStatus(), employee);
        requestRepository.save(request);
        var checks = checkHelper.createChecksTree(request);
        return requestMapper.requestToPatchMonitoringResponse(request, checks);
    }
    
    @Override
    public List<RequestHistory> getStatusHistory(UUID requestId) {
        return historyRepository.findByRequestIdOrderByChangeTime(requestId);
    }
    
    @Override
    public Page<Request> search(ReportSearchDto reportSearchDto) {
        return doSearch(reportSearchDto, ReportSearchDto.getPageRequest(reportSearchDto));
    }

    @Override
    public MonitoringRequestDto getForMonitoring(UUID requestId, UUID userId) {
        //TODO add role check
        var request = get(requestId);
        var mappedRequest = requestMapper.requestToMonitoringRequestDto(request);
        var optionalEwb = ewbRepository.findEwbWithTransportByRequestId(requestId);
        if (optionalEwb.isPresent()) {
            var ewb = optionalEwb.get();
            var lastEwbTitleType = ewbTitleRepository.findByEwbId(ewb.getId())
                .stream()
                .max(Comparator.comparing(EwbTitle::getType))
                .orElseThrow(() -> new EwbNotFoundException(ewb.getId()));
            mappedRequest = mappedRequest.withEwb(new MonitoringEwbDto(ewb.getId(),
                    ewb.getHumanReadableId(),
                    ewb.getEwbUuid(),
                    ewb.getStatus(),
                    ewb.getStartDate(),
                    ewb.getFinishDate(),
                    EwbTitleType.getNextTitleType(lastEwbTitleType.getType())))
                .withTransport(mappedRequest.transport()
                    .withTransportType(ewb.getTransport().getType())
                    .withOdometerOut(ewb.getOdometerOut())
                    .withOdometerIn(ewb.getOdometerIn()));
        }
        return mappedRequest.withChecks(checkHelper.createChecksTree(request));
    }
    
    @Override
    @Transactional
    public void close(UUID requestId, UUID userId) {
        var employee = employeeService.getByUserId(userId);
        var request = get(requestId);
        
        if (!request.getAuthor().getId().equals(employee.getId())) {
            throw new CloseRequestException(MSG_AUTHOR);
        }
        if (!request.getStatus().equals(ON_THE_LINE)) {
            throw new CloseRequestException(MSG_STATUS);
        }
        
        request.setStatus(RequestStatus.FINISHED);
        saveStatusChangeHistory(request, RequestStatus.FINISHED, employee);
    }
    
    @Override
    @Transactional
    public void statusAutoUpdate() {
        var statuses = Set.of(IN_PROGRESS, WARNING, DONE);
        var requests = requestRepository.findAllByStatusInAndCreationTimeBetween(statuses,
                                                                                 LocalDateTime.now(clock).minusDays(2),
                                                                                 LocalDateTime.now(clock).minusDays(1));
        if (!requests.isEmpty()) {
            var comment = "Статус заявки изменен пользователем: Система (Планировщик)";
            var requestHistory = requests.stream()
                                         .map(request -> requestMapper.requestToRequestHistory(request, EXPIRED, comment, null))
                                         .toList();
            var updatedRequests = requests.stream()
                                          .map(request -> request.setStatus(EXPIRED))
                                          .toList();
            
            requestRepository.saveAll(updatedRequests);
            historyRepository.saveAll(requestHistory);
        }
    }
    
    @Override
    public void updateRequestThirdTitleSent(Request request, UUID userId) {
        request.setInspector(employeeService.getByUserId(userId));
        request.setInspectionTime(LocalDateTime.now(clock));
        requestRepository.save(request);
    }
    
    private void enrichUpdatedRequest(Request request, MonitoringRequestDto newData) {
        if (Objects.nonNull(newData.comment())) {
            request.setComment(newData.comment());
        }
        if (Objects.nonNull(newData.requestStatus())) {
            request.setStatus(newData.requestStatus());
        }
        newData.checks().forEach(monitoringCheckDto -> request.getChecks().stream()
                                                              .filter(check -> check.getId().equals(monitoringCheckDto.id()))
                                                              .findAny()
                                                              .ifPresent(check -> check.setComment(monitoringCheckDto.comment())));
    }
    
    private Page<Request> doSearch(UUID userOrganizationId, RequestSearchDto requestSearchDTO, Pageable pageable) {
        var spec = RequestSearchSpecHelper.getSpecification(userOrganizationId, requestSearchDTO);
        return requestRepository.findAll(spec, pageable);
    }
    
    private Page<Request> doSearch(ReportSearchDto reportSearchDto, Pageable pageable) {
        Specification<Request> spec = ReportSearchSpecHelper.getSpecification(reportSearchDto);
        return requestRepository.findAll(spec, pageable);
    }
    
    private void validateEditStatus(RequestStatus status) throws IllegalStateResponseException {
        if (Stream.of(DONE,
                      WARNING).noneMatch(Predicate.isEqual(status))) {
            throw new IllegalStateResponseException(String.format("Невозможно изменить заявку в статусе %s", status));
        }
    }
    
    private void validateEditNewStatus(RequestStatus status) {
        if (Stream.of(ON_THE_LINE,
                      RequestStatus.DECLINED,
                      IN_PROGRESS,
                      DONE,
                      WARNING).noneMatch(Predicate.isEqual(status))) {
            throw new IllegalStateResponseException(String.format("Невозможно изменить заявку на статус %s", status));
        }
    }
    
    private static void validateCancelAuthor(Employee authenticatedEmployee, Employee author) {
        if (!authenticatedEmployee.getId().equals(author.getId())) {
            throw new IllegalCallerResponseException("Только создатель заявки может ее отменить");
        }
    }
    
    private void validateCancelStatus(RequestStatus status) throws IllegalStateResponseException {
        if (Stream.of(DONE,
                      WARNING,
                      IN_PROGRESS).noneMatch(Predicate.isEqual(status))) {
            throw new IllegalStateResponseException(String.format("Невозможно отменить заявку в статусе %s", status));
        }
    }
    
    private void saveStatusChangeHistory(Request request, RequestStatus status, Employee employee) {
        historyRepository.save(new RequestHistory()
                                       .setRequestId(request.getId())
                                       .setComment("Статус заявки изменен пользователем: " +
                                                   employee.getLastName() + " " +
                                                   employee.getFirstName() +
                                                   (employee.getPatronymic() == null ? "" : (" " + employee.getPatronymic())))
                                       .setChangeTime(LocalDateTime.now(clock))
                                       .setStatus(status)
                                       .setInitiator(employee));
    }
    
    private RequestOnTheLineDto createResponseOnTheLineDto(
            boolean ewbPath,
            UUID ewbId,
            Request request,
            Integer odometerOut,
            Integer fuelLitreageOut,
            boolean qrCode
                                                          ) {
        return new RequestOnTheLineDto(ewbPath,
                                       ewbId,
                                       request == null ? null : request.getId(),
                                       request == null ? null : request.getStatus(),
                                       odometerOut,
                                       fuelLitreageOut,
                                       qrCode,
                                       getTransportInfo(request));
    }
    
    private RequestOnTheLineDto.TransportOnTheLineDto getTransportInfo(Request request) {
        if (Objects.nonNull(request) &&
            request.getStatus().equals(ON_THE_LINE)) {
            return new RequestOnTheLineDto.TransportOnTheLineDto(request.getTransport().getId(),
                                                                 request.getTransport().getStateNumber(),
                                                                 request.getTransport().getBrand(),
                                                                 request.getTransport().getModel(),
                                                                 request.getTransport().getFuelTankVolume());
        }
        return null;
    }
}
