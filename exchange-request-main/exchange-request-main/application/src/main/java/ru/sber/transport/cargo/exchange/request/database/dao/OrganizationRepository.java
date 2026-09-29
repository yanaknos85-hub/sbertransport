package ru.sber.transport.cargo.exchange.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.cargo.exchange.request.database.model.Organization;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью Organization.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    /**
     * Находит организацию по ИНН (inn).
     *
     * @param inn ИНН организации
     * @return Optional с найденной организацией или пустой, если не найдена
     */
    Optional<Organization> findByInn(String inn);
}
