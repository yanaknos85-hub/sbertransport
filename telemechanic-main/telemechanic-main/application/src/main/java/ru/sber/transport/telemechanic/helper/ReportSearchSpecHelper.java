package ru.sber.transport.telemechanic.helper;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.ReportSearchDto;

import java.time.temporal.ChronoUnit;
import java.util.Objects;


@UtilityClass
public class ReportSearchSpecHelper {

    public Specification<Request> getSpecification(ReportSearchDto searchDto) {
        return (root, query, builder) -> {
            var predicate = builder.isNotNull(root.get(Request_.id));
            if (StringUtils.hasText(searchDto.getPersonnelNumber())) {
                predicate = builder.and(predicate,
                        builder.equal(root.get(Request_.AUTHOR).get(Employee_.PERSONNEL_NUMBER),
                                searchDto.getPersonnelNumber()));
            }
            if (StringUtils.hasText(searchDto.getHumanReadableId())) {
                predicate = builder.and(predicate, builder.like(builder.lower(root.get(Request_.HUMAN_READABLE_ID)),
                        "%" + searchDto.getHumanReadableId().toLowerCase()
                                .replace("о", "o")
                                .replace("т", "t") + "%"));
            }
            if (searchDto.getOrganizationId() != null) {
                predicate = builder.and(predicate, builder.equal(root
                                .get(Request_.AUTHOR).get(Employee_.ORGANIZATION).get(Organization_.ID),
                        searchDto.getOrganizationId()));
            }
            if (searchDto.getPeriod() != null) {
                predicate = builder.and(predicate, builder.between(root.get(Request_.CREATION_TIME),
                                                                   searchDto.getPeriod().getStart().truncatedTo(ChronoUnit.MINUTES),
                                                                   searchDto.getPeriod().getEnd().truncatedTo(ChronoUnit.MINUTES)));
            }
            if (Objects.nonNull(searchDto.getDepartmentIds()) && !searchDto.getDepartmentIds().isEmpty()) {
                predicate = builder.and(predicate, builder.in(
                                                root.get(Request_.AUTHOR)
                                                    .get(Employee_.DEPARTMENT)
                                                    .get(Department_.ID)).value(searchDto.getDepartmentIds())
                                       );
            }
            return predicate;
        };
    }
}
