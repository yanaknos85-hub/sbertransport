package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.*;
import ru.sber.transport.magenta.model.EmployeeDTO;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingJoinRequest;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingJoinRequestText;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.ContractorAndJoinStatus;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.carsharing.*;

import java.util.List;
import java.util.Set;

/**
 * Маппер заявок на подключение к корп.каршерингу
 */
@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CarsharingJoinRequestMapper {
    
    ContractorDTO contractorToDto(Contractor contractor);
    Contractor dtoToContractor(ContractorDTO contractor);
    
    EmployeeDTO employeeToDto(Employee employee);
    Employee dtoToEmployee(EmployeeDTO dto);
    
    GetCarsharingJoinRequestTextDTO joinRequestTextToDto(CarsharingJoinRequestText text);
    CarsharingJoinRequestText updateJoinRequestTextToDto(UpdateCarsharingJoinRequestTextDTO textUpdateDto);
    
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "beforeFioSecondPart", ignore = true)
    CarsharingJoinRequestText updateJoinRequestByTextDto(@MappingTarget CarsharingJoinRequestText text,
                                                         UpdateCarsharingJoinRequestTextDTO textUpdateDto);
    
    ContractorAndJoinStatus dtoToContractorStatus(ContractorAndJoinStatusDTO dto);
    GetContractorAndJoinStatusDTO contractorStatusToDto(ContractorAndJoinStatus contractorStatus);
    ContractorAndJoinStatus processedDtoToContractorStatus(ProcessedContractorAndJoinStatusDTO dto);
    
    @Mapping(target = "employeeChoice", ignore = true)
    @Mapping(target = "id", ignore = true)
    ContractorAndJoinStatus updateContractorStatusByProcessedDto(
            ProcessedContractorAndJoinStatusDTO dto, @MappingTarget ContractorAndJoinStatus contractorStatus);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    Set<ContractorAndJoinStatusDTO> contractorStatusesToDtoSet(Set<ContractorAndJoinStatus> contractorStatuses);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    Set<ContractorAndJoinStatus> dtoSetToContractorStatuses(Set<ContractorAndJoinStatusDTO> dtoSet);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    Set<ContractorAndJoinStatus> processedDtoSetToContractorStatuses(Set<ProcessedContractorAndJoinStatusDTO> dtoSet);
    
    CarsharingJoinRequest updateDtoToJoinRequest(UpdateCarsharingJoinRequestDTO dto);
    CarsharingJoinRequest newDtoToJoinRequest(NewCarsharingJoinRequestDTO dto);
    
    @Named("toGetShortDto")
    GetCarsharingJoinRequestShortDTO joinRequestToShortDto(CarsharingJoinRequest joinRequest);
    @Named("toGetDto")
    GetCarsharingJoinRequestDTO joinRequestToDto(CarsharingJoinRequest joinRequest);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL, qualifiedByName = "toGetShortDto")
    List<GetCarsharingJoinRequestShortDTO> getJoinRequestsToShortDtos(List<CarsharingJoinRequest> joinRequests);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    Set<ContractorAndJoinStatusDTO> getJoinStatusDtosToDtoSet(Set<GetContractorAndJoinStatusDTO> joinStatusDtos);
}
