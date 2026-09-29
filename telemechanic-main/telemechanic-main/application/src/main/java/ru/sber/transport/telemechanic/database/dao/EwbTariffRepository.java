package ru.sber.transport.telemechanic.database.dao;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.database.model.EwbTariff;
import ru.sber.transport.telemechanic.database.model.EwbTariff_;
import ru.sber.transport.telemechanic.database.projection.EwbContractDetailsProjection;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Репозиторий тарифов ЭПЛ
 */
@Repository
public interface EwbTariffRepository extends JpaRepository<EwbTariff, UUID> {
    
    boolean existsByOrganizationIdAndActiveTrue(UUID organizationId);

    @NotNull
    @Override
    @EntityGraph(attributePaths = { EwbTariff_.CONTRACT })
    List<EwbTariff> findAll();
    
    /**
     * Получаем активные тарифы по списку идентификаторов договоров
     *
     * @param contractIdList список идентификаторов договоров
     *
     * @return {@link List<EwbTariff>}
     */
    @EntityGraph(attributePaths = { EwbTariff_.CONTRACT })
    List<EwbTariff> findAllByActiveIsTrueAndContractContractIdIn(List<UUID> contractIdList);
    
    @Query(
            value = """
                    WITH RECURSIVE department_hierarchy AS (
                        SELECT
                            d.id,
                            d.parent_id,
                            d.organization_id,
                            t.contract_id,
                            c.inspection_type,
                            c.organization_id AS contract_org_id,
                            CASE WHEN c.inspection_type = 'TECHNIC' AND t.active = true AND c.active = true THEN true ELSE false END AS is_technic_found
                        FROM
                            telemechanic.department d
                        LEFT JOIN
                            telemechanic.ewb_tariff t ON t.department_id = d.id AND t.active = true
                        LEFT JOIN
                            telemechanic.ewb_contract c ON t.contract_id = c.contract_id AND c.active = true
                        WHERE
                            d.id = :departmentId
                        UNION ALL
                        SELECT
                            parent.id,
                            parent.parent_id,
                            parent.organization_id,
                            t.contract_id,
                            c.inspection_type,
                            c.organization_id AS contract_org_id,
                            CASE WHEN c.inspection_type = 'TECHNIC' AND t.active = true AND c.active = true THEN true ELSE false END AS is_technic_found
                        FROM
                            telemechanic.department parent
                        JOIN
                            department_hierarchy child ON parent.id = child.parent_id
                        LEFT JOIN
                            telemechanic.ewb_tariff t ON t.department_id = parent.id AND t.active = true
                        LEFT JOIN
                            telemechanic.ewb_contract c ON t.contract_id = c.contract_id AND c.active = true
                        WHERE
                            NOT child.is_technic_found
                    )
                    SELECT
                        contract_org_id AS organization_id
                    FROM
                        department_hierarchy
                    WHERE
                        inspection_type = 'TECHNIC' AND is_technic_found = true
                    LIMIT 1
                    """,
             nativeQuery = true
    )
    UUID findContractOrganizationIdByTariffDepartmentIdWithRecursiveHierarchy(UUID departmentId);
    
