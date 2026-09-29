package ru.sber.transport.trip.web.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.trip.business.TripUseCases;
import ru.sber.transport.trip.business.dto.*;
import ru.sber.transport.trip.business.model.Request;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.messaging.mapper.RequestMapper;
import ru.sber.transport.trip.messaging.providers.ContractorProvider;
import ru.sber.transport.trip.messaging.providers.IntegrationClientProvider;
import ru.sber.transport.trip.web.controller.IntegrationController;
import ru.sber.transport.trip.web.exceptions.UnauthorizedException;
import ru.sber.transport.trip.web.service.RequestService;
import ru.sber.transport.trip.web.service.ResultProcessor;

import java.util.List;
import java.util.UUID;

@Slf4j
@Transactional
@RestController
@RequiredArgsConstructor
public class IntegrationControllerImpl implements IntegrationController {

    private final TripUseCases<Request> tripUseCases;

    private final RequestMapper requestMapper;

    private final ContractorProvider contractorProvider;

    private final RequestService requestService;

    private final ResultProcessor resultProcessor;

    private final TripProvider tripProvider;

    private final IntegrationClientProvider integrationClientProvider;

    private final ObjectMapper objectMapper;

    @Override
    public CreateTripResponse createTrip(IntegrationRequestDTO integrationRequestDTO, JwtAuthenticationToken authentication) throws JsonProcessingException {
        log.debug("Trip creation ({}): {}", integrationRequestDTO.getHumanReadableId(), objectMapper.writeValueAsString(integrationRequestDTO));
        var contractorId = checkAuth(authentication);
        var result = tripUseCases.process(integrationRequestDTO, contractorId);
        log.debug("Trip creation response ({}): {}", integrationRequestDTO.getHumanReadableId(), objectMapper.writeValueAsString(result));
        return result;
    }

    @Override
    public GetTripResponse getTrip(String humanReadableId, JwtAuthenticationToken authentication) throws JsonProcessingException {
        log.debug("Get trip with id: {}", humanReadableId);
        var contractorId = checkAuth(authentication);
        var trip = requestService.find(contractorId, humanReadableId);
        if(trip.isEmpty()){
            return new GetTripResponse(false,
                    null,
                    new GetTripResponse.Error(
                            HttpStatus.NOT_FOUND.value(),
                            "Trip "+humanReadableId+" not found")
            );
        }
        var result = resultProcessor.processForIntegration(trip.get());
        log.debug("Get trip response ({}): {}", humanReadableId, objectMapper.writeValueAsString(result));
        return result;
    }

    @Override
    public CreateTripResponse cancelTrip(String humanReadableId, JwtAuthenticationToken authentication) throws JsonProcessingException {
        log.debug("Cancel trip with id: {}", humanReadableId);
        var contractorId = checkAuth(authentication);
        var tripOpt = tripProvider.findByContractorIdAndHumanReadableId(contractorId, humanReadableId);
        if(tripOpt.isEmpty()){
            return new CreateTripResponse(false,
                    null,
                    null,
                    new CreateTripResponse.Error(
                            HttpStatus.NOT_FOUND.value(),
                            "Trip "+humanReadableId+" not found")
            );
        }
        tripOpt.get().setHumanReadableId(humanReadableId);
        var result = tripUseCases.processCancel(tripOpt.get());
        log.debug("Cancel trip response ({}): {}", humanReadableId, objectMapper.writeValueAsString(result));
        return result;
    }

    private UUID checkAuth(JwtAuthenticationToken authentication) {
        var contractorId = UUID.fromString(authentication.getToken().getId());

        if(!contractorProvider.checkContractorExistence(contractorId)){
            var client = integrationClientProvider.get(contractorId);
            if (client.isEmpty()) {
                throw new UnauthorizedException("Unauthorized");
            }
            return client.get().getContractorId();
        } else return contractorId;
    }

}
