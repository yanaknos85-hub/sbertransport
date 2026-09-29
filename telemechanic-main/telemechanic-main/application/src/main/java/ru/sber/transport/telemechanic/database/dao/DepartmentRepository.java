package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.Department;
import ru.sber.transport.telemechanic.database.model.DepartmentWithChain;
import ru.sber.transport.telemechanic.dto.DepartmentWithAutoparkDto;
import ru.sber.transport.telemechanic.dto.TariffDepartmentResponse;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Repository of department
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    
    List<Department> findAllByOrganizationIdOrderByDepartmentName(UUID organizationId);
    
    @Query(value = """
                SELECT
                    d.id,
                    d.autopark_name
                FROM telemechanic.department d
                WHERE d.organization_id=:organizationId AND d.active=TRUE AND d.autopark_id IS NOT NULL
            """, nativeQuery=true)
    List<DepartmentWithAutoparkDto> findOrganiztionWithAutopark(UUID organizationId);

    @Query(nativeQuery = true, value =
            """
                with recursive
                    departments (id, name, parent, org_id, deep) as
                        -- мы сразу говорим, что глубина 1 - это начало нашего прохода до корня, тем самым задаем начальное значение
                        (select d.id, d.department_name, d.parent_id, d.organization_id, 1 as deep
                         from telemechanic.department d
                         where d.id in :departmentIds
                         union all
                         -- по мере поднятия до корня мы добавляем номер глубины
                         select ds.id, d.department_name, d.parent_id, d.organization_id, deep + 1 as deep
                         from departments ds
                                  join telemechanic.department d on d.id = ds.parent),
                    deps_chain (id, chain_deps_names, org_id) as
                        (select dd.id, string_agg(dd.name, ';') as chain_deps_names, dd.org_id
                         from (select d.id,
                                      d.name,
                                      d.org_id
                               from departments d
                                    -- тут мы сразу задаем порядок и отдаем на группировку, ревертим по значению глубины: от корня до листа
                               order by deep desc) as dd
                         group by dd.id, dd.org_id)
                select dc.id, o.official_name || ';' || dc.chain_deps_names as chain
                from deps_chain dc
                         join telemechanic.organization o
                              on o.id = dc.org_id
            """)
    List<DepartmentWithChain> findDepartmentChains(Set<UUID> departmentIds);
    
    @Query(value = """
                   SELECT id
                   FROM telemechanic.department
                   WHERE id in (:departmentIdSet)
                   AND organization_id != :organizationId
                   """,
           nativeQuery = true)
    Set<UUID> findNotOrganizationIds(UUID organizationId, Set<UUID> departmentIdSet);
    
    @Query(value = """
                   WITH RECURSIVE department_tree AS (
                    SELECT
                    	d.id,
                    	d.department_name,
                    	d.parent_id
                    FROM telemechanic.department d
                    JOIN telemechanic.ewb_tariff et ON d.id = et.department_id
                    WHERE d.organization_id = :organizationId
                        AND et.active IS TRUE
                    UNION ALL
                    SELECT
                        child.id,
                        child.department_name,
                        child.parent_id
                    FROM telemechanic.department child
                    JOIN department_tree parent ON child.parent_id = parent.id
                    WHERE child.active IS TRUE
                   )
                   SELECT DISTINCT dt.*
                   FROM department_tree dt
                   """, nativeQuery = true)
    List<TariffDepartmentResponse> findAllByOrganizationIdWithActiveTariffWithChildren(UUID organizationId);
    
    Optional<Department> findByAutoparkId(UUID autoparkId);
    @Query( value = """
                    select id
                    from telemechanic.department
                    where autopark_id in (:autoparkIds)
                    """,
            nativeQuery = true)
    List<UUID> getIdsByAutoparkIds(List<UUID> autoparkIds);
}
