package ru.sberbank.transport.oto.cargo.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.transport.oto.cargo.database.model.OrganizationGroup;

import java.util.UUID;

@Repository
public interface OrganizationGroupRepository extends JpaRepository<OrganizationGroup, UUID> {
}