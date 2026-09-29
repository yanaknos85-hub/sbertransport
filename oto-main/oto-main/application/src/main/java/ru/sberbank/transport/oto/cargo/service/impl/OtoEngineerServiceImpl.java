package ru.sberbank.transport.oto.cargo.service.impl;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.SingularAttribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.sber.transport.request.enums.RequestTypeEnum;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.transport.oto.cargo.database.dao.TemplateForCargoRepository;
import ru.sberbank.transport.oto.cargo.database.model.*;
import ru.sberbank.transport.oto.cargo.mappers.EvaluationDTOMapper;
import ru.sberbank.transport.oto.cargo.mappers.RequestMapper;
import ru.sberbank.transport.oto.cargo.mappers.TemplateMapper;
import ru.sberbank.transport.oto.cargo.service.*;
import ru.sberbank.transport.oto.cargo.database.model.template.TemplateForCargo;
import ru.sberbank.transport.oto.cargo.database.model.template.TemplateForCargo_;
import ru.sberbank.transport.oto.cargo.dto.FactDataDTO;
import ru.sberbank.transport.oto.cargo.dto.OrganizationAndOrganizationGroup;
import ru.sberbank.transport.oto.cargo.dto.cargo.CargoRequestDto;
import ru.sberbank.transport.oto.cargo.dto.cargo.ExpectedCargoDataDTO;
import ru.sberbank.transport.oto.cargo.dto.cargo.OtoEngineerCargoRequestDetailDTO;
import ru.sberbank.transport.oto.cargo.dto.oto.GetTemplateForCargoForOtoDto;
import ru.sberbank.transport.oto.cargo.enums.CriticalExpireDateEnum;
import ru.sberbank.transport.oto.cargo.enums.SortDirection;
import ru.sberbank.transport.oto.cargo.exception.AccessOrganizationForbiddenException;
import ru.sberbank.transport.oto.cargo.mappers.*;
import ru.sberbank.transport.oto.cargo.util.CheckOrganizationAccessUtils;

