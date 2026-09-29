package ru.sber.transport.trips.cargo.web.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trips.cargo.business.TripUseCases;
import ru.sber.transport.trips.cargo.business.dto.CreateTripResponse;
import ru.sber.transport.trips.cargo.business.dto.GetTripResponse;
import ru.sber.transport.trips.cargo.business.dto.IntegrationRequestDTO;
import ru.sber.transport.trips.cargo.business.dto.Prefix;
import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.messaging.providers.IntegrationClientProvider;
import ru.sber.transport.trips.cargo.web.controller.IntegrationController;
import ru.sber.transport.trips.cargo.web.exceptions.UnauthorizedException;
import ru.sber.transport.trips.cargo.web.service.RequestService;
import ru.sber.transport.trips.cargo.web.service.ResultProcessor;
import ru.sberbank.ditsib.transport.request.messaging.RouteMessage;

import java.util.List;
import java.util.UUID;

@Slf4j
@Transactional
@RestController
@RequiredArgsConstructor
public class IntegrationControllerImpl implements IntegrationController {

    private final ContractorProvider contractorProvider;

    private final ResultProcessor resultProcessor;

    private final TripUseCases<RouteMessage> tripUseCases;

    private final RequestService requestService;

    private final TripProvider tripProvider;

    private final IntegrationClientProvider integrationClientProvider;

    @Override
    public List<CreateTripResponse> createTrip(List<IntegrationRequestDTO> integrationRequestDTO, JwtAuthenticationToken authentication) {
        var contractorId = checkAuth(authentication);
        return integrationRequestDTO.stream()
                .map(integrationRequest -> tripUseCases.process(integrationRequest, contractorId))
                .toList();
    }

    @Override
    public GetTripResponse getTrip(String humanReadableId, JwtAuthenticationToken authentication) {
        var contractorId = checkAuth(authentication);
        var trip = requestService.find(contractorId, humanReadableId);
        if(trip.isEmpty()){
            return new GetTripResponse(false,
                    null,
                    new GetTripResponse.Error(
                            HttpStatus.NOT_FOUND.value(),
                            "Route "+humanReadableId+" not found")
            );
        }
        return resultProcessor.processForIntegration(trip.get());
    }

    @Override
    public CreateTripResponse cancelTrip(String humanReadableId, JwtAuthenticationToken authentication) {
        var contractorId = checkAuth(authentication);
        var tripOpt = tripProvider.findByContractorIdAndHumanReadableId(contractorId, humanReadableId);
        if(tripOpt.isEmpty()){
            return new CreateTripResponse(false,
                    null,
                    null,
                    new CreateTripResponse.Error(
                            HttpStatus.NOT_FOUND.value(),
                            "Route "+humanReadableId+" not found")
            );
        }
        tripOpt.get().setHumanReadableId(humanReadableId);
        return tripUseCases.processCancel(tripOpt.get());
    }

    private UUID checkAuth(JwtAuthenticationToken authentication){
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
