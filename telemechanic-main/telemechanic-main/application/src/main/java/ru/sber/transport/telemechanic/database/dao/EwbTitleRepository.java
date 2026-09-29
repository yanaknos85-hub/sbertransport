package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.database.model.EwbTitle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EwbTitleRepository extends JpaRepository<EwbTitle, UUID> {
    Optional<EwbTitle> findByEwbIdAndType(UUID ewbId, EwbTitleType type);
    Optional<EwbTitle> findByEwbEwbUuidAndType(UUID ewbId, EwbTitleType type);
    List<EwbTitle> findByEwbId(UUID ewbId);
}
