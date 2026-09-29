package ru.sberbank.ditsib.transport.tariff.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.dto.ContractDTO;
import ru.sberbank.ditsib.transport.tariff.dto.ContractForKafkaMessage;
import ru.sberbank.ditsib.transport.tariff.dto.GetContractDTO;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper
public interface ContractMapper {
    
    @Mapping(source = "deleted", target = "deleted")
    @Mapping(source = "contract.organizations", target = "organizations", qualifiedByName = "organizationsToDto")
    ContractForKafkaMessage toMessage(Contract contract, boolean deleted);

    @Mapping(target = "active", constant = "true")
    Contract toContract(ContractDTO contractDTO);

    @Mapping(target = "contractorName", ignore = true)
    @Mapping(target = "organizationIds", ignore = true)
    @Mapping(target = "organizationNames", ignore = true)
    GetContractDTO toGetContractDTO(Contract contract);

    @Named("organizationsToDto")
    default Set<UUID> organizationsToDto(Set<Organization> organizations) {
        return organizations.stream().map(Organization::getId).collect(Collectors.toSet());
    }
}
