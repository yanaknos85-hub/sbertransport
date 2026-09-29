package ru.sber.transport.contractor.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ru.sber.ditsib.encription.PasswordEncryption;
import ru.sber.transport.contractor.config.IntegrationConfig;
import ru.sber.transport.contractor.database.model.*;
import ru.sber.transport.contractor.dto.*;
import ru.sber.transport.contractor.dto.enums.ContractorProjection;
import ru.sber.transport.contractor.dto.internal.StaffDto;
import ru.sber.transport.contractor.dto.search.ContractorSearchDTO;
import ru.sber.transport.contractor.dto.search.TransportSearchDTO;
import ru.sber.transport.contractor.exceptions.ClientFeignException;
import ru.sber.transport.contractor.exceptions.ConflictException;
import ru.sber.transport.contractor.feign.InternalClient;
import ru.sber.transport.contractor.mappers.ContractorMapper;
import ru.sber.transport.contractor.mappers.JsonIntegrationParamsMapper;
import ru.sber.transport.contractor.messaging.senders.ContractorSender;
import ru.sber.transport.contractor.messaging.senders.EmployeeRoleSender;
import ru.sber.transport.contractor.serde.OffsetDateTimeSerializer;
import ru.sber.transport.contractor.service.ContractorControllerService;
import ru.sber.transport.contractor.service.ContractorService;
import ru.sber.transport.contractor.service.IntegrationServiceFactory;
import ru.sber.transport.contractor.service.grpc.ContractGrpcService;
import ru.sber.transport.contractor.util.LoginPasswordGeneratorUtils;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.io.Serializable;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static ru.sber.transport.contractor.database.model.ServiceType.AUTOSERVICE;

