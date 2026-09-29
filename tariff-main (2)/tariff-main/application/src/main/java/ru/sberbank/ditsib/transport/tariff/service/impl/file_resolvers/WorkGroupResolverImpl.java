package ru.sberbank.ditsib.transport.tariff.service.impl.file_resolvers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sberbank.ditsib.transport.tariff.database.dao.ContractorRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.TaxiTariffRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.Contractor;
import ru.sberbank.ditsib.transport.tariff.database.model.TaxiTariff;
import ru.sberbank.ditsib.transport.tariff.dto.files.WorkGroupFileDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация разбора договоров.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class WorkGroupResolverImpl implements GeoZoneResolver, DataExporter<WorkGroupFileDto> {
    
    private final TaxiTariffRepository taxiTariffRepository;
    
    private final ContractorRepository contractorRepository;
    
    @NotNull
    @Override
    public List<WorkGroupFileDto> exportData(@NotNull Map<String, ?> parameters, @NotNull JwtAuthenticationToken authentication) {
        final var result = new ArrayList<WorkGroupFileDto>();
        final var uniqueWGTariffs =
                taxiTariffRepository.findAll().stream().filter(tariff -> tariff.getWorkGroup() != null).collect(
                        Collectors.toMap(TaxiTariff::getWorkGroup, elt -> elt,(tariff1,tariff2)->tariff1)).values();
        final var contractorMap = contractorRepository
                .findAllById(uniqueWGTariffs.stream().map(elt -> elt.getContract().getContractorId()).collect(
                        Collectors.toSet())).stream().collect(Collectors.toMap(Contractor::getId, elt -> elt));
        
        for (final var item : uniqueWGTariffs) {

            final var contractor = contractorMap.get(item.getContract().getContractorId());
            if (contractor == null) {
                log.error("Contractor for id {} was null for contract {}, tariff id : {}",
                          item.getContract().getContractorId(), item.getContract().getId(), item.getId());
            }
            final var resItem = WorkGroupFileDto.builder()
                                          .name(item.getWorkGroup())
                                          .organizationName(item.getOrganization().getName())
                                          .geoZoneId(item.getRegionId().toString())
                                          .geoZoneName(item.getRegion())
                                          .contractorName(Optional.ofNullable(contractor).map(Contractor::getName).orElse(null))
                                          .integrationEmail(Optional.ofNullable(contractor).map(Contractor::getIntegrationEmail).orElse(null))
                                          .contractNumber(item.getContract().getContractNumber())
                                          .build();
            result.add(resItem);
        }
        return result;
    }
}
