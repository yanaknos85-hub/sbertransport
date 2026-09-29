package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.dao.messages.contractor.DispatcherRepository;
import ru.sber.transport.notifications.database.model.NotificationContact;
import ru.sber.transport.notifications.database.model.contractor.Dispatcher;
import ru.sber.transport.notifications.services.DispatcherService;
import ru.sber.transport.notifications.services.NotificationContactService;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DispatcherServiceImpl implements DispatcherService {

    private final DispatcherRepository dispatcherRepository;

    private final NotificationContactService notificationContactService;

    @Override
    public Optional<Dispatcher> get(UUID id) {
        return dispatcherRepository.findById(id);
    }

    @Override
    public Dispatcher save(Dispatcher dispatcher) {
        var saved = dispatcherRepository.save(dispatcher);

        notificationContactService.save(NotificationContact.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .phoneConfirmed(saved.isPhoneConfirmed())
                .build());

        return saved;
    }

    @Override
    public void deleteById(UUID dispatcherId) {
        dispatcherRepository.deleteById(dispatcherId);
        notificationContactService.get(dispatcherId)
                .map(NotificationContact::getId)
                .ifPresent(notificationContactService::deleteById);
    }
}
