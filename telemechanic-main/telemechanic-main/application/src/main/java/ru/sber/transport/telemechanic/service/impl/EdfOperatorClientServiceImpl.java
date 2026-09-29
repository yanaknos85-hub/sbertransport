package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.telemechanic.client.KorusClient;
import ru.sber.transport.telemechanic.dto.ewb.KorusEwbTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.KorusTitleRequest;
import ru.sber.transport.telemechanic.helper.EwbHelper;
import ru.sber.transport.telemechanic.service.EdfOperatorClientService;
import ru.sber.transport.telemechanic.service.SignatureVerifier;

@Slf4j
@Service
@RequiredArgsConstructor
public class EdfOperatorClientServiceImpl implements EdfOperatorClientService {
    
    private final SignatureVerifier signatureVerifier;
    private final KorusClient korusClient;
    
    @Override
    public KorusEwbTitleResponse sendTitle(String employeeFullName, String fileName, byte[] content, String signature) {
        signatureVerifier.verify(content, signature, employeeFullName);
        var archive = EwbHelper.zipFiles(content, signature.getBytes(), fileName);
        var response = korusClient.sendTitle(KorusTitleRequest.builder()
                                                              .name("%s.zip".formatted(fileName))
                                                              .content(archive)
                                                              .build());
        log.info("Response from Korus {}", response);
        return response;
    }
}
