package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.client.KorusClient;
import ru.sber.transport.telemechanic.dto.ewb.KorusEwbTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.KorusTitleRequest;
import ru.sber.transport.telemechanic.service.SignatureVerifier;

import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class EdfOperatorClientServiceImplTest {
    
    @Mock
    private SignatureVerifier signatureVerifier;
    @Mock
    private KorusClient korusClient;
    @InjectMocks
    private EdfOperatorClientServiceImpl edfOperatorClientService;
    
    @Test
    void sendTitle() {
        var employeeFullName = Instancio.create(String.class);
        var fileName = Instancio.create(String.class);
        var content = Instancio.create(String.class).getBytes();
        var signature = Instancio.create(String.class);
        
        doNothing().when(signatureVerifier).verify(content, signature, employeeFullName);
        var korusId = UUID.randomUUID();
        var chainId = UUID.randomUUID();
        doReturn(new KorusEwbTitleResponse(korusId, chainId)).when(korusClient).sendTitle(any(KorusTitleRequest.class));
        
        var actual = edfOperatorClientService.sendTitle(employeeFullName, fileName, content, signature);
        assertThat(actual)
                .isNotNull()
                .extracting(
                        KorusEwbTitleResponse::id,
                        KorusEwbTitleResponse::chainId
                           )
                .containsExactly(
                        korusId,
                        chainId
                                );
    }
}