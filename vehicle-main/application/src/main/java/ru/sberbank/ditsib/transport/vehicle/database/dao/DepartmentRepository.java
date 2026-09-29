package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Department;

import java.util.List;
import java.util.UUID;

/**
 * Repository of department
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    /**
     * Получаем подразделения по дереву вверх, включая начальное подразделение
     *
     * @param rootId начальное подразделение для построения дерева
     * @return список идентификаторов подразделений
     */
    @Query(value = """
            with recursive parent_to_id(id, parent_id) as (
                select id, parent_id
                from vehicle.department
                where id = :rootId
                union all
                select d.id, d.parent_id
                from parent_to_id p,
                     vehicle.department d
                where d.id = p.parent_id)
            select d.id
            from vehicle.department d,
                 parent_to_id p
            where d.id = p.id
            """, nativeQuery = true)
    List<UUID> getParentDepartments(UUID rootId);

    @Query(value = "SELECT d.id, d.organization_id FROM vehicle.department d WHERE d.id IN (:depIds)", nativeQuery = true)
    List<Object[]> findDepOrgPairsByIds(@Param("depIds") List<UUID> depIds);
}
