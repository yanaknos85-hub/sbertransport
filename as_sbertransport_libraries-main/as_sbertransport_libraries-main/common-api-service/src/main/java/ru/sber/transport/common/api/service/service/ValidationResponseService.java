package ru.sber.transport.common.api.service.service;

import org.springframework.http.ResponseEntity;

public interface ValidationResponseService {

    void validate(ResponseEntity<?> response);

}
