package ru.sberbank.transport.oto.cargo.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.transport.oto.cargo.database.model.Department;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    
    @Query(value = "WITH RECURSIVE r AS (SELECT * FROM oto_cargo.department WHERE parent_id = ? UNION SELECT oto_cargo.department.* " +
            "FROM oto_cargo.department JOIN r ON oto_cargo.department.parent_id = r.id) SELECT * FROM r;", nativeQuery = true)
    List<Department> findAllByParentId(UUID parentId);
    
}
