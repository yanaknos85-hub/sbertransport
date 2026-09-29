package ru.sber.transport.etrn.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.etrn.controller.EtrnController;
import ru.sber.transport.etrn.dto.*;
import ru.sber.transport.etrn.service.AttorneyCheckService;
import ru.sber.transport.etrn.service.EtrnService;
import ru.sber.transport.etrn.service.LockService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2ERqId;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@E2EController
public class EtrnControllerImpl implements EtrnController {

    private final EtrnService etrnService;
    private final LockService lockService;
    private final AttorneyCheckService attorneyCheckService;

    @Override
    public ResponseEntity<EtrnDto> create(EtrnCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(etrnService.create(request));
    }

    @Override
    public ResponseEntity<Page<EtrnJournalDto>> list(SearchEtrnDto request) {
        return ResponseEntity.ok(etrnService.list(request));
    }

    @Override
    public ResponseEntity<EtrnDetailDto> getById(@E2ERqId UUID id) {
        return ResponseEntity.ok(etrnService.getById(id));
    }

    @Override
    public ResponseEntity<Void> lock(@E2ERqId UUID id, @E2EUser("principal") Authentication authentication) {
        lockService.lock(id, authentication);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> unlock(@E2ERqId UUID id, @E2EUser("principal") Authentication authentication) {
        lockService.unlock(id, authentication);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<AttorneyCheckResponseDto> attorneyCheck(@E2EUser("principal") Authentication authentication) {
        return attorneyCheckService.checkAttorney();
    }
}