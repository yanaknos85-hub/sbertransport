package ru.sber.transport.authsb.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.authsb.database.model.Organization;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByOgrnAndKpp(String ogrn, String kpp);
}
