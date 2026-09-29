package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.humanreadableid.model.Prefix;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.tariff.model.CalculatedDto;
import ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingJoinRequestProcessingModeSettingRepository;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingJoinRequestRepository;
import ru.sberbank.ditsib.transport.request.database.dao.CorporateCarsharingRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.dto.CancelDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.*;
import ru.sberbank.ditsib.transport.request.mappers.CarsharingJoinRequestMapper;
import ru.sberbank.ditsib.transport.request.mappers.ContractorMapper;
import ru.sberbank.ditsib.transport.request.service.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus.CANCELLED;

@Service
@RequiredArgsConstructor
@Transactional
public class CarsharingJoinRequestServiceImpl implements CarsharingJoinRequestService {
    
    private final CorporateCarsharingRepository carsharingRepository;
    private final CorporateCarsharingService carsharingService;
    private final CarsharingTariffService tariffService;
    private final ContractorService contractorService;
    private final CarsharingJoinRequestTextService textService;
    private final CarsharingJoinRequestRepository joinRequestRepository;
    private final CarsharingJoinRequestProcessingModeSettingRepository modeSettingRepository;
    private final ContractorMapper contractorMapper;
    private final CarsharingJoinRequestMapper joinRequestMapper;
    private final SQGenerator sqGenerator;
    
    @Override
    public List<CarsharingJoinAndCalculatedDTO> getCarsharingJoinsForEmployee(
            List<CalculatedDto> calculatedList, UUID organizationId, UUID employeeId) {
        // отфильтруем список по типу транспорта и приведем его к мапе
        final var carsharingCalculatedAndTariffIds =
                calculatedList.stream()
                              .filter(dto -> TransportTypeEnum.CARSHARING.name().equals(dto.getTransportType().getName()))
                              .collect(Collectors.toMap(
                                      el -> el,
                                      CalculatedDto::getId));
        
        Map<CalculatedDto, CarsharingTariff> carsharingCalculatedAndTariffs =
                carsharingCalculatedAndTariffIds.entrySet().stream()
                                                .collect(Collectors.toMap(
                                                        Map.Entry::getKey,
                                                        e -> tariffService.getTariffById(e.getValue())));
        
        Map<CalculatedDto, CorporateCarsharing> carsharingCalculatedAndCarsharings =
                carsharingCalculatedAndTariffs.entrySet().stream()
                                              .collect(Collectors.toMap(
                                                      Map.Entry::getKey,
                                                      e -> carsharingService.getByContractIdAndOrganizationId(
                                                              e.getValue().getContractId(), organizationId)));

        return carsharingCalculatedAndCarsharings
                .entrySet()
                .stream()
                .map(e -> CarsharingJoinAndCalculatedDTO.builder()
                        .calculatedData(e.getKey())
                        .region(e.getValue().getContract().getRegion())
                        .contractor(contractorMapper.entityToDto(
                                contractorService.get(e.getValue().getContract().getContractorId())))
                        .joined(carsharingService.checkEmployeesSetContainsUuid(
                                e.getValue().getJoinedEmployees(), employeeId))
                        .build())
                .toList();
    }
    