/**
 * Implementation of controller service for working with contractors.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContractorControllerServiceImpl implements ContractorControllerService {

    private final ContractorService contractorService;
    private final ContractorSender contractorSender;
    private final EmployeeRoleSender employeeRoleSender;
    private final ContractorMapper mapper;
    private final ContractGrpcService contractGrpcService;
    private final JsonIntegrationParamsMapper paramsMapper;
    private final RestTemplate restTemplate;
    private final PasswordEncryption passwordEncryption;
    private final IntegrationServiceFactory integrationServiceFactory;
    private final IntegrationConfig integrationConfig;
    private final ObjectMapper objectMapper;
    private final InternalClient internalClient;

    @Value("${internal.autopark.main-dispatcher.role:ROLE_MAIN_DISPATCHER_CONTRACTOR}")
    private String dispatcherRole;

    @Override
    @Transactional
    public ContractorDTO add(NewContractorDTO contractor, UUID orgId, String authorization) {
        checkContractorExists(contractor.name(), contractor.tin(), orgId, null, contractor.contactPersonPhone(), contractor.contactPersonEmail());
        if (ServiceType.INTERNAL_AUTO_PARK.equals(contractor.serviceType())) {
            checkInternalAutoParkExists(orgId, null);
        }
        var entity = contractorService.findByNameAndTin(contractor.name(), contractor.tin()).orElseGet(Contractor::new);

        mapper.update(entity, contractor);
        entity.getOrganizations().add(orgId);
        entity.setIntegrationType(IntegrationType.JSON_API_1_0);
        entity = contractorService.save(entity);

        if (entity.getContractorType() != ContractorType.OFFLINE) {
            var params = Optional.ofNullable(entity.getJsonIntegrationParams()).orElseGet(JsonIntegrationParams::new);
            var password = LoginPasswordGeneratorUtils.generatePassword(16);
            if (Objects.requireNonNull(entity.getContractorType()) == ContractorType.API) {
                params.setPassword(contractor.jsonIntegrationParams().password());
            } else {
                var login = LoginPasswordGeneratorUtils
                        .generateLogin(entity.getContactPersonLastName(),
                                entity.getContactPersonFirstName(),
                                entity.getContactPersonPatronymic());
                params.setLogin(login);
                params.setPassword(password);
                params.setUrl(integrationConfig.getUrlByContractorTypeAndServiceType(entity.getContractorType(), entity.getServiceType()));
                entity.setJsonIntegrationParams(params);
                entity = contractorService.save(entity);
            }
            paramsMapper.encryption(entity.getJsonIntegrationParams(), params.getPassword());

            try {
                var externalId = integrationServiceFactory.getService(contractor.contractorType())
                        .add(entity, password, authorization,
                                ServiceType.INTERNAL_AUTO_PARK.equals(contractor.serviceType()) ? contractor.employeeId() : null);
                entity.setExternalId(externalId);
                entity = contractorService.save(entity);
            } catch (FeignException.FeignClientException e) {
                throw new ClientFeignException("Произошел конфликт данных во время создания контрагента во внешнем сервисе.", e);
            }
        }

        contractorSender.send(entity);
        if (entity.getServiceType() == ServiceType.INTERNAL_AUTO_PARK) {
            employeeRoleSender.send(contractor.employeeId(), dispatcherRole);
        }
        return createResponse(entity);
    }

    @Override
    @Transactional
    public void edit(UUID id, NewContractorDTO contractor, UUID orgId, String authorization) {
        var entity = contractorService.get(id).orElseThrow(() -> new EntityNotFoundException(Contractor.class, id));
        checkContractorExists(contractor.name(), contractor.tin(), orgId, entity, contractor.contactPersonPhone(), contractor.contactPersonEmail());
        if (ServiceType.INTERNAL_AUTO_PARK.equals(contractor.serviceType())) {
            checkInternalAutoParkExists(orgId, entity);

        }
        if (ContractorType.API.equals(contractor.contractorType())) {
            var params = Optional.ofNullable(entity.getJsonIntegrationParams()).orElseGet(JsonIntegrationParams::new);
            paramsMapper.encryption(params, contractor.jsonIntegrationParams().password());
            entity.setJsonIntegrationParams(params);
        }
        entity.getOrganizations().add(orgId);

        mapper.update(entity, contractor);

        var saved = contractorService.save(entity);
        try {
            integrationServiceFactory.getService(saved.getContractorType()).edit(saved.getExternalId(), saved, authorization);
        } catch (FeignException.FeignClientException e) {
            throw new ClientFeignException("Произошла ошибка во время изменения данных контрагента во внешнем сервисе.", e);
        }
        contractorSender.send(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id, String authorization) {
        var entity = contractorService.get(id).orElseThrow(() -> new EntityNotFoundException(Contractor.class, id));
        if (entity.getServiceType() == AUTOSERVICE) {
            contractGrpcService.haveActiveContractsByContractorId(id);
        }

        entity.setEmployeeCount(0);
        var deleted = contractorService.delete(entity);
        try {
            integrationServiceFactory.getService(entity.getContractorType()).delete(deleted.getExternalId(), authorization);
        } catch (FeignException.FeignClientException e) {
            throw new ClientFeignException("Произошел конфликт данных во время удаления контрагента во внешнем сервисе.", e);
        }
        contractorSender.send(deleted);
    }

    @Override
    @Transactional
    public ContractorDTO get(UUID id) {
        return contractorService.get(id).map(this::createResponse).orElseThrow(() -> new EntityNotFoundException(Contractor.class, id));
    }

    @SuppressWarnings("java:S3958")
    @Override
    @Transactional
    public Iterable<ContractorDTO> get(boolean paged,
                                       ContractorSearchDTO contractorSearchDTO,
                                       ContractorProjection projection, UUID organizationId) {
        var result = contractorService.getAll(paged, contractorSearchDTO, organizationId);
        if (ContractorProjection.SELECT.equals(projection) && result instanceof Collection<Contractor> collection) {
            return collection.parallelStream().map(this::createShortResponse).toList();
        } else if (!paged && result instanceof Collection<Contractor> collection) {
            return collection.parallelStream().map(this::createResponse).toList();
        } else if (result instanceof Page<Contractor> pagedResult) {
            return pagedResult.map(this::createResponse);
        }
        throw new IllegalArgumentException("Projection '%s' is not compatible with result type '%s'".formatted(projection, result.getClass().getName()));
    }

    @SuppressWarnings("java:S3958")
    @Override
    @Transactional
    public Collection<ContractorDTO> getAll() {
        return contractorService.getAll().stream().map(this::createResponse).toList();
    }

    @Override
    @Transactional
    public void editAutoassignFlag(UUID contractorId, Boolean autoassign) {
        Contractor contractor = contractorService.get(contractorId)
                .orElseThrow(() -> new EntityNotFoundException(Contractor.class, contractorId));

        if (autoassign == null) {
            return;
        }
        contractor.setAutoassign(autoassign);
        var saved = contractorService.save(contractor);
        contractorSender.send(saved);
    }

    @Override
    public Object getAllFreeTransport(UUID contractorId, TransportSearchDTO transportSearchDTO) {
        var contractor = getContractor(contractorId);
        var httpEntity = createEntity(contractor);
        var url = applyQuery(contractor.getJsonIntegrationParams().getUrl() + "/transport", transportSearchDTO);
        var response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<>() {
                });
        return response.getBody();
    }

    @Override
    public Object getTransport(UUID contractorId, UUID vehicleId, TransportSearchDTO transportSearchDTO) {
        var contractor = getContractor(contractorId);
        var httpEntity = createEntity(contractor);
        var url = applyQuery(contractor.getJsonIntegrationParams().getUrl() + "/transport/" + vehicleId + "/trips/", transportSearchDTO);
        var response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<>() {
                });
        return response.getBody();
    }

    @Override
    @Transactional
    public void editContactPerson(UUID contractorId, Serializable data, String authorizationHeader) {
        var staff = objectMapper.convertValue(data, StaffDto.class);
        var contractorOpt = contractorService.get(contractorId);
        if (contractorOpt.isPresent()) {
            var contractor = contractorOpt.get();
            contractor.setContactPersonFirstName(staff.getFirstName());
            contractor.setContactPersonLastName(staff.getLastName());
            contractor.setContactPersonPatronymic(staff.getPatronymic());
            contractor.setContactPersonEmail(staff.getEmail());
            contractor.setContactPersonPhone(staff.getPhone());
            contractor = contractorService.save(contractor);
            if (contractor.getServiceType().equals(ServiceType.INTERNAL_AUTO_PARK)) {
                integrationServiceFactory.getService(contractor.getContractorType()).changeContactPerson(contractor, authorizationHeader, staff.getExternalId());
            }
        }
    }

    @Override
    @Transactional
    public void editVehicleCountNorm(UUID contractorId, PatchData data, String authorizationHeader) {
        var contractorOpt = contractorService.get(contractorId);
        if (contractorOpt.isPresent()) {
            var contractor = contractorOpt.get();
            if (contractor.getServiceType().equals(ServiceType.INTERNAL_AUTO_PARK)) {
                contractor.setVehicleCountNorm(Integer.valueOf(String.valueOf(data.value())));
                contractorService.save(contractor);
                var baseUrl = URI.create(integrationConfig.getClientUrl(contractor.getContractorType()));
                try {
                    internalClient.patchContractor(baseUrl, contractor.getExternalId(), List.of(data), authorizationHeader);
                } catch (FeignException.FeignClientException e) {
                    throw new ClientFeignException("Произошла ошибка во время изменения нормы по транспорту во внешнем сервисе.", e);
                }
            }
        }
    }

    private Contractor getContractor(UUID contractorId) {
        var contractor = contractorService.get(contractorId).orElseThrow(() -> new EntityNotFoundException(Contractor.class, contractorId));
        if (contractor.getJsonIntegrationParams() == null) {
            throw new ConflictException("Невозможно запросить данные по транспорту, интеграционные данные контрагента отсутствуют");
        }
        return contractor;
    }

    private String authorize(Contractor contractor) {
        var httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization",
                "Basic " + Base64.getEncoder().encodeToString(
                        (contractor.getJsonIntegrationParams().getLogin()
                                + ":"
                                + passwordEncryption.decode(contractor.getJsonIntegrationParams().getPassword())
                        ).getBytes(StandardCharsets.UTF_8)));
        HttpEntity<String> httpEntity = new HttpEntity<>(null, httpHeaders);
        var authResponse = restTemplate.postForEntity(contractor.getJsonIntegrationParams().getUrl() + "/auth", httpEntity, AuthResponseDTO.class);
        return Objects.requireNonNull(authResponse.getBody()).getToken();
    }

    private HttpEntity<Object> createEntity(Contractor contractor) {
        var httpHeaders = new HttpHeaders();
        httpHeaders.clear();
        httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        httpHeaders.add("Authorization", "Bearer " + authorize(contractor));
        return new HttpEntity<>(null, httpHeaders);
    }

    private String applyQuery(String baseUrl, TransportSearchDTO transportSearchDTO) {
        if (transportSearchDTO == null) {
            return baseUrl + "/";
        }
        String timeZone = "Z";
        if (transportSearchDTO.getStartDate() != null) {
            timeZone = transportSearchDTO.getStartDate().getOffset().getId();
            transportSearchDTO.setStartDate(transportSearchDTO.getStartDate().withOffsetSameInstant(ZoneOffset.UTC));
        }
        if (transportSearchDTO.getEndDate() != null) {
            timeZone = transportSearchDTO.getEndDate().getOffset().getId();
            transportSearchDTO.setEndDate(transportSearchDTO.getEndDate().withOffsetSameInstant(ZoneOffset.UTC));
        }
        var stringBuilder = new StringBuilder(baseUrl + "/?");
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SimpleModule().addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer()));
        var result = transportSearchDTO.getResult();
        transportSearchDTO.setResult(null);
        var queryData = objectMapper.convertValue(transportSearchDTO, new TypeReference<Map<String, String>>() {
        });
        queryData.forEach((key, value) -> {
            if (value != null) {
                stringBuilder.append(key).append("=").append(value).append("&");
            }
        });
        if (result != null && !result.isEmpty()) {
            result.forEach(field -> {
                stringBuilder.append("result").append("=").append(field.name()).append("&");
            });
        }
        stringBuilder.append("timeZone").append("=").append(URLEncoder.encode(timeZone, StandardCharsets.UTF_8));
        return stringBuilder.toString();
    }

    /**
     * Create response by entity.
     *
     * @param entity source entity.
     * @return response.
     */
    private ContractorDTO createResponse(Contractor entity) {
        return mapper.toDto(entity);
    }

    /**
     * Create short response by entity.
     *
     * @param entity source entity.
     * @return response.
     */
    private ContractorDTO createShortResponse(Contractor entity) {
        return mapper.toShortDto(entity);
    }

    private void checkContractorExists(String name, String tin, UUID organizationId, Contractor contractor, String phone, String email) {
        var uuid = contractor == null
                ? contractorService.isContractorExists(name, tin, organizationId)
                : contractorService.isContractorExists(name, tin, organizationId, contractor);
        if (uuid.isPresent()) {
            throw new DuplicateDataException(Contractor.class, Map.of("name", name, "tin", tin, "organizationId", organizationId));
        }

        var exists = contractorService.findByPhoneAndActive(phone, true);
        if (exists.isPresent() && ((contractor == null) || !exists.get().getId().equals(contractor.getId()))) {
            throw new DuplicateDataException(Contractor.class, Map.of("id", exists.get().getId(), "phone", phone));
        }

        exists = contractorService.findByEmailAndActive(email, true);
        if (exists.isPresent() && ((contractor == null) || !exists.get().getId().equals(contractor.getId()))) {
            throw new DuplicateDataException(Contractor.class, Map.of("id", exists.get().getId(), "email", email));
        }
    }

    private void checkInternalAutoParkExists(UUID organizationId, Contractor contractor) {
        var exists = contractorService.getInternalAutoPark(organizationId, contractor);
        if (exists.isPresent()) {
            throw new ConflictException("Для данной организации уже существует внутренний автопарк %s, ИНН - %s, ОГРН - %s"
                    .formatted(exists.get().getName(), exists.get().getTin(), exists.get().getMsrn()));
        }
    }
}
