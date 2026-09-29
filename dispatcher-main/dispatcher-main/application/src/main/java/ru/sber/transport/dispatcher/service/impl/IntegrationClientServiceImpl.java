package ru.sber.transport.dispatcher.service.impl;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.database.dao.IntegrationClientRepository;
import ru.sber.transport.dispatcher.database.model.IntegrationClient;
import ru.sber.transport.dispatcher.dto.feign.RegistrationDataDto;
import ru.sber.transport.dispatcher.exceptions.FeignClientException;
import ru.sber.transport.dispatcher.feign.RegistrationClient;
import ru.sber.transport.dispatcher.messaging.senders.IntegrationClientSender;
import ru.sber.transport.dispatcher.service.IntegrationClientService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IntegrationClientServiceImpl implements IntegrationClientService {

    private final RegistrationClient client;

    private final IntegrationClientRepository repository;

    private final IntegrationClientSender sender;

    @Override
    public void add(@NotNull UUID contractorId, @NotNull String email, @NotNull String login, @NotNull String password) {
        var request = RegistrationDataDto.builder()
                .email(email)
                .login(login)
                .password(password)
                .scope("CONTRACTOR")
                .build();
        UUID userId;
        try {
            var response = client.register(request);
            userId = response.userId();
        } catch (FeignException.FeignServerException e) {
            throw new RuntimeException("Ошибка при регистрации ТУЗ, cервис авторизации недоступен", e);
        } catch (FeignException.FeignClientException e) {
            throw new FeignClientException("Ошибка при регистрации ТУЗ, cервис авторизации вернул ошибку", e);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при регистрации ТУЗ, непредвиденная ошибка", e);
        }
        var integrationClient = IntegrationClient.builder()
                .id(userId)
                .contractorId(contractorId)
                .build();
        integrationClient = repository.save(integrationClient);
        sender.send(integrationClient);
        log.info("Create new TA for contractor {}, userId {}", contractorId, userId);
    }


}
