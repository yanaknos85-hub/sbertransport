package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.user.UserControls;
import ru.sberbank.ditsib.transport.reports.model.user.UserProperty;

import java.util.UUID;

@Repository
public interface UserPropertiesRepository extends JpaRepository<UserProperty, UUID> {

}
