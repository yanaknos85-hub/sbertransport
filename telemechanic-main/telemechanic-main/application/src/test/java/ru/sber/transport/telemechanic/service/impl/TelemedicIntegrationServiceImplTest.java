package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.sber.transport.telemechanic.client.TelemedicClient;
import ru.sber.transport.telemechanic.database.model.Ewb;
import ru.sber.transport.telemechanic.dto.telemedicine.TelemedicFirstTitleRequest;
import ru.sber.transport.telemechanic.exception.TelemedicClientException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemedicIntegrationServiceImplTest {
    
    @Mock
    private TelemedicClient telemedicClient;
    @InjectMocks
    private TelemedicIntegrationServiceImpl telemedicIntegrationService;
    
    @Test
    void sendTitleToContractor() {
        var name = "name";
        var content = "content";
        var apiKey = "sbertransport01";
        var ewb = Instancio.create(Ewb.class);
        
        doReturn(ResponseEntity.status(HttpStatus.BAD_GATEWAY).build()).when(telemedicClient).sendFirstTitle(
                new TelemedicFirstTitleRequest(
                        name,
                        content,
                        ewb.getEwbUuid(),
                        ewb.getDriver().getSnils()
                ), apiKey
                                                                                                            );
        
        assertThatExceptionOfType(TelemedicClientException.class)
                .isThrownBy(() -> telemedicIntegrationService.sendTitleToContractor(name, content, ewb, apiKey))
                .withMessage("Ошибка создания заявки на медицинский осмотр! Обратитесь к диспетчеру.");
        
        doReturn(ResponseEntity.ok().build()).when(telemedicClient).sendFirstTitle(new TelemedicFirstTitleRequest(name, content, ewb.getEwbUuid(),
                                                                                                                  ewb.getDriver().getSnils()), apiKey);
        telemedicIntegrationService.sendTitleToContractor(name, content, ewb, apiKey);
        verify(telemedicClient, times(2)).sendFirstTitle(any(TelemedicFirstTitleRequest.class), anyString());
    }
}