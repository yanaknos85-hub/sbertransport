package ru.sberbank.ditsib.transport.tariff.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.tariff.client.ContractorClient;
import ru.sberbank.ditsib.transport.tariff.controller.TransportController;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.Field;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.SortDirection;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.TransportDTO;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.TransportPageDTO;
import ru.sberbank.ditsib.transport.tariff.mappers.TransportMapper;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TransportControllerImpl implements TransportController {
    
    private final ContractorClient contractorClient;
    
    private final TransportMapper mapper;
    
    private final TariffService tariffService;
    
    
    @Override
    public TransportPageDTO search(
            Integer page,
            Integer size,
            String search,
            LocalDate tariffStartDate,
            LocalDate tariffEndDate,
            UUID contractorId,
            UUID organizationId,
            Set<UUID> regionIds,
            SortDirection direction,
            String field,
            String token
                                  ) {
        
        var response =
                contractorClient.getTransports(page, size, null, null, search, List.of(Field.ID, Field.BRAND, Field.MODEL, Field.STATE_NUMBER),
                                               direction, field,
                                               contractorId, token);
        
        var transportIds = response.getContent().stream().map(TransportDTO::getId).collect(Collectors.toSet());
        
        var conflictTariffMap = tariffService.findConflictTariff(tariffStartDate, tariffEndDate, contractorId, organizationId, regionIds,
                                                                 transportIds);
        
        var enrichTransport = response.getContent().stream().map(t ->
                                                                 {
                                                                     var newDTO = mapper.toDto(t);
                                                                     var conflictTariff =
                                                                             Optional.ofNullable(conflictTariffMap.get(t.getId()))
                                                                                     .flatMap(l -> l.stream().findFirst());
                                                                     conflictTariff.ifPresent(tariff -> {
                                                                         newDTO.setTariffId(tariff.getId());
                                                                         newDTO.setTariffHumanReadableId(tariff.getHumanReadableId());
                                                                     });
                                                                     return newDTO;
                                                                 }).toList();
        response.setContent(enrichTransport);
        return response;
    }
}
