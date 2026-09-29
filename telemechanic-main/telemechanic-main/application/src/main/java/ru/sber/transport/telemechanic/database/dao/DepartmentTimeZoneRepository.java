package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.DepartmentTimeZone;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentTimeZoneRepository extends JpaRepository<DepartmentTimeZone, UUID> {
    
    @Query(
            value = """
                    WITH RECURSIVE department_hierarchy AS (
                        SELECT
                            d.id,
                            d.parent_id,
                            dtz.time_zone,
                            0 as level
                        FROM telemechanic.department d
                        LEFT JOIN telemechanic.department_time_zone dtz ON d.id = dtz.department_id
                        WHERE d.id = :departmentId
                        UNION ALL
                        SELECT
                            parent.id,
                            parent.parent_id,
                            dtz.time_zone,
                            child.level + 1
                        FROM telemechanic.department parent
                        JOIN department_hierarchy child ON parent.id = child.parent_id
                        LEFT JOIN telemechanic.department_time_zone dtz ON parent.id = dtz.department_id
                    )
                    SELECT time_zone
                    FROM department_hierarchy
                    WHERE time_zone IS NOT NULL
                    ORDER BY level ASC
                    LIMIT 1
                    """, nativeQuery = true
    )
    List<String> findByDepartmentIdWithHierarchy(UUID departmentId);
}
