package ru.sber.transport.telemechanic.database.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbSearchDto;
import ru.sber.transport.telemechanic.enumerate.EwbRegistryField;
import ru.sber.transport.telemechanic.mapper.EwbRegistryMapper;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static ru.sber.transport.telemechanic.enumerate.EwbRegistryField.*;

@Repository
@RequiredArgsConstructor
public class EwbRegistryDynamicRepository {
    
    private final DSLContext dslContext;
    
    private static final String ORGANIZATION = "telemechanic.organization";
    private static final String DEPARTMENT = "telemechanic.department";
    private static final String DRIVER = "telemechanic.driver";
    private static final String EMPLOYEE = "telemechanic.employee";
    private static final String ORG_ID = "org.id";
    private static final String DEP_ID = "dep.id";
    
    public EwbSearchDto findEwbRegistry(
            PageRequest pageRequest,
            Set<EwbRegistryField> columns,
            String searchText,
            String humanReadableId,
            UUID organizationId,
            Set<UUID> departmentIds,
            DateRange dateRange
                                       ) {
        
        var query = createQuery(columns, searchText, humanReadableId, organizationId, departmentIds, dateRange);
        var total = dslContext.fetchCount(query);
        var orderedQuery = order(query, pageRequest);
        var ewbs = joinLimitAndOffset(orderedQuery, pageRequest).fetch();
        return new EwbSearchDto(EwbRegistryMapper.recordToEwbRegistryResponse(ewbs), total);
    }
    
    public Result<Record> findEwbRegistryExcel(
            Set<EwbRegistryField> columns,
            String searchText,
            String humanReadableId,
            UUID organizationId,
            Set<UUID> departmentIds,
            DateRange dateRange
                                              ) {
        var query = createQuery(columns, searchText, humanReadableId, organizationId, departmentIds, dateRange);
        return order(query, null).fetch();
    }
    
    private SelectConditionStep<Record> createQuery(
            Set<EwbRegistryField> columns,
            String searchText,
            String humanReadableId,
            UUID organizationId,
            Set<UUID> departmentIds,
            DateRange dateRange
                                                   ) {
        var fields = columns.stream()
                            .map(column -> field(column.getSqlViewFieldName()).as(column.getAlias()))
                            .collect(Collectors.toList());
        fields.add(field("mr.status").as("medicRequestStatus"));
        fields.add(field("r.status").as("requestStatus"));
        
        var query = dslContext
                .select(fields)
                .from(table("telemechanic.ewb").as("ewb"))
                .join(table("telemechanic.transport").as("tr")).on(field("ewb.transport_id").eq(field("tr.id")))
                .join(table(DRIVER).as("dr")).on(field("ewb.driver_id").eq(field("dr.id")))
                .join(table(EMPLOYEE).as("emp")).on(field("dr.employee_id").eq(field("emp.id")))
                .join(DSL.table(ORGANIZATION).as("org")).on(DSL.field("emp.organization_id").eq(DSL.field(ORG_ID)))
                .join(table(DEPARTMENT).as("dep")).on(field("emp.department_id").eq(field(DEP_ID)))
                .leftJoin(table("telemechanic.medic_request").as("mr")).on(field("ewb.medic_request_id").eq(field("mr.id")))
                .leftJoin(table("telemechanic.request").as("r")).on(field("ewb.request_id").eq(field("r.id")));
        var queryWithExtraJoins = joinTables(query, columns);
        return joinConditions(queryWithExtraJoins, searchText, humanReadableId, organizationId, departmentIds, dateRange);
    }
    
    private List<SortField<Object>> getOrderFields(Sort sort) {
        return sort.stream()
                   .map(order -> {
                       Field<Object> field = field(order.getProperty());
                       return order.isAscending() ? field.asc() : field.desc();
                   }).toList();
    }
    
    private Condition humanReadableIdCondition(String humanReadableId) {
        if (humanReadableId == null) {
            return DSL.noCondition();
        }
        return field("ewb.human_readable_id").likeIgnoreCase("%" + humanReadableId + "%");
    }
    
    private Condition organizationIdCondition(UUID organizationId) {
        if (organizationId == null) {
            return DSL.noCondition();
        }
        return field(ORG_ID).eq(organizationId);
    }
    
