package ru.sber.transport.dispatcher.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.database.model.Dispatcher;
import ru.sber.transport.dispatcher.dto.*;
import ru.sber.transport.dispatcher.dto.enums.PatchField;
import ru.sber.transport.dispatcher.dto.search.DispatcherSearchDto;
import ru.sber.transport.dispatcher.mappers.DispatcherMapper;
import ru.sber.transport.dispatcher.service.ContractorService;
import ru.sber.transport.dispatcher.service.DispatcherService;
import ru.sber.transport.dispatcher.controller.DispatcherController;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
class DispatcherControllerImpl implements DispatcherController {

    private final ContractorService contractorService;

    private final DispatcherService dispatcherService;

    private final DispatcherMapper mapper;

    @Override
    public DispatcherDto add(UUID contractorId, NewDispatcherDto dispatcher) {
        checkContractor(contractorId);
        var saved = dispatcherService.add(contractorId, dispatcher);
        return mapper.toDto(saved);
    }

    @Override
    public void edit(UUID contractorId, UUID dispatcherId, NewDispatcherDto dispatcher) {
        checkContractor(contractorId);
        dispatcherService.edit(contractorId, dispatcherId, dispatcher);
    }

    @Override
    public void delete(UUID contractorId, UUID dispatcherId) {
        checkContractor(contractorId);
        dispatcherService.delete(contractorId, dispatcherId);
    }

    @Override
    public void delete(UUID contractorId) {
        checkContractor(contractorId);
        dispatcherService.delete(contractorId);
    }

    @Override
    public DispatcherDto get(UUID contractorId, UUID dispatcherId) {
        checkContractor(contractorId);
        return dispatcherService.get(contractorId, dispatcherId)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(Dispatcher.class, dispatcherId));
    }

    @Override
    public Iterable<HasName> getAll(UUID contractorId, Projection projection, DispatcherSearchDto searchDto) {
        checkContractor(contractorId);
        var effectiveProjection = Optional.ofNullable(projection).orElse(Projection.FULL);
        var dispatchers = dispatcherService.get(contractorId, searchDto, effectiveProjection);
        log.trace("Dispatchers:\n" + dispatchers);
        return dispatchers;
    }

    @Override
    public void patch(UUID contractorId, UUID dispatcherId, List<PatchDataV2> data) {
        checkContractor(contractorId);
        dispatcherService.patchDispatcher(dispatcherId, PatchField.getPatchDataMap(data));
    }

    @Override
    public DispatcherDto getSelfProfile(Authentication authentication) {
        var userId = UUID.fromString(((JwtAuthenticationToken) authentication).getToken().getId());
        return dispatcherService.get(userId)
                .map(mapper::toDto)
                .orElseGet(() -> dispatcherService.getByOauthId(userId).map(mapper::toDto)
                        .orElseThrow(() -> new EntityNotFoundException(Dispatcher.class, userId)));
    }

    @Override
    public void signPdn(Authentication authentication) {
        var userId = UUID.fromString(((JwtAuthenticationToken) authentication).getToken().getId());
        dispatcherService.signPdn(userId);
    }

    @Override
    public void patchDispatcher(List<PatchDataV2> data, Authentication authentication) {
        var authenticated = UUID.fromString(((JwtAuthenticationToken) authentication).getToken().getId());
        dispatcherService.patchDispatcher(authenticated, data.stream().collect(Collectors.toMap(PatchDataV2::field, PatchDataV2::value)));
    }


    private void checkContractor(UUID contractorId) {
        if (!contractorService.isContractorExists(contractorId)) {
            throw new EntityNotFoundException(Contractor.class, contractorId);
        }
    }
}
