package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.reports.model.attributes.UserAttributes;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for persisting user attributes.
 */
public interface UserAttributesRepository extends JpaRepository<UserAttributes, UUID> {
    Optional<UserAttributes> findByUserId(UUID userId);
}
