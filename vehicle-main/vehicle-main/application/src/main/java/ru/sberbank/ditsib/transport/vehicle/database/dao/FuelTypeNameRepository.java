package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelTypeName;

import java.util.List;

@Repository
public interface FuelTypeNameRepository extends JpaRepository<FuelTypeName, FuelTypeName.FuelTypeNameId> {

    List<FuelTypeName> findByNameIgnoreCaseIn(List<String> names);

}
