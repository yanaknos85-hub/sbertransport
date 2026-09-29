package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.EvalSettings;
import ru.sberbank.ditsib.transport.request.database.model.EvalSettingsId;

@Repository
public interface EvalSettingsRepository extends JpaRepository<EvalSettings, EvalSettingsId> {
}
