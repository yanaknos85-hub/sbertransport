package ru.sber.transport.dispatcher.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.sber.transport.dispatcher.database.model.IntegrationClient;

import java.util.UUID;

/**
 * Репозиторий для работы с клиентами интеграции
 */
@Repository
public interface IntegrationClientRepository extends JpaRepository<IntegrationClient, UUID>, JpaSpecificationExecutor<IntegrationClient> {
}
