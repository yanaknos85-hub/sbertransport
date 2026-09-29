package ru.sber.transport.token_generator.providers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.token_generator.database.token_generator.tables.Roles;
import ru.sber.transport.token_generator.database.token_generator.tables.records.RolesRecord;
import ru.sber.transport.token_generator.messaging.providers.RolesProvider;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
class RolesProviderImpl implements RolesProvider, ru.sber.transport.token_generator.grpc.providers.RolesProvider {

    private final JooqRepository<Roles, RolesRecord, String> repository;

    @Override
    public Optional<RolesRecord> get(String id) {
        return repository.findById(id);
    }

    @Override
    public void save(RolesRecord role) {
        repository.save(role);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public boolean isDataMaster(List<String> roles) {
        return repository.findAllById(roles).stream().map(RolesRecord::getDataMaster)
                .reduce((l, r) -> l || r).orElse(false);
    }
}
