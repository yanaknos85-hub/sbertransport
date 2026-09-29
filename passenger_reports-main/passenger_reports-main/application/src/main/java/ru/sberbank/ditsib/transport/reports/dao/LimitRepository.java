package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.model.Limit;
import ru.sberbank.ditsib.transport.reports.model.Request;

import java.util.List;
import java.util.UUID;

public interface LimitRepository extends JpaRepository<Limit, UUID> {

    List<Limit> findAllByActive(boolean active);
    
    /**
     * Получить лимиты по подразделениям и году лимита
     * @param departmentIds список uuid подразделений
     * @param minYear нижняя гранциа года лимита
     * @param maxYear верхняя граница года лимита
     * @return список лимитов
     */
    List<Limit> findAllByDepartmentIdInAndYearIsBetweenAndActive(Iterable<UUID> departmentIds, int minYear, int maxYear, boolean active);
    
    @Query("SELECT sum(l.sum) from Limit l where l.sum is not null and l.year = :year and l.organizationId = :organizationId and l.active = :active" +
           " and l.transportType in :transportTypes")
    Long sumAllByYearAndOrganizationIdAndActiveAndTransportTypes(int year, UUID organizationId, boolean active,
                                                                 List<TransportTypeEnum> transportTypes);
}