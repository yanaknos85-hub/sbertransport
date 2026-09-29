package ru.sber.transport.notifications.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.NotificationContact;

import java.util.UUID;

/**
 * Репозиторий для получения данных о контактах
 */
public interface NotificationContactRepository extends JpaRepository<NotificationContact, UUID> {
}
