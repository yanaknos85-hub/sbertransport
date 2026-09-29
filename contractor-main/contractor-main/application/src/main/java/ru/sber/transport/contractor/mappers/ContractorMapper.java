package ru.sber.transport.contractor.mappers;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValueMappingStrategy;
import ru.sber.transport.contractor.database.model.Contractor;
import ru.sber.transport.contractor.dto.ContractorDTO;
import ru.sber.transport.contractor.dto.NewContractorDTO;
import ru.sber.transport.contractor.dto.files.ContractorFile;
import ru.sber.transport.contractor.dto.internal.InternalContractorRequestDto;
import ru.sber.transport.contractor.dto.internal.LinkRequestDTO;
import ru.sber.transport.contractor.messages.ContractorMessage;

import java.util.List;
import java.util.UUID;

import static ru.sber.transport.contractor.database.model.ServiceType.INTERNAL_AUTO_PARK;

/**
 * Маппер договоров.
 */
@Mapper(uses = {EmailIntegrationParamsMapper.class, BooleanMapper.class, JsonIntegrationParamsMapper.class})
public interface ContractorMapper {

    /**
     * Конвертация в объект обмена данных.
     *
     * @param source источник.
     * @return результат.
     */
    @Mapping(target = "humanReadableId", source = "digitId")
    ContractorDTO toDto(Contractor source);

    /**
     * Конвертация в сокращенный объект обмена данных.
     *
     * @param source источник.
     * @return результат.
     */
    @Mapping(target = "msrn", ignore = true)
    @Mapping(target = "tin", ignore = true)
    @Mapping(target = "contactPersonFirstName", ignore = true)
    @Mapping(target = "contactPersonLastName", ignore = true)
    @Mapping(target = "contactPersonPatronymic", ignore = true)
    @Mapping(target = "contactPersonPhone", ignore = true)
    @Mapping(target = "contactPersonEmail", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "img", ignore = true)
    @Mapping(target = "integrationParams", ignore = true)
    @Mapping(target = "jsonIntegrationParams", ignore = true)
    @Mapping(target = "digitId", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "integrationType", ignore = true)
    @Mapping(target = "mainDispatcherId", ignore = true)
    @Mapping(target = "vehicleCountNorm", ignore = true)
    @Named("short")
    ContractorDTO toShortDto(Contractor source);

    /**
     * Обновление контрагента.
     *
     * @param target целевой объект.
     * @param source исходный объект.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "digitId", ignore = true)
    @Mapping(target = "active", ignore = true)
    void update(@MappingTarget Contractor target, NewContractorDTO source);

    /**
     * Преобразование в объект файла.
     *
     * @param source источник.
     * @return результат.
     */
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<ContractorFile> toFile(List<ContractorDTO> source);

    /**
     * Преобразование в объект файла.
     *
     * @param source источник.
     * @return результат.
     */
    @Mapping(target = "contractorRusName", source = "integrationParams.contractorRusName")
    @Mapping(target = "integrationEmail", source = "integrationParams.email")
    @Mapping(target = "url", source = "jsonIntegrationParams.url")
    @Mapping(target = "login", source = "jsonIntegrationParams.login")
    @Mapping(target = "password", ignore = true)
    ContractorFile toFile(ContractorDTO source);

    /**
     * Преобразование из объекта файла.
     *
     * @param source источник.
     * @return результат.
     */
    @Mapping(target = "jsonIntegrationParams.url", source = "url")
    @Mapping(target = "jsonIntegrationParams.login", source = "login")
    @Mapping(target = "jsonIntegrationParams.password", source = "password")
    NewContractorDTO fromFile(ContractorFile source);

    /**
     * Преобразование в сообщение брокера.
     *
     * @param contractor источник.
     * @return результат.
     */
    @Mapping(target = "contractorName", source = "integrationParams.contractorName")
    @Mapping(target = "contractorRusName", source = "integrationParams.contractorRusName")
    @Mapping(target = "integrationEmail", source = "integrationParams.email")
    @Mapping(target = "url", source = "jsonIntegrationParams.url")
    @Mapping(target = "login", source = "jsonIntegrationParams.login")
    @Mapping(target = "password", source = "jsonIntegrationParams.password")
    @Mapping(target = "deleted", source = "active", qualifiedByName = BooleanMapper.NEGATE)
    ContractorMessage toMessage(Contractor contractor);

    @Mapping(target = "contractorName", source = "integrationParams.contractorName")
    @Mapping(target = "contractorRusName", source = "integrationParams.contractorRusName")
    @Mapping(target = "integrationEmail", source = "integrationParams.email")
    @Mapping(target = "url", source = "jsonIntegrationParams.url")
    @Mapping(target = "login", source = "jsonIntegrationParams.login")
    @Mapping(target = "password", source = "jsonIntegrationParams.password")
    @Mapping(target = "deleted", source = "active", qualifiedByName = BooleanMapper.NEGATE)
    ru.sber.transport.contractor.messages.avro.ContractorMessage toAvroMessage(Contractor contractor);

    default InternalContractorRequestDto map(Contractor contractor, String password, UUID oauthId) {
        var employee = InternalContractorRequestDto.NewDispatcherDto.builder()
                .firstName(contractor.getContactPersonFirstName())
                .lastName(contractor.getContactPersonLastName())
                .patronymic(contractor.getContactPersonPatronymic())
                .phone(contractor.getContactPersonPhone())
                .email(contractor.getContactPersonEmail())
                .oauthId(oauthId).build();

        return InternalContractorRequestDto.builder()
                .name(contractor.getName())
                .tin(contractor.getTin())
                .msrn(contractor.getMsrn())
                .mainDispatcher(employee)
                .technicalAccountOwner(contractor.getContactPersonInfo())
                .technicalAccountOwnerEmail(contractor.getContactPersonEmail())
                .integrationType(contractor.getIntegrationType().name())
                .technicalAccountLogin(contractor.getJsonIntegrationParams().getLogin())
                .technicalAccountPassword(password)
                .vehicleCountNorm(contractor.getVehicleCountNorm())
                .isInternal(contractor.getServiceType() == INTERNAL_AUTO_PARK)
                .build();
    }

    default LinkRequestDTO mapToLink(Contractor contractor, String password) {
        return new LinkRequestDTO(contractor.getMsrn(), contractor.getTin(), contractor.getContactPersonEmail(),
                contractor.getJsonIntegrationParams().getLogin(), password);
    }

}
