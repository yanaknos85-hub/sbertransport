package ru.sberbank.ditsib.transport.srm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.srm.model.SrmRequestKpi;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий совместных поездок
 */
@Repository
public interface SrmRequestKpiRepository extends JpaRepository<SrmRequestKpi, UUID>,
        JpaSpecificationExecutor<SrmRequestKpi> {
    
    /**
     * Получение по старому идентификатору.
     *
     * @param oldId старый идентификатор
     *
     * @return объект kpi заявки
     */
    Optional<SrmRequestKpi> findByOldId(Integer oldId);
    
    /**
     * Получение по идентификатору оригинального запроса.
     *
     * @param orgRequestId идентификатор оригинального запроса
     *
     * @return объект kpi заявки
     */
    Optional<SrmRequestKpi> findByOrgRequestId(UUID orgRequestId);
}
