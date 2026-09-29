package ru.sber.transport.cargo.exchange.request.service.impl;

import jakarta.persistence.criteria.*;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.sber.transport.cargo.exchange.request.database.model.*;
import ru.sber.transport.cargo.exchange.request.dto.CarrierRequestFilterDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
public class RequestSearchForCarrierSpecImpl {

    public static Specification<Request> getRequestSearchForCarrierBySpec(
            final CarrierRequestFilterDto searchDto,
            UUID carrierOrganizationId) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            // Подзапрос: выбираем заявки, на которые есть отклик от организации
            Subquery<Request> subquery = query.subquery(Request.class);
            Root<CarrierReply> replyRoot = subquery.from(CarrierReply.class);
            subquery.select(replyRoot.get(CarrierReply_.REQUEST))
                    .where(criteriaBuilder.equal(
                            replyRoot.get(CarrierReply_.ORGANIZATION).get("id"), // ← организация.id
                            carrierOrganizationId
                    ));
            // Основной запрос: либо организация в отклике, либо назначена напрямую
            Predicate repliedByOrg = criteriaBuilder.in(root).value(subquery);
            Predicate assignedToOrg = criteriaBuilder.equal(root.get(Request_.CARRIER_ORGANIZATION_ID), carrierOrganizationId);
            predicates.add(criteriaBuilder.or(repliedByOrg, assignedToOrg));

            // === Фильтр по городу погрузки ===
            if (StringUtils.hasText(searchDto.getAddressFrom())) {
                Join<Request, Waypoint> fromWaypointJoin = getFirstWaypointJoin(root, query, criteriaBuilder);
                Expression<String> cityExpr = extractCityFromAddressInfo(criteriaBuilder, fromWaypointJoin);
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(cityExpr),
                        searchDto.getAddressFrom().toLowerCase()
                ));
            }

            // === Фильтр по городу выгрузки ===
            if (StringUtils.hasText(searchDto.getAddressTo())) {
                Join<Request, Waypoint> toWaypointJoin = getLastWaypointJoin(root, query, criteriaBuilder);
                Expression<String> cityExpr = extractCityFromAddressInfo(criteriaBuilder, toWaypointJoin);
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(cityExpr),
                        searchDto.getAddressTo().toLowerCase()
                ));
            }

            // === Фильтр по диапазону даты погрузки (используем поле loadingDate) ===
            if (searchDto.getLoadingDateRange() != null) {
                LocalDate start = searchDto.getLoadingDateRange().getStart();
                LocalDate end = searchDto.getLoadingDateRange().getEnd();

                if (start != null || end != null) {
                    Expression<LocalDate> dateExpr = root.get(Request_.LOADING_DATE);
                    List<Predicate> datePredicates = new ArrayList<>();

                    if (start != null) {
                        datePredicates.add(criteriaBuilder.greaterThanOrEqualTo(dateExpr, start));
                    }
                    if (end != null) {
                        datePredicates.add(criteriaBuilder.lessThanOrEqualTo(dateExpr, end));
                    }

                    predicates.add(criteriaBuilder.and(datePredicates.toArray(new Predicate[0])));
                }
            }

            // === Фильтр по диапазону даты доставки (используем поле deliveryDate) ===
            if (searchDto.getDeliveryDateRange() != null) {
                LocalDate start = searchDto.getDeliveryDateRange().getStart();
                LocalDate end = searchDto.getDeliveryDateRange().getEnd();

                if (start != null || end != null) {
                    Expression<LocalDate> dateExpr = root.get(Request_.DELIVERY_DATE);
                    List<Predicate> datePredicates = new ArrayList<>();

                    if (start != null) {
                        datePredicates.add(criteriaBuilder.greaterThanOrEqualTo(dateExpr, start));
                    }
                    if (end != null) {
                        datePredicates.add(criteriaBuilder.lessThanOrEqualTo(dateExpr, end));
                    }

                    predicates.add(criteriaBuilder.and(datePredicates.toArray(new Predicate[0])));
                }
            }

            // === Фильтр по статусам ===
            if (searchDto.getStatusSet() != null && !searchDto.getStatusSet().isEmpty()) {
                List<Predicate> statusPredicates = searchDto.getStatusSet().stream()
                        .filter(StringUtils::hasText)
                        .map(status -> criteriaBuilder.equal(root.get(Request_.STATUS), status.trim()))
                        .toList();
                predicates.add(criteriaBuilder.or(statusPredicates.toArray(new Predicate[0])));
            }

            // Объединяем все предикаты через AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Join<Request, Waypoint> getFirstWaypointJoin(Root<Request> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Join<Request, Waypoint> waypointJoin = root.join(Request_.WAYPOINTS, JoinType.LEFT);
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<Waypoint> subRoot = subquery.from(Waypoint.class);
        subquery.select(cb.min(subRoot.get(Waypoint_.ORDERING_INDEX)))
                .where(cb.equal(subRoot.get(Waypoint_.REQUEST), root));
        return waypointJoin.on(cb.equal(waypointJoin.get(Waypoint_.ORDERING_INDEX), subquery));
    }

    private static Join<Request, Waypoint> getLastWaypointJoin(Root<Request> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Join<Request, Waypoint> waypointJoin = root.join(Request_.WAYPOINTS, JoinType.LEFT);
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<Waypoint> subRoot = subquery.from(Waypoint.class);
        subquery.select(cb.max(subRoot.get(Waypoint_.ORDERING_INDEX)))
                .where(cb.equal(subRoot.get(Waypoint_.REQUEST), root));
        return waypointJoin.on(cb.equal(waypointJoin.get(Waypoint_.ORDERING_INDEX), subquery));
    }

    private static Expression<String> extractCityFromAddressInfo(CriteriaBuilder cb, Join<Request, Waypoint> waypointJoin) {
        return cb.function("jsonb_extract_path_text", String.class,
                waypointJoin.get("addressInfo"),
                cb.literal("city"));
    }
}