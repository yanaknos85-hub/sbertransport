package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.messaging.message.ApprovalsSettingsMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.OtherTrTypesApprovalsSettingsMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.PublicApprovalsSettingsMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.TaxiApprovalsSettingsMessage;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.OtherTrTypesApprovalsSettings;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.PublicTrApprovalsSettings;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.TaxiApprovalsSettings;
import ru.sberbank.ditsib.transport.request.mappers.ApprovalsSettingsMapper;
import ru.sberbank.ditsib.transport.request.service.approvals.settings.OtherTrTypesApprovalsSettingsService;
import ru.sberbank.ditsib.transport.request.service.approvals.settings.PublicApprovalsSettingsService;
import ru.sberbank.ditsib.transport.request.service.approvals.settings.TaxiApprovalsSettingsService;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.PUBLIC;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.TAXI;

@RequiredArgsConstructor
@Slf4j
public class ApprovalsSettingsListenerImpl implements Consumer<Message<Map<String, Object>>> {
    
    private final TaxiApprovalsSettingsService taxiSettingsService;
    private final PublicApprovalsSettingsService publicSettingsService;
    private final OtherTrTypesApprovalsSettingsService otherSettingsService;
    private final ApprovalsSettingsMapper mapper;
    private final ObjectMapper objectMapper;
    
    private void handleTaxiSettings(TaxiApprovalsSettingsMessage message) {
        // hard delete
        TransportTypeEnum transportType = getTransportType(message);
        if (!transportType.equals(TAXI)) {
            return;
        }
        if (message.isDeleted()) {
            Optional<TaxiApprovalsSettings> optional = taxiSettingsService.getOptional(message.getOrganizationId());
            if (optional.isPresent()) {
                taxiSettingsService.delete(message.getOrganizationId());
                log.info("Настройки согласований такси ID '{}' были удалены", message.getId());
            } else {
                log.warn("При попытке удаления Настроек согласований такси ID '{}', они не были найдены в БД",
                         message.getId());
            }
        } else {
            taxiSettingsService.save(mapper.toTaxiSettings(message));
            log.info("Настройки согласований такси ID '{}' были записаны / отредактированы", message.getId());
        }
    }
    
    private void handlePublicSettings(PublicApprovalsSettingsMessage message) {
        // hard delete
        TransportTypeEnum transportType = getTransportType(message);
        if (!transportType.equals(PUBLIC)) {
            return;
        }
        if (message.isDeleted()) {
            Optional<PublicTrApprovalsSettings> optional = publicSettingsService.getOptional(message.getOrganizationId());
            if (optional.isPresent()) {
                publicSettingsService.delete(message.getOrganizationId());
                log.info("Настройки согласований ОТ ID '{}' были удалены", message.getId());
            } else {
                log.warn("При попытке удаления Настроек согласований ОТ ID '{}', они не были найдены в БД",
                         message.getId());
            }
        } else {
            publicSettingsService.save(mapper.toPublicSettings(message));
            log.info("Настройки согласований ОТ ID '{}' были записаны / отредактированы", message.getId());
        }
    }
    
    private void handleOtherTrSettings(OtherTrTypesApprovalsSettingsMessage message) {
        // hard delete
        TransportTypeEnum transportType = getTransportType(message);
        if (transportType.equals(TAXI) || transportType.equals(PUBLIC)) {
            return;
        }
        if (message.isDeleted()) {
            Optional<OtherTrTypesApprovalsSettings> optional =
                    otherSettingsService.getOptional(message.getOrganizationId(), transportType);
            if (optional.isPresent()) {
                otherSettingsService.delete(message.getOrganizationId(), transportType);
                log.info("Настройки согласований {} ID '{}' были удалены", transportType.name(), message.getId());
            } else {
                log.warn("При попытке удаления Настроек согласований {} ID '{}', они не были найдены в БД",
                         transportType.name(), message.getId());
            }
        } else {
            otherSettingsService.save(mapper.toOtherTrSettings(message));
            log.info("Настройки согласований {} ID '{}' были записаны / отредактированы",
                     transportType.name(), message.getId());
        }
    }
    
    @Override
    public void accept(Message<Map<String, Object>> rawMessage) {
        var message = rawMessage.getPayload();
        handleTaxiSettings(objectMapper.convertValue(message, TaxiApprovalsSettingsMessage.class));
        handlePublicSettings(objectMapper.convertValue(message, PublicApprovalsSettingsMessage.class));
        handleOtherTrSettings(objectMapper.convertValue(message, OtherTrTypesApprovalsSettingsMessage.class));
    }
    
    private TransportTypeEnum getTransportType(ApprovalsSettingsMessage message) {
        return TransportTypeEnum.getByName(message.getTransportType()).orElseThrow();
    }
}
