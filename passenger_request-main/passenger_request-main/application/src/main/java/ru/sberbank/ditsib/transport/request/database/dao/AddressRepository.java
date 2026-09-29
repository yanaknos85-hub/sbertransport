package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sberbank.ditsib.transport.request.database.model.Address;

import java.util.UUID;

/**
 * Repository for working with addresses.
 */
public interface AddressRepository extends JpaRepository<Address, UUID> {
    
    /**
     * Find address by coordinates.
     *
     * @param latitude latitude.
     * @param longitude longitude.
     *
     * @return address.
     */
    @Query("SELECT address FROM Address address " +
           "WHERE address.latitude = :latitude AND address.longitude = :longitude")
    Page<Address> findByAddressCoordinates(double latitude, double longitude, Pageable pageable);

}
