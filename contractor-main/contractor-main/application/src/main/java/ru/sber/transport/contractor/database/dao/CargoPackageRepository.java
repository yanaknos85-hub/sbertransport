package ru.sber.transport.contractor.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.contractor.database.model.CargoPackage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for cargo package
 */
@Repository
public interface CargoPackageRepository extends JpaRepository<CargoPackage, UUID> {

    /**
     * Get package by id.
     *
     * @param contractorId id of contractor of package
     * @param uuid id a package
     *
     * @return  package.
     */
    @Query("SELECT c from CargoPackage c where c.id = :uuid and " +
           "c.contractor.id = :contractorId and c.active = true")
    Optional<CargoPackage> findByIdAndContractor(@NotNull UUID uuid, @NotNull UUID contractorId);

    /**
     * Get all package by contractor id
     *
     * @param contractorId id of contractor of package
     *
     * @return  package.
     */
    @Query("SELECT c from CargoPackage c where " +
            "c.contractor.id = :contractorId and c.active = true")
    List<CargoPackage> findAllByContractor(@NotNull UUID contractorId);
    
    /**
     * Get package by label and contractor id
     *
     * @param label id a package
     * @param contractorId id of contractor of package
     *
     * @return  package.
     */
    @Query("SELECT c from CargoPackage c where " +
           "lower(c.label) = lower(:label) and " +
           "c.contractor.id = :contractorId and " +
           "c.active = true")
    Optional<CargoPackage> findByLabelAndContractor(@NotBlank String label, @NotNull UUID contractorId);
}
