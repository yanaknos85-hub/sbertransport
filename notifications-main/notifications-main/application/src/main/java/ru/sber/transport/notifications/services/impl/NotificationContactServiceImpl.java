package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.dao.NotificationContactRepository;
import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.NotificationContact;
import ru.sber.transport.notifications.messaging.listeners.mappers.NotificationContactMapper;
import ru.sber.transport.notifications.services.NotificationContactService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationContactServiceImpl implements NotificationContactService {

    private final NotificationContactRepository repository;

    private final NotificationContactMapper mapper;

    @Override
    public Optional<NotificationContact> get(UUID id) {
        return repository.findById(id);
    }

    @Override
    public NotificationContact save(NotificationContact contact) {
        return repository.save(contact);
    }

    @Override
    public void deleteById(UUID contactId) {
        repository.deleteById(contactId);
    }

    @Override
    public Optional<HasContactData> findContactDataById(UUID id) {
        return get(id)
                .flatMap(mapper::toContactData);
    }
}
