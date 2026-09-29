package ru.sber.transport.common.api.service.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.sber.transport.common.api.service.exception.AuthorizationException;
import ru.sber.transport.common.api.service.exception.RequestException;
import ru.sber.transport.common.api.service.exception.ServerErrorException;
import ru.sber.transport.common.api.service.service.ValidationResponseService;

@Slf4j
@Service
public class ValidationResponseServiceImpl implements ValidationResponseService {

    @Override
    public void validate(ResponseEntity<?> response) {
        log.info("Response: {}", response);
        if (response.getStatusCode().value() == 401) {
            throw new AuthorizationException("Authorization exception. " + response.getBody());
        }

        if (response.getStatusCode().is4xxClientError()) {
            throw new RequestException(response.getStatusCode() + " Error. Response: " + response.getBody());
        }
        if (response.getStatusCode().is5xxServerError()) {
            throw new ServerErrorException(response.getStatusCode() + " Error. Response: " + response.getBody());
        }
    }
}
