package ru.sberbank.ditsib.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.database.model.PointLead;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с точками маршрута заявок.
 * Предоставляет базовые операции CRUD и поиск точек по главной заявке.
 */
@Repository
public interface PointLeadRepository extends JpaRepository<PointLead, UUID> {

    /**
     * Находит все точки маршрута для указанной главной заявки.
     * @param id идентификатор главной заявки
     * @return список точек маршрута
     */
    List<PointLead> findAllByMainLeadId(UUID id);
}

