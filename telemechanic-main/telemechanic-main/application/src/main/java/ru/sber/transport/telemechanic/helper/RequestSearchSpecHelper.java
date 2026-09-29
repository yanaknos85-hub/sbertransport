package ru.sber.transport.telemechanic.helper;

import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.RequestSearchDto;

import java.util.Objects;
import java.util.UUID;

@UtilityClass
public class RequestSearchSpecHelper {

    public Specification<Request> getSpecification(UUID userOrganizationId, RequestSearchDto searchDto) {
        return (root, query, builder) -> {
            var predicate = builder.isTrue(builder.literal(true));
            if (userOrganizationId != null) {
                predicate = builder.and(predicate, builder.equal(root.get(Request_.ORGANIZATION_ID), userOrganizationId));
            }
            if (searchDto.requestStatusSet() != null && !searchDto.requestStatusSet().isEmpty()) {
                predicate = builder.and(predicate, root.get(Request_.status).in(searchDto.requestStatusSet()));
            }
            if (StringUtils.hasText(searchDto.searchText())) {
                predicate = builder.and(predicate, builder.or(builder.like(builder.lower(root.get(Request_.HUMAN_READABLE_ID)),
                                "%" + searchDto.searchText().toLowerCase()
                                        .replace("м", "m")
                                        .replace("т", "t") + "%"),
                        builder.like(builder.lower(root.get(Request_.AUTHOR).get(Employee_.LAST_NAME)),
                                "%" + searchDto.searchText().toLowerCase() + "%"),
                        builder.like(builder.lower(root.get(Request_.TRANSPORT).get(Transport_.STATE_NUMBER)),
                                "%" + TransliterationHelper.transliterateNumber(searchDto.searchText())
                                        .toLowerCase() + "%")));
            }
            if (Objects.nonNull(searchDto.organizationId())) {
                predicate = builder
                        .and(predicate, builder.equal(
                                root.get(Request_.AUTHOR).get(Employee_.ORGANIZATION).get(Organization_.ID),
                                searchDto.organizationId()));
            }
            if (CollectionUtils.isNotEmpty(searchDto.departmentIds())) {
                predicate = builder
                        .and(predicate, root.get(Request_.AUTHOR).get(Employee_.DEPARTMENT).get(Department_.ID)
                        .in(searchDto.departmentIds()));
            }
            return predicate;
        };
    }
}
