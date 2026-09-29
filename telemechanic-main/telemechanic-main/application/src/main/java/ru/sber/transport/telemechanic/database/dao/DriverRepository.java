package ru.sber.transport.telemechanic.database.dao;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.database.projection.DriverByFioProjection;
import ru.sber.transport.telemechanic.dto.driver.DriverSearchRequest;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID>, JpaSpecificationExecutor<Driver> {
    
    @NotNull
    @Override
    @EntityGraph(attributePaths = {
            Driver_.DRIVING_LICENSE,
            Driver_.DRIVING_LICENSE + "." + DrivingLicense_.CATEGORIES,
            Driver_.EMPLOYEE,
            Driver_.EMPLOYEE + "." + Employee_.DEPARTMENT,
            Driver_.EMPLOYEE + "." + Employee_.ORGANIZATION
    })
    Optional<Driver> findById(@NotNull UUID id);
    
    boolean existsByEmployeeIdAndDrivingLicenseSeriesAndDrivingLicenseNumberAndDrivingLicenseActiveIsTrue(UUID employeeId, String series, String number);
    
    @Query(value = """
                   SELECT COALESCE(
                       (SELECT TRUE
                        FROM telemechanic.driver d
                        JOIN telemechanic.employee e on e.id = d.employee_id
                        WHERE e.active
                          AND d.tin = :tin
                          AND d.snils = :snils
                          AND d.employee_id <> :employeeId),
                        FALSE
                   )
                   """, nativeQuery = true)
    boolean existsByTinAndSnilsAndActiveEmployeeNot(String tin, String snils, UUID employeeId);
    
    boolean existsByDrivingLicenseSeriesAndDrivingLicenseNumberAndDrivingLicenseActiveIsTrueAndEmployeeIdNot(
            String series, String number,
            UUID employeeId
                                                                                                            );
    
    @SuppressWarnings("java:S100")
    @EntityGraph(attributePaths = {
            Driver_.EMPLOYEE,
            Driver_.EMPLOYEE + "." + Employee_.DEPARTMENT,
            Driver_.DRIVING_LICENSE
    })
    Optional<Driver> findWithEmployeeAndLicenseByEmployeeIdAndDrivingLicense_ActiveTrue(UUID employeeId);
    
    @EntityGraph(attributePaths = { Driver_.DRIVING_LICENSE })
    Optional<Driver> findWithLicenseByEmployeeId(UUID employeeId);
    
    @Query(value = """
                   SELECT d.id                AS id,
                          e.personnel_number  AS personnelNumber,
                          concat(e.last_name, ' ', e.first_name, COALESCE(' ' || e.patronymic, '')) AS fullName,
                          o.official_name     AS organizationName,
                          dep.id              AS departmentId,
                          dep.department_name AS departmentName,
                          d.tin               AS tin,
                          dl.id               AS drivingLicenceId,
                          dl.series           AS series,
                          dl."number"         AS number,
                          dl.issue_date       AS issueDate
                   FROM telemechanic.driver d
                   JOIN telemechanic.driving_license dl ON dl.active = TRUE AND d.driving_license_id = dl.id
                   JOIN telemechanic.employee e ON d.employee_id = e.id
                   JOIN telemechanic.fleet_owner_organization foo ON foo.active = TRUE AND foo.organization_id = :dispatcherOrganizationId
                   JOIN telemechanic.organization o ON e.organization_id = o.id
                   JOIN telemechanic.department dep ON e.department_id = dep.id
                   WHERE (:fio IS NULL OR upper(e.full_name_index) LIKE concat('%', upper(replace(:fio, ' ', '')), '%'))
                          AND EXISTS (
                            SELECT 1
                            FROM telemechanic.transport t
                            JOIN telemechanic.transport_organization to_org ON t.id = to_org.transport_id AND to_org.organization_id = e.organization_id
                            )
                   ORDER BY e.full_name_index ASC
                   """, nativeQuery = true,
           countQuery = """
                        SELECT count(*)
                        FROM telemechanic.driver d
                        JOIN telemechanic.driving_license dl ON dl.active = TRUE AND d.driving_license_id = dl.id
                        JOIN telemechanic.employee e ON e.organization_id = :dispatcherOrganizationId AND d.employee_id = e.id
                        JOIN telemechanic.fleet_owner_organization foo ON foo.active = TRUE AND foo.organization_id = :dispatcherOrganizationId
                        WHERE (:fio IS NULL OR upper(e.full_name_index) LIKE concat('%', upper(replace(:fio, ' ', '')), '%'))
                                AND EXISTS (
                                    SELECT 1
                                    FROM telemechanic.transport t
                                    JOIN telemechanic.transport_organization to_org ON t.id = to_org.transport_id
                                        AND to_org.organization_id = e.organization_id
                                )
                        """)
    Page<DriverByFioProjection> findByFio(UUID dispatcherOrganizationId, String fio, UUID transportId, PageRequest pageRequest);
    
    @Query(value = """
                   SELECT   dr.id AS id, e.personnel_number                                             AS personnelNumber,
                            CONCAT(e.last_name, ' ', e.first_name, COALESCE(' ' || e.patronymic, ''))   AS fullName,
                            o.official_name                                                             AS organizationName,
                            dep.department_name                                                         AS departmentName,
                            dr.tin                                                                      AS tin,
                            dr.snils                                                                    AS snils,
                            dl.series                                                                   AS series,
                            dl."number"                                                                 AS number,
                            dl.issue_date                                                               AS issueDate,
                            dl.expiry_date                                                              AS expiryDate,
                            dl.active                                                                   AS active,
                            array_agg(c.category_code ORDER BY c.category_code ASC)                     AS categoryNames
                   FROM telemechanic.driver dr
                   JOIN telemechanic.employee e ON dr.employee_id = e.id
                   JOIN telemechanic.organization o ON e.organization_id = o.id
                   JOIN telemechanic.department dep ON e.department_id = dep.id
                   JOIN telemechanic.driving_license dl ON dr.driving_license_id = dl.id
                   JOIN telemechanic.driving_license_category dlc ON dlc.driving_license_id = dl.id
                   JOIN telemechanic.category c ON dlc.category_id = c.id
                   WHERE (CAST(:#{#req.active} AS BOOLEAN) IS NULL          OR dl.active = :#{#req.active})
                     AND (CAST(:#{#req.personnelNumber} AS VARCHAR) IS NULL OR e.personnel_number LIKE '%' || :#{#req.personnelNumber} || '%')
                     AND (CAST(:#{#req.organizationId} AS UUID) IS NULL     OR e.organization_id = :#{#req.organizationId})
                     AND (CAST(:#{#req.departmentId} AS UUID) IS NULL       OR e.department_id = :#{#req.departmentId})
                   GROUP BY e.id, o.id, dep.id, dr.id, dl.id
                   """,
           countQuery = """
                        SELECT COUNT(DISTINCT dr.id)
                        FROM telemechanic.driver dr
                        JOIN telemechanic.employee e ON dr.employee_id = e.id
                        JOIN telemechanic.driving_license dl ON dr.driving_license_id = dl.id
                        WHERE (CAST(:#{#req.active} AS BOOLEAN) IS NULL          OR dl.active = :#{#req.active})
                          AND (CAST(:#{#req.personnelNumber} AS VARCHAR) IS NULL OR e.personnel_number LIKE '%' || :#{#req.personnelNumber} || '%')
                          AND (CAST(:#{#req.organizationId} AS UUID) IS NULL     OR e.organization_id = :#{#req.organizationId})
                          AND (CAST(:#{#req.departmentId} AS UUID) IS NULL       OR e.department_id = :#{#req.departmentId})
                        """, nativeQuery = true)
    Page<DriverProjection> search(DriverSearchRequest req, PageRequest pageRequest);
    
    @Modifying
    @Query(value = """
                   UPDATE telemechanic.driving_license dl
                   SET active = false
                   FROM telemechanic.driver d
                   WHERE d.driving_license_id = dl.id
                   AND dl.expiry_date < CURRENT_DATE
                   """, nativeQuery = true)
    void setActiveFalseWhereExpiryDateBeforeNow();
    
    @Override
    @EntityGraph("search-driver")
    Page<Driver> findAll(Specification<Driver> spec, Pageable pageable);
}
