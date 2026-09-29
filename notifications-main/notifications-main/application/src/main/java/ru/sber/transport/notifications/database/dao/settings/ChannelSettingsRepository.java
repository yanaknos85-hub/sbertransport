package ru.sber.transport.notifications.database.dao.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelSettings;

import java.util.UUID;

/**
 * Репозиторий для работы с настройками уведомлений.
 */
public interface ChannelSettingsRepository extends JpaRepository<ChannelSettings, UUID> {
}