    // todo добавить в запрос с фронта регион, когда будет реализовано региональное деление
    @Override
    public GetCarsharingJoinRequestDTO getCreatedOrBlank(UUID organizationId, Employee employee) {
        // проверить, созданы в БД текстовые поля, если нет - создать
        CarsharingJoinRequestText joinRequestText = textService.getOrCreateDefaults(organizationId);
        // проверить, что заявка еще не создана
        List<CarsharingJoinRequest> requests = joinRequestRepository.findByEmployeeIdAndRequestStatus(
                employee.getId(), CarsharingJoinRequestStatus.UNDER_CONSIDERATION);
        if (requests.isEmpty()) {
            // создать заявку для сотрудника, выбрав для него все возможные каршеринги, к которым он не подключен
            Set<ContractorAndJoinStatus> contractors = getPossibleToJoiningCarsharings(organizationId,
                                                                                       employee.getId());
            // если нет доступных каршерингов, прервать создание заявки
            if (contractors.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        String.format("Сотрудник ID '%s' уже подключен ко всем возможным корп.каршерингам!",
                                      employee.getId()));
            }
            //заполнить номер телефона по умолчанию из профиля сотрудника
            CarsharingJoinRequest joinRequest = CarsharingJoinRequest.builder()
                                                                     .employee(employee)
                                                                     .phone(employee.getMobilePhone())
                                                                     .contractors(contractors)
                                                                     .text(joinRequestText)
                                                                     .build();
            return joinRequestMapper.joinRequestToDto(joinRequest);
        } else {
            // если заявка уже создана, вернуть ее из БД
            return joinRequestMapper.joinRequestToDto(requests.getFirst());
        }
        //todo отправка сообщения, сендер
    }
    
    @Override
    public GetCarsharingJoinRequestDTO create(
            NewCarsharingJoinRequestDTO joinRequestDto, Organization organization, Employee employee) {
        // проверить, созданы ли в БД текстовые поля, если нет - создать
        CarsharingJoinRequestText joinRequestText = textService.getOrCreateDefaults(organization.getId());
        // проверить, что заявка еще не создана, иначе прервать создание заявки
        List<CarsharingJoinRequest> requests = joinRequestRepository.findByEmployeeIdAndRequestStatus(
                employee.getId(), CarsharingJoinRequestStatus.UNDER_CONSIDERATION);
        if (!requests.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Заявка для сотрудника с ID '%s' уже создана! ID заявки '%s'",
                                  employee.getId(), requests.getFirst().getId()));
        }
        // проверить заполнение чекбоксов, если хоть с чем-то сотрудник не согласен - отклонить заявку
        if (!joinRequestDto.isPersonalDataAgree() || !joinRequestDto.isRulesP144Agree()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Заявка сотрудника с ID '%s' отклонена по причине несогласия с Правилами " +
                                  "Памятки П-144 или Политикой обработки персональных данных", employee.getId()));
        }
        Set<ContractorAndJoinStatus> carsharingsFromDb =
                getPossibleToJoiningCarsharings(organization.getId(), employee.getId());
        CarsharingJoinRequest joinRequest = joinRequestMapper.newDtoToJoinRequest(joinRequestDto);
        Set<ContractorAndJoinStatus> carsharingsFromRequest = joinRequest.getContractors();
        // убедиться, что полученный список каршерингов совпадает с разрешенным для сотрудника
        checkCarhsaringsFromRequest(employee.getId(), carsharingsFromRequest, carsharingsFromDb);
        // установить актуальные статусы заявки
        checkEmployeeChoice(carsharingsFromRequest, employee.getId());
        // дозаполнить joinRequest, сохранить и вернуть dto
        joinRequest.setHumanReadableId(sqGenerator.getNextId(Prefix.CR, organization.getDigitId()));
        joinRequest.setEmployee(employee);
        joinRequest.setCreationTime(LocalDateTime.now());
        joinRequest.setText(joinRequestText);
        joinRequest = joinRequestRepository.save(joinRequest);
        
        /*
            TODO Если режим обработки - ручной, то заявка уходит в кабинет инженеру
             Если режим автоматический - то заявка улетает по интеграции в каршеринговую компанию
        */
        List<CarsharingJoinRequestProcessingModeSetting> settings = modeSettingRepository.findAll();
        if (settings.size() == 1 && settings.getFirst().getProcessingMode().equals(
                CarsharingJoinRequestProcessingMode.AUTOMATICALLY_BY_INTEGRATION)
        ) {
            // todo код, связанный с интеграцией...
        }
        //todo отправка сообщения, сендер
        
        return joinRequestMapper.joinRequestToDto(joinRequest);
    }
    
    @Override
    public void update(UpdateCarsharingJoinRequestDTO joinRequestDto, CarsharingJoinRequest joinRequestFromDb) {
        // проверить, что статус заявки "В обработке", иначе прервать изменение заявки
        checkRequestStatus(joinRequestFromDb);
        // сравнить список каршерингов из запроса со списком из БД
        CarsharingJoinRequest updatedJoinRequest = joinRequestMapper.updateDtoToJoinRequest(joinRequestDto);
        Set<ContractorAndJoinStatus> carsharingsFromRequest = updatedJoinRequest.getContractors();
        Set<ContractorAndJoinStatus> carsharingsFromDb = joinRequestFromDb.getContractors();
        UUID employeeId = joinRequestFromDb.getEmployee().getId();
        checkCarhsaringsFromRequest(employeeId, carsharingsFromRequest, carsharingsFromDb);
        // проверить выбор каршерингов
        checkEmployeeChoice(carsharingsFromRequest, employeeId);
        joinRequestFromDb.setContractors(carsharingsFromRequest);
        // перезаписать сущность из БД
        joinRequestFromDb.setPhone(updatedJoinRequest.getPhone());
        joinRequestFromDb.setEmail(updatedJoinRequest.getEmail());
        joinRequestRepository.save(joinRequestFromDb);
        
        /*
            TODO Если режим обработки - ручной, то заявка уходит в кабинет инженеру
             Если режим автоматический - то заявка улетает по интеграции в каршеринговую компанию
        */
        List<CarsharingJoinRequestProcessingModeSetting> settings = modeSettingRepository.findAll();
        if (settings.size() == 1 && settings.getFirst().getProcessingMode().equals(
                CarsharingJoinRequestProcessingMode.AUTOMATICALLY_BY_INTEGRATION)
        ) {
            // todo код, связанный с интеграцией...
        }
        //todo отправка сообщения, сендер
    }
    
    @Override
    public void cancel(CarsharingJoinRequest request, CancelDTO cancelDTO) {
        request.setRequestStatus(CANCELLED);
        request.setStatusCode(cancelDTO.getCode());
        request.setCancelReason(cancelDTO.getReason());
        joinRequestRepository.save(request);
    }
    
    @Override
    public void delete(CarsharingJoinRequest joinRequestFromDb) {
        joinRequestRepository.delete(joinRequestFromDb);
    }
    
    @Override
    public CarsharingJoinRequest getById(UUID joinRequestId) {
        return joinRequestRepository.findById(joinRequestId).orElseThrow(
                () -> new EntityNotFoundException(CarsharingJoinRequest.class, joinRequestId));
    }
    
    @Override
    public GetCarsharingJoinRequestDTO getDtoById(UUID joinRequestId) {
        return joinRequestMapper.joinRequestToDto(getById(joinRequestId));
    }
    
    @Override
    public List<GetCarsharingJoinRequestShortDTO> getByStatus(CarsharingJoinRequestStatus status) {
        return joinRequestMapper.getJoinRequestsToShortDtos(joinRequestRepository.findByRequestStatus(status));
    }
    
    @Override
    public GetCarsharingJoinRequestDTO process(
            Set<ProcessedContractorAndJoinStatusDTO> processedDtos, UUID joinRequestId, Organization organization) {
        // проверить существование заявки
        CarsharingJoinRequest joinRequestFromDb = getById(joinRequestId);
        // проверить, что статус заявки "В обработке", иначе прервать изменение заявки
        checkRequestStatus(joinRequestFromDb);
        // сравнить список каршерингов из запроса со списком из БД
        Set<ContractorAndJoinStatus> joinsFromRequest =
                joinRequestMapper.processedDtoSetToContractorStatuses(processedDtos);
        Set<ContractorAndJoinStatus> joinsFromDb = joinRequestFromDb.getContractors();
        Employee employee = joinRequestFromDb.getEmployee();
        checkCarhsaringsFromRequest(employee.getId(), joinsFromRequest, joinsFromDb);
        
        // установить статусы подключений и, если необходимо, добавить сотрудника в каршеринги
        UUID organizationId = organization.getId();
        for (ContractorAndJoinStatus join : joinsFromDb) {
            // отфильтровать заранее проверенный список. Д.б. только один элемент
            ContractorAndJoinStatus joinFromRequest =
                    joinsFromRequest.stream()
                                    .filter(c -> c.getContractor().getId().equals(join.getContractor().getId()) &&
                                                 c.getRegion().equals(join.getRegion()))
                                    .findFirst().orElseThrow();
            // установить статус "отклонено"
            join.setJoinStatus(CorporateCarsharingJoinStatus.DECLINED);
            // если сотрудником был осуществлен выбор, и инженер осуществил подключение, то вписать в список каршеринга
            if (joinFromRequest.getJoinStatus().equals(CorporateCarsharingJoinStatus.JOINED) && join.isEmployeeChoice()) {
                CorporateCarsharing carsharing = carsharingRepository
                        .findByContractContractorIdAndContractRegionAndOrganizationIdAndActive(
                                join.getContractor().getId(), join.getRegion(), organizationId, true)
                        .orElseThrow(() -> new EntityNotFoundException(
                                CorporateCarsharing.class,
                                "by contractor " + join.getContractor().getId() +
                                "and region" + join.getRegion() +
                                "and organization " + organizationId));
                
                carsharing.getJoinedEmployees().add(employee);
                carsharingRepository.save(carsharing);
                // заменить статус на "подключено"
                join.setJoinStatus(CorporateCarsharingJoinStatus.JOINED);
            }
        }
        joinRequestFromDb.setRequestStatus(CarsharingJoinRequestStatus.DONE);
        joinRequestFromDb = joinRequestRepository.save(joinRequestFromDb);
        return joinRequestMapper.joinRequestToDto(joinRequestFromDb);
    }
    
    /**
     * Вернуть для сотрудника все каршеринги, к которым он не подключен
     * @param organizationId ID корп.клиента
     * @param employeeId ID сотрудника
     * @return неподключенные каршеринги
     */
    private Set<ContractorAndJoinStatus> getPossibleToJoiningCarsharings(UUID organizationId, UUID employeeId) {
        return carsharingService.getByOrganizationId(organizationId).stream()
                .filter(c -> !carsharingService.checkEmployeesSetContainsUuid(
                        c.getJoinedEmployees(), employeeId))
                .map(c -> ContractorAndJoinStatus.builder()
                        .region(c.getContract().getRegion())
                        .contractor(contractorService.get(c.getContract().getContractorId()))
                        .build())
                .collect(Collectors.toSet());
    }
    
    /**
     * Сравнить полученный список каршерингов с допустимым для сотрудника корп.клиента. Использовать связку
     * "регион - id контрагента"
     * @param employeeId ID сотрудника
     * @param carsharingsFromRequest полученный список каршерингов
     * @param carsharingsFromDb допустимый список каршерингов
     */
    private void checkCarhsaringsFromRequest(UUID employeeId, Set<ContractorAndJoinStatus> carsharingsFromRequest,
                                             Set<ContractorAndJoinStatus> carsharingsFromDb) {
        // проверить, что размерность совпадает
        if (carsharingsFromRequest.size() != carsharingsFromDb.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Заявка для сотрудника с ID '%s' содержит некорректное количество корп.каршерингов!",
                                  employeeId));
        }
        // проверить каждый элемент по полям "регион - id контрагента" из одного списка в другом
        for (ContractorAndJoinStatus el : carsharingsFromRequest) {
            if (carsharingsFromDb.stream()
                    .filter(c -> c.getContractor().getId().equals(el.getContractor().getId()) &&
                                                       c.getRegion().equals(el.getRegion())).count() != 1) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Заявка для сотрудника с ID '%s' содержит не разрешенные для подключения " +
                                      "каршеринги!", employeeId));
            }
        }
    }
    
    /**
     * Проверить, что сотрудник выбрал хотя бы один каршеринг
     * @param carsharings Set<ContractorAndJoinStatus> из заявки
     * @param employeeId ID сотрудника
     */
    private void checkEmployeeChoice(Set<ContractorAndJoinStatus> carsharings, UUID employeeId) {
        long chosen = carsharings.stream()
                .filter(ContractorAndJoinStatus::isEmployeeChoice)
                .count();
        if (chosen == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Заявка для сотрудника с ID '%s' не содержит выбранных для подключения " +
                                  "корп.каршерингов!", employeeId));
        }
    }
    
    /**
     * Проверить статус заявки
     *
     * @param joinRequest заявка из БД
     */
    private void checkRequestStatus(CarsharingJoinRequest joinRequest) {
        if (!CarsharingJoinRequestStatus.UNDER_CONSIDERATION.equals(joinRequest.getRequestStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Заявка с ID '%s' не может быть отредактирована, т.к. она уже была обработана!",
                                  joinRequest.getId()));
        }
    }
}
