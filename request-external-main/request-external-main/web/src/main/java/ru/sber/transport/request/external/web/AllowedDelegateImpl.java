package ru.sber.transport.request.external.web;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.business.providers.AvailableClasses;
import ru.sber.transport.web.api.TemporaryApi;
import ru.sber.transport.web.model.Allowed;

@RequiredArgsConstructor
public class AllowedDelegateImpl implements TemporaryApi {

    private final AvailableClasses availableClasses;

    @Override
    public CompletableFuture<ResponseEntity<Allowed>> allowOrder() {
        final var user = ControllerUtils.currentUser();
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok(new Allowed(availableClasses.allow(user))));
    }
}
