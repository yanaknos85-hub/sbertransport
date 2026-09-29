package ru.sber.transport.dispatcher.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.authorization.exceptions.UnauthorizedException;
import ru.sber.transport.dispatcher.database.dao.ContractorRepository;
import ru.sber.transport.dispatcher.database.dao.DispatcherRepository;
import ru.sber.transport.dispatcher.service.AuthCheckService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthCheckServiceImpl implements AuthCheckService {

    private final ContractorRepository contractorRepository;

    private final DispatcherRepository dispatcherRepository;

    @Override
    public UUID getContractorIdByToken(JwtAuthenticationToken token) {
        var userId = UUID.fromString(token.getToken().getId());
        var contractor = contractorRepository.findById(userId);
        if (contractor.isPresent()) {
            return contractor.get().getId();
        } else {
            var dispatcher = dispatcherRepository.findById(userId).orElseGet(() -> dispatcherRepository.findByOauthId(userId).orElse(null));
            if (dispatcher != null) {
                return dispatcher.getContractor().getId();
            } else throw new UnauthorizedException(userId);
        }
    }
}