    @Query(
            value =
            """
            WITH RECURSIVE department_hierarchy AS (
                SELECT
                    d.id,
                    d.parent_id,
                    d.organization_id,
                    t.contract_id,
                    c.inspection_type,
                    c.start as contract_start,
                    c.end as contract_end,
                    c.organization_id AS contract_org_id,
                    CASE WHEN c.inspection_type = 'TECHNIC' AND t.active = true AND c.active = true THEN true ELSE false END AS is_technic_found,
                    CASE WHEN c.inspection_type IN :types AND t.active = true AND c.active = true THEN true ELSE false END AS is_types_found
                FROM
                    telemechanic.department d
                LEFT JOIN
                    telemechanic.ewb_tariff t ON t.department_id = d.id AND t.active = true
                LEFT JOIN
                    telemechanic.ewb_contract c ON t.contract_id = c.contract_id AND c.active = true
                WHERE
                    d.id = :departmentId
                UNION ALL
                SELECT
                    parent.id,
                    parent.parent_id,
                    parent.organization_id,
                    t.contract_id,
                    c.inspection_type,
                    c.start as contract_start,
                    c.end as contract_end,
                    c.organization_id AS contract_org_id,
                    CASE WHEN c.inspection_type = 'TECHNIC' AND t.active = true AND c.active = true THEN true ELSE false END AS is_technic_found,
                    CASE WHEN c.inspection_type IN :types AND t.active = true AND c.active = true THEN true ELSE false END AS is_types_found
                FROM
                    telemechanic.department parent
                JOIN
                    department_hierarchy child ON parent.id = child.parent_id
                LEFT JOIN
                    telemechanic.ewb_tariff t ON t.department_id = parent.id AND t.active = true
                LEFT JOIN
                    telemechanic.ewb_contract c ON t.contract_id = c.contract_id AND c.active = true
                WHERE
                    NOT child.is_technic_found
            )
            SELECT
                inspection_type, contract_start, contract_end
            FROM
                department_hierarchy
            WHERE
                inspection_type IN :types AND is_types_found = true
            GROUP BY inspection_type, contract_start, contract_end
            """,
            nativeQuery = true
    )
    Set<EwbContractDetailsProjection> findContractsWithDetailsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(UUID departmentId, Set<String> types);


    @Query(
            value = """
                    WITH RECURSIVE department_hierarchy AS (
                        SELECT
                            d.id,
                            d.parent_id,
                            d.organization_id,
                            t.contract_id,
                            c.inspection_type,
                            c.start as contract_start,
                            c.end as contract_end,
                            c.organization_id AS contract_org_id,
                            c.edf_operator_id,
                            c.edf_code,
                            c.organization_medical_license_id,
                            c.active,
                            CASE WHEN c.inspection_type = 'TECHNIC' AND t.active = true AND c.active = true THEN true ELSE false END AS is_technic_found,
                            CASE WHEN c.inspection_type IN ('TECHNIC', 'MEDIC', 'TELEMEDIC') AND t.active = true AND c.active = true THEN true ELSE false END AS is_types_found
                        FROM
                            telemechanic.department d
                        LEFT JOIN
                            telemechanic.ewb_tariff t ON t.department_id = d.id AND t.active = true
                        LEFT JOIN
                            telemechanic.ewb_contract c ON t.contract_id = c.contract_id AND c.active = true
                        WHERE
                            d.id = :departmentId
                        UNION ALL
                        SELECT
                            parent.id,
                            parent.parent_id,
                            parent.organization_id,
                            t.contract_id,
                            c.inspection_type,
                            c.start as contract_start,
                            c.end as contract_end,
                            c.organization_id AS contract_org_id,
                            c.edf_operator_id,
                            c.edf_code,
                            c.organization_medical_license_id,
                            c.active,
                            CASE WHEN c.inspection_type = 'TECHNIC' AND t.active = true AND c.active = true THEN true ELSE false END AS is_technic_found,
                            CASE WHEN c.inspection_type IN ('TECHNIC', 'MEDIC', 'TELEMEDIC') AND t.active = true AND c.active = true THEN true ELSE false END AS is_types_found
                        FROM
                            telemechanic.department parent
                        JOIN
                            department_hierarchy child ON parent.id = child.parent_id
                        LEFT JOIN
                            telemechanic.ewb_tariff t ON t.department_id = parent.id AND t.active = true
                        LEFT JOIN
                            telemechanic.ewb_contract c ON t.contract_id = c.contract_id AND c.active = true
                        WHERE
                            NOT child.is_technic_found
                    )
                    SELECT
                            contract_id,
                            contract_org_id as organization_id,
                            inspection_type,
                            edf_operator_id,
                            edf_code,
                            organization_medical_license_id,
                            active,
                            contract_start as start,
                            contract_end as "end"
                    FROM
                        department_hierarchy
                    WHERE
                        inspection_type IN ('TECHNIC', 'MEDIC', 'TELEMEDIC') AND is_types_found = true
                            GROUP BY
                                contract_id,
                                contract_org_id,
                                inspection_type,
                                edf_operator_id,
                                edf_code,
                                organization_medical_license_id,
                                active,
                                contract_start,
                                contract_end
                    """,
            nativeQuery = true
    )
    Set<EwbContract> findContractsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(UUID departmentId);
}
