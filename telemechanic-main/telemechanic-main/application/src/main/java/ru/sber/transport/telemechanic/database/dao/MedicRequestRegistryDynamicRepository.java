package ru.sber.transport.telemechanic.database.dao;

import lombok.RequiredArgsConstructor;
import org.jooq.Record;
import org.jooq.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestSearchDto;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestSearchResponse;
import ru.sber.transport.telemechanic.enumerate.MedicRequestSortOption;
import ru.sber.transport.telemechanic.mapper.MedicRequestRegistryMapper;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

@Repository
@RequiredArgsConstructor
public class MedicRequestRegistryDynamicRepository {
    
    private final DSLContext dslContext;
    
    public MedicRequestSearchResponse findMedicRequestRegistry(
            PageRequest pageRequest,
            MedicRequestSearchDto searchDto
                                                              ) {
        var query = createQuery(searchDto);
        var total = dslContext.fetchCount(query);
        var orderedQuery = order(query, pageRequest);
        var medicRequests = joinLimitAndOffset(orderedQuery, pageRequest).fetch();
        return new MedicRequestSearchResponse(MedicRequestRegistryMapper.recordToRegistryResponse(medicRequests, searchDto.fieldSet()), total);
    }
    
    public Result<Record> findMedicRequestExcel(
            MedicRequestSearchDto searchDto
                                               ) {
        var query = createQuery(searchDto);
        var orderedQuery = order(query, null);
        return orderedQuery.fetch();
    }
    
    private SelectConditionStep<Record> createQuery(
            MedicRequestSearchDto request
                                                   ) {
        var fields = request.fieldSet().stream()
                            .map(column -> field(column.getSqlViewFieldName()).as(column.getAlias()))
                            .collect(Collectors.toList());
        fields.add(field("e1_0.id").as("ewbId"));
        
        var query = dslContext
                .select(fields)
                .from(table("telemechanic.ewb").as("e1_0"))
                .join(table("telemechanic.medic_request").as("m1_0")).on(field("e1_0.medic_request_id").eq(field("m1_0.id")))
                .leftJoin(table("telemechanic.employee").as("a1_0")).on(field("e1_0.medic_id").eq(field("a1_0.user_id")))
                .leftJoin(table("telemechanic.medic_contractor").as("m2_0")).on(field("e1_0.medic_contractor_id").eq(field("m2_0.id")))
                .leftJoin(table("telemechanic.organization").as("o1_0")).on(field("a1_0.organization_id").eq(field("o1_0.id")))
                .leftJoin(table("telemechanic.department").as("d1_0")).on(field("a1_0.department_id").eq(field("d1_0.id")))
                .join(table("telemechanic.organization_medical_license").as("b1_0")).on(field("e1_0.organization_medical_license_id").eq(field("b1_0.id")))
                .join(table("telemechanic.driver").as("dr1_0")).on(field("e1_0.driver_id").eq(field("dr1_0.id")))
                .join(table("telemechanic.employee").as("a2_0")).on(field("dr1_0.employee_id").eq(field("a2_0.id")))
                .join(table("telemechanic.organization").as("o2_0")).on(field("a2_0.organization_id").eq(field("o2_0.id")))
                .join(table("telemechanic.department").as("d2_0")).on(field("a2_0.department_id").eq(field("d2_0.id")))
                .join(table("telemechanic.driving_license").as("c1_0")).on(field("e1_0.driver_license_id").eq(field("c1_0.id")));
        
        return joinConditions(query, request);
    }
    
    private SelectConditionStep<Record> joinConditions(
            SelectOnConditionStep<Record> query,
            MedicRequestSearchDto searchDto
                                                      ) {
        return query.where(humanReadableIdCondition(searchDto.searchText()))
                    .and(personnelNumberCondition(searchDto.personnelNumber()))
                    .and(humanReadableIdCondition(searchDto.humanReadableId()))
                    .and(organizationCondition(searchDto.organizationId()))
                    .and(departmentCondition(searchDto.departmentIdSet()))
                    .and(periodCondition(searchDto.period()));
    }
    
    private Condition personnelNumberCondition(String personnelNumber) {
        if (personnelNumber == null) {
            return noCondition();
        }
        
        return field("a1_0.personnel_number").eq(personnelNumber);
    }
    
    private Condition humanReadableIdCondition(String humanReadableId) {
        if (humanReadableId == null) {
            return noCondition();
        }
        
        return field("m1_0.human_readable_id").likeIgnoreCase("%" + humanReadableId + "%");
    }
    
    private Condition organizationCondition(UUID organizationId) {
        if (organizationId == null) {
            return noCondition();
        }
        return field("o1_0.id").eq(organizationId);
    }
    
    private Condition departmentCondition(Set<UUID> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return noCondition();
        }
        return field("d1_0.id").in(departmentIds);
    }
    
    private Condition periodCondition(DateRange period) {
        if (period == null || (period.start() == null && period.end() == null)) {
            return noCondition();
        }
        return field("m1_0.creation_time").between(period.start()).and(period.end());
    }
    
    private SelectSeekStepN<Record> order(SelectConditionStep<Record> query, PageRequest pageRequest) {
        if (pageRequest == null) {
            var orderFields = getOrderFields(Sort.by(
                    Sort.Direction.ASC,
                    MedicRequestSortOption.MEDIC_REQUEST_HUMAN_READABLE_ID.getSqlValue(),
                    MedicRequestSortOption.EWB_ID.getSqlValue()));
            return query.orderBy(orderFields);
        }
        var orderFields = getOrderFields(pageRequest.getSort());
        return query.orderBy(orderFields);
    }
    
    private List<SortField<Object>> getOrderFields(Sort sort) {
        return sort.stream()
                   .map(order -> {
                       var field = field(order.getProperty());
                       return order.isAscending() ? field.asc() : field.desc();
                   }).toList();
    }
    
    private SelectForUpdateStep<Record> joinLimitAndOffset(SelectSeekStepN<Record> query, PageRequest pageRequest) {
        return query.limit(pageRequest.getPageSize())
                    .offset(pageRequest.getPageSize() * pageRequest.getPageNumber());
    }
}