package ru.sber.transport.etrn.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.etrn.database.model.OrganizationGroup;

import java.util.UUID;

@Repository
public interface OrganizationGroupRepository extends JpaRepository<OrganizationGroup, UUID> {
}