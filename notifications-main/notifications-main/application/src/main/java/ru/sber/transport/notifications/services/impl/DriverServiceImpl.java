package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.dao.messages.contractor.DriverRepository;
import ru.sber.transport.notifications.database.model.NotificationContact;
import ru.sber.transport.notifications.database.model.contractor.Driver;
import ru.sber.transport.notifications.services.DriverService;
import ru.sber.transport.notifications.services.NotificationContactService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;

    private final NotificationContactService notificationContactService;

    @Override
    public Optional<Driver> get(UUID driverId) {
        return driverRepository.findById(driverId);
    }

    @Override
    public Driver save(Driver driver) {
        var saved = driverRepository.save(driver);

        notificationContactService.save(NotificationContact.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .phoneConfirmed(saved.isPhoneConfirmed())
                .build());

        return saved;
    }

    @Override
    public void deleteById(UUID driverId) {
        driverRepository.deleteById(driverId);
        notificationContactService.get(driverId)
                .map(NotificationContact::getId)
                .ifPresent(notificationContactService::deleteById);
    }

}
