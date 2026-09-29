package ru.sberbank.ditsib.transport.request.service.approvals.settings.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.dao.approvals.settings.OtherTrTypesApprovalsSettingsRepository;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.OtherTrTypesApprovalsSettings;
import ru.sberbank.ditsib.transport.request.service.approvals.settings.OtherTrTypesApprovalsSettingsService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtherTrTypesApprovalsSettingsServiceImpl implements OtherTrTypesApprovalsSettingsService {
    
    private final OtherTrTypesApprovalsSettingsRepository otherSettingsRepository;
    
    @Override
    public OtherTrTypesApprovalsSettings save(OtherTrTypesApprovalsSettings settings) {
        return otherSettingsRepository.save(settings);
    }
    
    @Override
    public Optional<OtherTrTypesApprovalsSettings> getOptional(UUID organizationId, TransportTypeEnum transportType) {
        return otherSettingsRepository.findByOrganizationIdAndTransportType(organizationId, transportType);
    }
    
    @Override
    public OtherTrTypesApprovalsSettings get(UUID organizationId, TransportTypeEnum transportType) {
        return getOptional(organizationId, transportType).orElseThrow(
                () -> new EntityNotFoundException(OtherTrTypesApprovalsSettings.class,
                                                  Map.of("organizationId", organizationId, "transportType", transportType)));
    }
    
    @Override
    public void delete(UUID organizationId, TransportTypeEnum transportType) {
        OtherTrTypesApprovalsSettings settings = get(organizationId, transportType);
        otherSettingsRepository.delete(settings);
    }
}
