package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.telemechanic.client.TelemedicClient;
import ru.sber.transport.telemechanic.database.model.Ewb;
import ru.sber.transport.telemechanic.dto.telemedicine.TelemedicFirstTitleRequest;
import ru.sber.transport.telemechanic.exception.TelemedicClientException;
import ru.sber.transport.telemechanic.service.TelemedicIntegrationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemedicIntegrationServiceImpl implements TelemedicIntegrationService {
    
    private final TelemedicClient telemedicClient;
    
    @Override
    public void sendTitleToContractor(String name, String content, Ewb ewb, String contractorApiToken) {
        var response = telemedicClient.sendFirstTitle(new TelemedicFirstTitleRequest(
                name,
                content,
                ewb.getEwbUuid(),
                ewb.getDriver().getSnils()
        ), contractorApiToken);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new TelemedicClientException("Ошибка создания заявки на медицинский осмотр! Обратитесь к диспетчеру.");
        }
    }
}
