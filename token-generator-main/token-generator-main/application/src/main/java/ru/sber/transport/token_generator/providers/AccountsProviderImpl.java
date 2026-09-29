package ru.sber.transport.token_generator.providers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.sudir.messages.AccountMessage;
import ru.sber.transport.token_generator.messaging.providers.AccountsProvider;
import ru.sber.transport.token_generator.providers.database.AccountRepository;

import java.util.Collection;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class AccountsProviderImpl implements AccountsProvider {

    private final AccountRepository repository;

    @Override
    public void save(String id, AccountMessage message) {
        var existRoles = repository.findRoles(id);
        var newRoles = message.roles();
        var rolesToDelete = existRoles.stream().filter(role -> !newRoles.contains(role)).toList();
        var rolesToAdd = newRoles.stream().filter(role -> !existRoles.contains(role)).toList();
        repository.saveAll(id, rolesToAdd);
        repository.deleteAll(id, rolesToDelete);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(UUID.fromString(id));
    }

    @Override
    public Collection<String> findRoles(String id) {
        return repository.findRoles(id);
    }
}