    private Condition departmentIdsCondition(Set<UUID> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return DSL.noCondition();
        }
        return field(DEP_ID).in(departmentIds);
    }
    
    private Condition dateRangeCondition(DateRange dateRange) {
        if (dateRange == null) {
            return DSL.noCondition();
        }
        return field("ewb.creation_time").between(dateRange.start()).and(dateRange.end());
    }
    
    private SelectSeekStepN<Record> order(SelectConditionStep<Record> query, PageRequest pageRequest) {
        if (pageRequest == null) {
            var orderFields = getOrderFields(Sort.by(Sort.Direction.ASC, "ewb.human_readable_id"));
            return query.orderBy(orderFields);
        }
        var orderFields = getOrderFields(pageRequest.getSort());
        return query.orderBy(orderFields);
    }
    
    private SelectForUpdateStep<Record> joinLimitAndOffset(SelectSeekStepN<Record> query, PageRequest pageRequest) {
        if (pageRequest == null) {
            return query;
        }
        return query.limit(pageRequest.getPageSize())
                    .offset(pageRequest.getPageSize() * pageRequest.getPageNumber());
    }
    
    private SelectJoinStep<Record> joinTables(SelectJoinStep<Record> query, Set<EwbRegistryField> columns) {
        query = joinAuthorTable(query, columns);
        query = joinMedicTable(query, columns);
        query = joinTelemechOutTable(query, columns);
        query = joinTelemechInTable(query, columns);
        query = joinDrivingLicenseTable(query, columns);
        query = joinAttorneyTable(query, columns);
        
        return query;
    }
    
    private SelectConditionStep<Record> joinConditions(
            SelectJoinStep<Record> query,
            String searchText,
            String humanReadableId,
            UUID organizationId,
            Set<UUID> departmentIds,
            DateRange dateRange
                                                      ) {
        return query.where(humanReadableIdCondition(searchText))
                    .and(humanReadableIdCondition(humanReadableId))
                    .and(organizationIdCondition(organizationId))
                    .and(departmentIdsCondition(departmentIds))
                    .and(dateRangeCondition(dateRange));
    }
    
    private SelectJoinStep<Record> joinAuthorTable(SelectJoinStep<Record> query, Set<EwbRegistryField> columns) {
        if (checkAuthorColumns(columns)) {
            return query.join(table(EMPLOYEE).as("auth")).on(field("ewb.author_id").eq(field("auth.id")));
        } else {
            return query;
        }
    }
    
    private SelectJoinStep<Record> joinMedicTable(SelectJoinStep<Record> query, Set<EwbRegistryField> columns) {
        if (checkMedicColumns(columns)) {
            return query.leftJoin(table(EMPLOYEE).as("med")).on(field("ewb.medic_id").eq(field("med.id")))
                        .leftJoin(table("telemechanic.medic_contractor").as("med_con")).on(field("ewb.medic_contractor_id").eq(field("med_con.id")))
                        .leftJoin(table(ORGANIZATION).as("med_org")).on(field("med.organization_id").eq(field("med_org.id")));
        } else {
            return query;
        }
    }
    
    private SelectJoinStep<Record> joinTelemechOutTable(SelectJoinStep<Record> query, Set<EwbRegistryField> columns) {
        if (checkTelemechOutColumns(columns)) {
            return query.leftJoin(table(EMPLOYEE).as("tm_out")).on(field("ewb.telemech_out_id").eq(field("tm_out.id")))
                        .leftJoin(table(ORGANIZATION).as("tm_out_org")).on(field("tm_out.organization_id").eq(field("tm_out_org.id")))
                        .leftJoin(table(DEPARTMENT).as("tm_out_dep")).on(field("tm_out.department_id").eq(field("tm_out_dep.id")));
        } else {
            return query;
        }
    }
    
    private SelectJoinStep<Record> joinTelemechInTable(SelectJoinStep<Record> query, Set<EwbRegistryField> columns) {
        if (checkTelemechInColumns(columns)) {
            return query.leftJoin(table(EMPLOYEE).as("tm_in")).on(field("ewb.telemech_in_id").eq(field("tm_in.id")));
        } else {
            return query;
        }
    }
    
    private SelectJoinStep<Record> joinAttorneyTable(SelectJoinStep<Record> query, Set<EwbRegistryField> columns) {
        if (checkAttorneyColumns(columns)) {
            return query.leftJoin(table("telemechanic.attorney").as("att")).on(field("ewb.attorney_out_id").eq(field("att.id")));
        } else {
            return query;
        }
    }
    
    private SelectJoinStep<Record> joinDrivingLicenseTable(SelectJoinStep<Record> query, Set<EwbRegistryField> columns) {
        if (checkDrivingLicenseColumns(columns)) {
            return query.leftJoin(table("telemechanic.driving_license").as("dl")).on(field("ewb.driver_license_id").eq(field("dl.id")));
        } else {
            return query;
        }
    }
    
    private boolean checkAuthorColumns(Set<EwbRegistryField> fields) {
        var authorColumns = Set.of(AUTHOR_FULL_NAME, AUTHOR_PERSONNEL_NUMBER);
        return fields.stream()
                     .anyMatch(authorColumns::contains);
    }
    
    private boolean checkMedicColumns(Set<EwbRegistryField> fields) {
        var medicColumns = Set.of(MEDIC_FULL_NAME, MEDIC_PERSONNEL_NUMBER, MEDIC_ORGANIZATION_NAME);
        return fields.stream()
                     .anyMatch(medicColumns::contains);
    }
    
    private boolean checkTelemechOutColumns(Set<EwbRegistryField> fields) {
        var telemechOutColumns = Set.of(TELEMECH_OUT_FULL_NAME, TELEMECH_OUT_PERSONNEL_NUMBER, TELEMECH_OUT_ORGANIZATION_NAME, TELEMECH_OUT_DEPARTMENT_NAME);
        return fields.stream()
                     .anyMatch(telemechOutColumns::contains);
    }
    
    private boolean checkTelemechInColumns(Set<EwbRegistryField> fields) {
        var telemechInColumns = Set.of(TELEMECH_IN_FULL_NAME, TELEMECH_IN_PERSONNEL_NUMBER);
        return fields.stream()
                     .anyMatch(telemechInColumns::contains);
    }
    
    private boolean checkAttorneyColumns(Set<EwbRegistryField> fields) {
        var attorneyColumns = Set.of(ATTORNEY_NUMBER, ATTORNEY_ISSUE_DATE, ATTORNEY_CREATION_SYSTEM);
        return fields.stream()
                     .anyMatch(attorneyColumns::contains);
    }
    
    private boolean checkDrivingLicenseColumns(Set<EwbRegistryField> fields) {
        var drivingLicenseColumns = Set.of(DRIVING_LICENSE_SERIES, DRIVING_LICENSE_NUMBER, DRIVING_LICENSE_ISSUE_DATE, DRIVING_LICENSE_EXPIRY_DATE);
        return fields.stream()
                     .anyMatch(drivingLicenseColumns::contains);
    }
}