import java.time.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OtoEngineerServiceImpl implements OtoEngineerService {
    private final RequestService requestService;

    private final RequestMapper mapper;

    private final EvaluationDTOMapper evaluationDTOMapper;

    private final DepartmentService departmentService;

    private final OrganizationService organizationService;

    private final EmployeeService employeeService;

    private final TemplateForCargoRepository templateForCargoRepository;

    private final TemplateMapper templateMapper;

    @Override
    public Page<OtoEngineerCargoRequestDetailDTO> getCargoRequests(
            CargoRequestDto dto,
            JwtAuthenticationToken authentication
    ) {
        OrganizationAndOrganizationGroup searchDto = Optional.ofNullable(dto.organizationId())
                .map(orgId -> checkOrganization(authentication, orgId))
                .orElseGet(() -> OrganizationAndOrganizationGroup.builder().build());

        Specification<Request> requestSpecification = (root, q, b) -> {
            var predicate = b.equal(b.literal(1), 1);
            predicate = addBasePredicate(dto.id(), Request_.humanReadableId, root, b, predicate);
            predicate = addStatusPredicate(dto.statuses(), Request_.status, root, b, predicate);
            predicate = addRequestTypePredicate(dto.requestTypes(), Request_.requestType, root, b, predicate);

            if (dto.senderName() != null) {
                var pattern = Pattern.compile("[ ,]");
                var check = pattern.split(dto.senderName().replace(",", " ").replaceAll("( )+", " "));
                for (var s : check) {
                    var checkNameS = b.literal("%" + s + "%");
                    predicate = b.and(predicate, b.like(b.lower(root.get(Request_.senderName)), b.lower(checkNameS)));
                }
            }

            if (dto.recipientName() != null) {
                var pattern = Pattern.compile("[ ,]");
                var check = pattern.split(dto.recipientName().replace(",", " ").replaceAll("( )+", " "));
                for (var s : check) {
                    var checkNameS = b.literal("%" + s + "%");
                    predicate = b.and(predicate, b.like(b.lower(root.get(Request_.recipientName)), b.lower(checkNameS)));
                }
            }

            if (dto.senderAddress() != null) {
                var pattern = Pattern.compile("[ ,]");
                var check = pattern.split(dto.senderAddress().replace(",", " ").replaceAll("( )+", " "));
                var waypointAddressJoin = root.join(Request_.START_WAYPOINT);
                var addressJoin = waypointAddressJoin.join(Waypoint_.ADDRESS);
                for (var s : check) {
                    var checkNameS = b.literal("%" + s + "%");
                    predicate = b.and(predicate, b.like(b.upper(addressJoin.get(Address_.ADDRESS_STRING)), b.upper(checkNameS)));
                }
            }

            if (dto.recipientAddress() != null) {
                var pattern = Pattern.compile("[ ,]");
                var check = pattern.split(dto.recipientAddress().replace(",", " ").replaceAll("( )+", " "));
                var waypointAddressJoin = root.join(Request_.END_WAYPOINT);
                var addressJoin = waypointAddressJoin.join(Waypoint_.ADDRESS);
                for (var s : check) {
                    var checkNameS = b.literal("%" + s + "%");
                    predicate = b.and(predicate, b.like(b.upper(addressJoin.get(Address_.ADDRESS_STRING)), b.upper(checkNameS)));
                }
            }

            predicate = addExecuterGroupPredicate(dto, root, b, predicate);

            predicate = addLocalDateTimePredicate(dto.creationTimeFrom(), dto.creationTimeTo(), Request_.creationTime, root, b, predicate);
            predicate = addLocalDateTimePredicate(dto.desiredTimeFrom(), dto.desiredTimeTo(), Request_.desiredDate, root, b, predicate);
            predicate = addLocalDateTimePredicate(dto.controlTimeFrom(), dto.controlTimeTo(), Request_.controlDate, root, b, predicate);
            predicate = addLocalDateTimePredicate(dto.approvalTimeFrom(), dto.approvalTimeTo(), Request_.approvalDate, root, b, predicate);
            predicate = addLocalDateTimePredicate(dto.transferTimeFrom(), dto.transferTimeTo(), Request_.transferTime, root, b, predicate);
            predicate = addLocalDateTimePredicate(dto.shipmentTimeFrom(), dto.shipmentTimeTo(), Request_.shipmentTime, root, b, predicate);

            if (searchDto.getOrganizationId() != null && searchDto.getOrganizationGroupId() == null) {
                Join<Request, Organization> organizationRoot = root.join(Request_.ORGANIZATION);
                predicate = b.and(predicate, b.equal(organizationRoot.get(Organization_.ID),
                        searchDto.getOrganizationId()));
            }

            if (searchDto.getOrganizationId() != null && searchDto.getOrganizationGroupId() != null) {
                Join<Request, Organization> organizationRoot = root.join(Request_.ORGANIZATION);
                Join<Organization, OrganizationGroup> groupJoin = organizationRoot.join(Organization_.organizationGroup);
                var predicateGroup = b.equal(groupJoin.get(OrganizationGroup_.ID), searchDto.getOrganizationGroupId());
                var predicateOrg = b.equal(organizationRoot.get(Organization_.ID), searchDto.getOrganizationId());
                var predicateGroupAndOrg = b.and(predicateOrg, predicateGroup);
                predicate = b.and(predicate, predicateGroupAndOrg);
            }

            if (dto.authorDepartment() != null) {
                var departments = departmentService.findAllChildren(dto.authorDepartment());
                predicate = addAuthorDepartmentPredicate(departments, root, b, predicate);
            }

            if (dto.transportType() == null) {
                predicate = b.and(predicate,
                        b.and(root.get(Request_.transportType)
                                .in(TransportTypeEnum.getByServiceType(TransportServiceType.CARGO_TRANSPORTATION).stream()
                                        .map(TransportTypeEnum::getName)
                                        .toList())));
            } else {
                predicate = b.and(predicate, b.equal(root.get(Request_.transportType), dto.transportType().name()));
            }

            predicate = addCriticalExpireDate(dto.criticalExpireDate(), root, b, predicate);

            return predicate;
        };

        var requests = requestService.findAllForCargo(
                requestSpecification,
                dto.getOrDefaultPageSize(),
                dto.getOrDefaultPage(),
                dto.getOrDefaultSortDirection(),
                dto.getOrDefaultSortField());
        List<OtoEngineerCargoRequestDetailDTO> otoEngineerCargoRequestDetailDTOS = getOtoEngineerCargoRequestDetailDTOS(requests.getContent());

        return new PageImpl<>(otoEngineerCargoRequestDetailDTOS,
                requests.getPageable(),
                requests.getTotalElements());
    }

    private static Predicate addExecuterGroupPredicate(CargoRequestDto dto, Root<Request> root, CriteriaBuilder b, Predicate predicate) {
        if (!CollectionUtils.isEmpty(dto.executorGroupIds())
                && !dto.isEmptyExecutorGroup()) {
            predicate = b.and(predicate, root.get(Request_.executorGroupId).in(dto.executorGroupIds()));

        } else if (dto.isEmptyExecutorGroup()) {
            predicate = b.and(predicate, root.get(Request_.executorGroupId).isNull());
        }
        return predicate;
    }

    private static Predicate addCriticalExpireDate(CriticalExpireDateEnum criticalExpireDate, Root<Request> root, CriteriaBuilder b, Predicate predicate) {
        if (criticalExpireDate != null) {
            Expression<LocalDate> cdLess20PctDate = b.function(
                    "calculate_date_by_percent",
                    LocalDate.class,
                    root.get(Request_.desiredDate),
                    root.get(Request_.controlDate),
                    b.literal(20)
            );

            Expression<LocalDate> cdLess50PctDate = b.function(
                    "calculate_date_by_percent",
                    LocalDate.class,
                    root.get(Request_.desiredDate),
                    root.get(Request_.controlDate),
                    b.literal(50)
            );

            // Compare with current date
            switch (criticalExpireDate) {
                case LESS_THAN_20 -> predicate = b.and(predicate,
                        b.lessThanOrEqualTo(cdLess20PctDate, LocalDate.now(ZoneOffset.UTC)));

                case BETWEEN_20_AND_50 -> predicate = b.and(predicate,
                        b.and(
                                b.greaterThan(cdLess20PctDate, LocalDate.now(ZoneOffset.UTC)),
                                b.lessThanOrEqualTo(cdLess50PctDate, LocalDate.now(ZoneOffset.UTC))
                        ));

                case MORE_THAN_50 -> predicate = b.and(predicate,
                        b.greaterThan(cdLess50PctDate, LocalDate.now(ZoneOffset.UTC)));
            }
        }
        return predicate;
    }

    private Predicate addBasePredicate(
            String variable, SingularAttribute<Request, String> attribute, Root<Request> root, CriteriaBuilder b, Predicate predicate
    ) {
        if (variable != null) {
            predicate = b.and(predicate, b.like(root.get(attribute), "%" + variable + "%"));
        }
        return predicate;
    }

    private Predicate addStatusPredicate(
            List<TripRequestStatus> status, SingularAttribute<Request, String> attribute, Root<Request> root, CriteriaBuilder b, Predicate predicate
    ) {
        if (!CollectionUtils.isEmpty(status)) {
            predicate = b.and(predicate,
                    root.get(attribute).in(status.stream().map(TripRequestStatus::name)
                            .collect(Collectors.toSet())));
        }
        return predicate;
    }

    private Predicate addRequestTypePredicate(
            List<RequestTypeEnum> requestTypes,
            SingularAttribute<Request, String> attribute,
            Root<Request> root,
            CriteriaBuilder b,
            Predicate predicate
    ) {
        if (!CollectionUtils.isEmpty(requestTypes)) {
            predicate = b.and(predicate,
                    b.or(
                            b.and(root.get(attribute).isNull(),
                                    b.isFalse(root.get(Request_.template)),
                                    b.literal(RequestTypeEnum.SINGLE.name()).in(requestTypes.stream().map(RequestTypeEnum::name)
                                            .collect(Collectors.toSet()))),
                            b.and(root.get(attribute).isNull(),
                                    b.isTrue(root.get(Request_.template)),
                                    b.literal(RequestTypeEnum.REGULAR.name()).in(requestTypes.stream().map(RequestTypeEnum::name)
                                            .collect(Collectors.toSet()))),
                            root.get(attribute).in(requestTypes.stream().map(RequestTypeEnum::name)
                                    .collect(Collectors.toSet()))));
        }
        return predicate;
    }


    private Predicate addDepartmentPredicate(
            List<UUID> departments, Join<Employee, Department> departmentJoin, CriteriaBuilder b, Predicate predicate
    ) {
        if (!departments.isEmpty()) {
            predicate = b.and(predicate, departmentJoin.get(Department_.id).in(departments));
        }
        return predicate;
    }

    private Predicate addAuthorDepartmentPredicate(
            List<UUID> departments, Root<Request> root, CriteriaBuilder b, Predicate predicate
    ) {
        if (!departments.isEmpty()) {
            predicate = b.and(predicate,
                    root.join(Request_.author).join(Employee_.DEPARTMENT).get(Department_.ID)
                            .in(departments));
        }
        return predicate;
    }

    private Predicate addEmployeePredicate(
            String variable, CriteriaBuilder b, Predicate predicate,
            Join<Request, Employee> join
    ) {
        if (variable != null) {
            var lastName = join.<String>get(Employee_.LAST_NAME);
            var firstName = join.<String>get(Employee_.FIRST_NAME);
            var patronymic = join.<String>get(Employee_.PATRONYMIC);
            var lastFirstPatronymic = b.concat(lastName, b.concat(firstName, patronymic));
            var firstLastPatronymic = b.concat(firstName, b.concat(lastName, patronymic));
            var firstPatronymicLast = b.concat(firstName, b.concat(patronymic, lastName));
            var checkName = b.literal(variable.replace(" ", "") + "%");
            return b.and(predicate, b.or(
                    b.like(b.lower(lastFirstPatronymic), b.lower(checkName)),
                    b.like(b.lower(firstLastPatronymic), b.lower(checkName)),
                    b.like(b.lower(firstPatronymicLast), b.lower(checkName))
            ));
        }
        return predicate;
    }

    private Predicate addAddressPredicate(
            String variable, Root<Request> root, CriteriaBuilder b,
            CriteriaQuery<?> q, Predicate predicate
    ) {
        if (variable != null) {
            var subQuery = q.subquery(Waypoint.class);
            var waypointFrom = subQuery.from(Waypoint.class);
            var concatenatedAddress = concatenateAddress(waypointFrom, b);
            subQuery.select(waypointFrom);
            var checkAddress = "%" + variable + "%";
            var subPredicate = b.equal(waypointFrom.get(Waypoint_.REQUEST).get(Request_.ID), root.get(Request_.id));
            subPredicate = b.and(subPredicate, b.equal(waypointFrom.get(Waypoint_.ORDERING_INDEX), 0));
            subPredicate = b.and(subPredicate, b.like(b.lower(concatenatedAddress), b.lower(b.literal(checkAddress))));
            subQuery.where(subPredicate);

            return b.and(predicate, b.exists(subQuery));
        }
        return predicate;
    }

    private Expression<String> concatenateAddress(
            Root<Waypoint> waypointFrom,
            CriteriaBuilder b
    ) {
        var address = waypointFrom.join(Waypoint_.address);
        return b.concat(b.coalesce(address.get(Address_.COUNTRY), ""),
                b.concat(" ",
                        b.concat(b.coalesce(address.get(Address_.REGION), ""),
                                b.concat(" ",
                                        b.concat(b.coalesce(address.get(Address_.CITY), ""),
                                                b.concat(" ",
                                                        b.concat(b.coalesce(address.get(Address_.STREET), ""),
                                                                b.concat(" ",
                                                                        b.coalesce(address.get(Address_.HOUSE), "")))))))));
    }

    private Predicate addLocalDateTimePredicate(
            LocalDateTime variableFrom, LocalDateTime variableTo,
            SingularAttribute<Request, LocalDateTime> attribute,
            Root<Request> root, CriteriaBuilder b,
            Predicate predicate
    ) {
        if (variableFrom != null || variableTo != null) {
            var checkTimeFrom = Optional.ofNullable(variableFrom).orElse(LocalDateTime.now(ZoneOffset.UTC));
            var checkTimeTo = Optional.ofNullable(variableTo).orElse(LocalDateTime.now(ZoneOffset.UTC));
            predicate = b.and(predicate, b.between(root.get(attribute), checkTimeFrom, checkTimeTo));
        }
        return predicate;
    }

    private Predicate addLocalDateTimePredicate(
            ZonedDateTime variableFrom, ZonedDateTime variableTo,
            SingularAttribute<Request, LocalDateTime> attribute,
            Root<Request> root, CriteriaBuilder b,
            Predicate predicate
    ) {
        var localDateTimeVariableFrom = Optional.ofNullable(variableFrom)
                .map(time -> time.toInstant().atZone(ZoneOffset.UTC))
                .map(ZonedDateTime::toLocalDateTime).orElse(null);
        var localDateTimeVariableTo = Optional.ofNullable(variableTo)
                .map(time -> time.toInstant().atZone(ZoneOffset.UTC))
                .map(ZonedDateTime::toLocalDateTime).orElse(null);

        return addLocalDateTimePredicate(localDateTimeVariableFrom, localDateTimeVariableTo, attribute, root, b, predicate);
    }

    private List<OtoEngineerCargoRequestDetailDTO> getOtoEngineerCargoRequestDetailDTOS(List<Request> requests) {
        if (isEmpty(requests)) {
            return new ArrayList<>();
        }

        return requests.stream().map(this::toOtoCargoDetails).toList();
    }

    public Page<GetTemplateForCargoForOtoDto> getTemplatesForCargo
            (
                    UUID organizationId, Integer size, Integer page,
                    SortDirection direction,
                    String field,
                    String humanReadableId,
                    List<TripRequestStatus> status,
                    LocalDateTime creationTimeFrom,
                    LocalDateTime creationTimeTo,
                    String senderName,
                    String senderAddress,
                    String recipientName,
                    String recipientAddress,
                    UUID authorDepartment
            ) {

        Specification<TemplateForCargo> templateSpecification = (root, q, b) -> {
            var predicate = b.equal(b.literal(1), 1);

            if (humanReadableId != null) {
                predicate = b.and(predicate, b.like(root.get(TemplateForCargo_.humanReadableId), "%" + humanReadableId + "%"));
            }

            if (!CollectionUtils.isEmpty(status)) {
                predicate = b.and(predicate,
                        root.get(TemplateForCargo_.status).in(new HashSet<>(status)));
            }

            if (senderName != null) {
                var pattern = Pattern.compile("[ ,]");
                var check = pattern.split(senderName.replace(",", " ").replaceAll("( )+", " "));
                for (var s : check) {
                    var checkNameS = b.literal("%" + s + "%");
                    predicate = b.and(predicate, b.like(b.lower(root.get(TemplateForCargo_.senderName)), b.lower(checkNameS)));
                }
            }

            if (recipientName != null) {
                var pattern = Pattern.compile("[ ,]");
                var check = pattern.split(recipientName.replace(",", " ").replaceAll("( )+", " "));
                for (var s : check) {
                    var checkNameS = b.literal("%" + s + "%");
                    predicate = b.and(predicate, b.like(b.lower(root.get(TemplateForCargo_.recipientName)), b.lower(checkNameS)));
                }
            }

            if (senderAddress != null) {
                var pattern = Pattern.compile("[ ,]");
                var check = pattern.split(senderAddress.replace(",", " ").replaceAll("( )+", " "));
                for (var s : check) {
                    var checkNameS = b.literal("%" + s + "%");
                    predicate = b.and(predicate, b.like(b.lower(root.get(TemplateForCargo_.senderAddress)), b.lower(checkNameS)));
                }
            }

            if (recipientAddress != null) {
                var pattern = Pattern.compile("[ ,]");
                var check = pattern.split(recipientAddress.replace(",", " ").replaceAll("( )+", " "));
                for (var s : check) {
                    var checkNameS = b.literal("%" + s + "%");
                    predicate = b.and(predicate, b.like(b.lower(root.get(TemplateForCargo_.recipientAddress)), b.lower(checkNameS)));
                }
            }

            if (creationTimeFrom != null || creationTimeTo != null) {
                var checkTimeFrom = Optional.ofNullable(creationTimeFrom).orElse(LocalDateTime.now(ZoneOffset.UTC));
                var checkTimeTo = Optional.ofNullable(creationTimeTo).orElse(LocalDateTime.now(ZoneOffset.UTC));
                predicate = b.and(predicate, b.between(root.get(TemplateForCargo_.creationTime), checkTimeFrom, checkTimeTo));
            }

            if (organizationId != null) {
                predicate = b.and(predicate, b.equal(root.get(TemplateForCargo_.organizationId), organizationId));
            }

            if (authorDepartment != null) {
                var departments = departmentService.findAllChildren(authorDepartment);
                if (!departments.isEmpty()) {
                    predicate = b.and(predicate,
                            root.join(TemplateForCargo_.author).join(Employee_.DEPARTMENT).get(Department_.ID)
                                    .in(departments));
                }

            }
            return predicate;
        };
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, TemplateForCargo_.CREATION_TIME));
        var templates = templateForCargoRepository.findAll(templateSpecification, pageable);

        if (isEmpty(templates.getContent())) {
            return new PageImpl<>(new ArrayList<>(),
                    templates.getPageable(),
                    templates.getTotalElements());
        }

        return new PageImpl<>(templates.stream().map(templateMapper::entityToTemplateForOtoDto).toList(),
                templates.getPageable(),
                templates.getTotalElements());
    }

    /**
     * Преобразовать заявку для инженера ОТО.
     *
     * @param request исходная заявка.
     * @return данные для инженера.
     */
    private OtoEngineerCargoRequestDetailDTO toOtoCargoDetails(Request request) {
        var requestDetailDto = mapper.toOtoCargoDetails(request);

        try {
            // trip can be not null, but routelist not exists
            Optional.ofNullable(request.getRoutelist())
                    .map(Routelist::getContractor)
                    .ifPresent(requestDetailDto::setContractor);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            log.warn("Не удалось получить информацию о маршруте {}. для заявки {}", request.getTripId(), request.getHumanReadableId(), e);
        }

        if (StringUtils.isEmpty(requestDetailDto.getCargoTypes())) {
            requestDetailDto.setCargoTypes("");
        }

        if (StringUtils.isEmpty(requestDetailDto.getCommonComment())) {
            requestDetailDto.setCommonComment("");
        }

        var waypoints = request.getWaypoints();
        if (waypoints == null) {
            return requestDetailDto;
        }

        boolean setAddress = false;

        try {
            setAddress = waypoints.size() > 1 && waypoints.getFirst() != null && waypoints.getFirst().getAddress() != null
                    && waypoints.getLast() != null && waypoints.getLast().getAddress() != null;
        } catch (jakarta.persistence.EntityNotFoundException e) {
            log.warn("Не удалось получить информацию об адресах. для заявки {}", request.getHumanReadableId(), e);
        }

        if (setAddress) {
            var senderAddress = getClearedAddressString(waypoints.getFirst().getAddress().getAddressString());
            var recipientAddress = getClearedAddressString(waypoints.getLast().getAddress().getAddressString());
            requestDetailDto.setSenderAddress(senderAddress);
            requestDetailDto.setRecipientAddress(recipientAddress);
        } else {
            log.info("Данные по заявке {} неполные (отсутствует информация по адресам доставки/разгрузки)", request.getHumanReadableId());
        }

        if (!StringUtils.isEmpty(requestDetailDto.getSenderOrganization())) {
            requestDetailDto.setSenderOrganization(request.getWaypoints().getFirst().getOrganization());
        }
        if (!StringUtils.isEmpty(requestDetailDto.getRecipientOrganization())) {
            requestDetailDto.setRecipientOrganization(request.getWaypoints().getLast().getOrganization());
        }

        requestDetailDto.setExpected(new ExpectedCargoDataDTO(waypoints.size()));

        return requestDetailDto;
    }

    /**
     * Подготовка параметров поиска согласно роли: поиск - по организации для роли Инженера КК, - по группе организации для ролей ограниченного доступа
     * - по произвольной организации для ролей полного доступа
     *
     * @param requestedOrganizationId id организации
     * @param authentication          инфо об авторизации
     * @throws AccessOrganizationForbiddenException - поиск по запрашиваемой группе не разрешел
     */
    private OrganizationAndOrganizationGroup checkOrganization(JwtAuthenticationToken authentication, UUID requestedOrganizationId)
            throws AccessOrganizationForbiddenException {
        OrganizationAndOrganizationGroup searchDto = OrganizationAndOrganizationGroup.builder().organizationId(requestedOrganizationId).build();

        var searchInitiatorEmployee = employeeService.getAuthenticatedEmployee(authentication);
        var initiatorEmployeeOrganization = organizationService.findById(searchInitiatorEmployee.getDepartment().getOrganizationId())
                .orElseThrow(() -> new AccessOrganizationForbiddenException(requestedOrganizationId));

        if (requestedOrganizationId.equals(initiatorEmployeeOrganization.getId())
                || CheckOrganizationAccessUtils.hasFullAccessRoles(authentication.getAuthorities())) {
            return searchDto;
        }

        if (!CheckOrganizationAccessUtils.hasRestrictedAccessRoles(authentication.getAuthorities())) {
            throw new AccessOrganizationForbiddenException(requestedOrganizationId);
        }

        var requestOrg = organizationService.findById(requestedOrganizationId).orElseThrow(() -> new RuntimeException("Запрошенная организация " +
                "не найдена"));
        var group = requestOrg.getOrganizationGroup();

        if (initiatorEmployeeOrganization.getOrganizationGroup() == null || group == null ||
                initiatorEmployeeOrganization.getOrganizationGroup() != group) {
            throw new AccessOrganizationForbiddenException(requestedOrganizationId);
        }

        return searchDto;
    }

    private String getClearedAddressString(String addressString) {
        if (addressString == null) {
            return null;
        }
        var patternCitySet = Set.of("Москва", "Санкт-Петербург", "Севастополь");
        for (String city : patternCitySet) {
            if (addressString.contains(city)) {
                var addressWords = List.of(addressString.split(","));
                var newWordSet = addressWords.stream().map(String::trim)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                return String.join(",", newWordSet);
            }
        }
        return addressString;
    }
}
