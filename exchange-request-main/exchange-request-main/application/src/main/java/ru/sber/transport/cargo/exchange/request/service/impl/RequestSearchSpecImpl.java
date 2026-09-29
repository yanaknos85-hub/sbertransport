package ru.sber.transport.cargo.exchange.request.service.impl;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.sber.transport.cargo.exchange.request.database.model.*;
import ru.sber.transport.cargo.exchange.request.dto.MarketplaceRequestFilterDto;
import ru.sber.transport.cargo.exchange.request.dto.ShipperRequestFilterDto;
import ru.sber.transport.cargo.exchange.request.dto.common.FilterComponents;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Collectors;

class RequestSearchSpecImpl {

    private RequestSearchSpecImpl() {
        // private constructor to prevent instantiation
    }

    public static Specification<Request> getPublishedRequestsBySpec(MarketplaceRequestFilterDto searchDto) {
        return (root, query, cb) -> {
            assert query != null;
            query.distinct(true);
            Predicate predicate = cb.isNotNull(root.get(Request_.id));

            predicate = getFromCityPredicate(searchDto, root, query, cb, predicate);

            predicate = getToCityPredicate(searchDto, root, query, cb, predicate);

            predicate = getLoadingDateRangePredicate(searchDto, root, cb, predicate);

            predicate = getWeightRangePredicate(searchDto, root, cb, predicate);

            predicate = getBodyTypesPredicate(searchDto, root, cb, predicate);

            return predicate;
        };
    }

    public static Specification<Request> getShipperRequestsBySpec(ShipperRequestFilterDto searchDto) {
        Specification<Request> spec = (root, query, cb) -> {
            assert query != null;
            query.distinct(true);
            Predicate predicate = cb.isNotNull(root.get(Request_.id));

            if (searchDto.getOrganizationId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get(Request_.ORGANIZATION_ID), searchDto.getOrganizationId()));
            }

            if (searchDto.getHumanreadableId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get(Request_.HUMAN_READABLE_ID), searchDto.getHumanreadableId()));
            }

            if (searchDto.getStatusSet() != null && !searchDto.getStatusSet().isEmpty()) {
                predicate = cb.and(predicate, root.get(Request_.status).in(searchDto.getStatusSet()));
            }

            if (searchDto.getLoadingDateRange() != null) {
                predicate = cb.and(predicate, buildDateRangePredicate(
                        cb, root.get(Request_.LOADING_DATE),
                        searchDto.getLoadingDateRange().getStart(),
                        searchDto.getLoadingDateRange().getEnd()
                ));
            }

            if (searchDto.getDeliveryDateRange() != null) {
                predicate = cb.and(predicate, buildDateRangePredicate(
                        cb, root.get(Request_.DELIVERY_DATE),
                        searchDto.getDeliveryDateRange().getStart(),
                        searchDto.getDeliveryDateRange().getEnd()
                ));
            }

            return predicate;
        };

        if (searchDto.getCreationDateRange() != null) {
            LocalDateTime from = toStartOfDay(searchDto.getCreationDateRange().getStart());
            LocalDateTime to = toEndOfDay(searchDto.getCreationDateRange().getEnd());
            spec = spec.and(dateBetween(Request_.createdAt, from, to));
        }

        return spec;
    }

    private static Predicate getFromCityPredicate(MarketplaceRequestFilterDto searchDto,
                                                  Root<Request> root,
                                                  CriteriaQuery<?> query,
                                                  CriteriaBuilder cb,
                                                  Predicate predicate) {
        if (StringUtils.hasText(searchDto.getFromCity())) {
            Join<Request, Waypoint> fromWaypointJoin = getFirstWaypointJoin(root, query, cb);
            Expression<String> cityExpr = extractCityFromAddressInfo(cb, fromWaypointJoin);
            predicate = cb.and(predicate, cb.equal(
                    cb.lower(cityExpr),
                    searchDto.getFromCity().toLowerCase()
            ));
        }
        return predicate;
    }

    private static Predicate getToCityPredicate(MarketplaceRequestFilterDto searchDto,
                                                Root<Request> root,
                                                CriteriaQuery<?> query,
                                                CriteriaBuilder cb,
                                                Predicate predicate) {
        if (StringUtils.hasText(searchDto.getToCity())) {
            Join<Request, Waypoint> toWaypointJoin = getLastWaypointJoin(root, query, cb);
            Expression<String> cityExpr = extractCityFromAddressInfo(cb, toWaypointJoin);
            predicate = cb.and(predicate, cb.equal(
                    cb.lower(cityExpr),
                    searchDto.getToCity().toLowerCase()
            ));
        }
        return predicate;
    }

    private static Predicate getLoadingDateRangePredicate(MarketplaceRequestFilterDto searchDto,
                                                          Root<Request> root,
                                                          CriteriaBuilder cb,
                                                          Predicate predicate) {
        if (searchDto.getLoadingDateRange() != null) {
            predicate = cb.and(predicate, buildDateRangePredicate(
                    cb, root.get(Request_.LOADING_DATE),
                    searchDto.getLoadingDateRange().getStart(),
                    searchDto.getLoadingDateRange().getEnd()
            ));
        }
        return predicate;
    }

    private static Predicate getWeightRangePredicate(MarketplaceRequestFilterDto searchDto,
                                                     Root<Request> root,
                                                     CriteriaBuilder cb,
                                                     Predicate predicate) {
        if (searchDto.getWeightRange() != null) {
            FilterComponents.BigDecimalRange range = searchDto.getWeightRange();
            Join<Request, CargoDetails> cargoJoin = root.join(Request_.cargoDetails, JoinType.LEFT);
            if (range.getFrom() != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(cargoJoin.get(CargoDetails_.weightKg), range.getFrom())
                );
            }
            if (range.getTo() != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(cargoJoin.get(CargoDetails_.weightKg), range.getTo())
                );
            }
        }
        return predicate;
    }

    private static Predicate getBodyTypesPredicate(MarketplaceRequestFilterDto searchDto,
                                                   Root<Request> root,
                                                   CriteriaBuilder cb,
                                                   Predicate predicate) {
        if (!CollectionUtils.isEmpty(searchDto.getBodyTypes())) {
            Join<Request, VehicleRequirements> vehicleReq =
                    root.join(Request_.vehicleRequirements);

            String pgArray = searchDto.getBodyTypes().stream()
                    .map(v -> "\"" + v + "\"")
                    .collect(Collectors.joining(",", "{", "}"));

            Expression<Boolean> function = cb.function(
                    "jsonb_exists_any",
                    Boolean.class,
                    vehicleReq.get("vehicleBodyType"),
                    cb.literal(pgArray)
            );

            predicate = cb.and(predicate, cb.isTrue(function));
        }
        return predicate;
    }

    private static Join<Request, Waypoint> getFirstWaypointJoin(Root<Request> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Join<Request, Waypoint> join = root.join(Request_.waypoints);
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<Waypoint> subRoot = subquery.from(Waypoint.class);
        subquery.select(cb.min(subRoot.get(Waypoint_.orderingIndex)))
                .where(cb.equal(subRoot.get(Waypoint_.request), root));
        query.where(cb.equal(join.get(Waypoint_.orderingIndex), subquery));
        return join;
    }

    private static Join<Request, Waypoint> getLastWaypointJoin(Root<Request> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Join<Request, Waypoint> join = root.join(Request_.waypoints);
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<Waypoint> subRoot = subquery.from(Waypoint.class);
        subquery.select(cb.max(subRoot.get(Waypoint_.orderingIndex)))
                .where(cb.equal(subRoot.get(Waypoint_.request), root));
        query.where(cb.equal(join.get(Waypoint_.orderingIndex), subquery));
        return join;
    }

    private static Expression<String> extractCityFromAddressInfo(CriteriaBuilder cb, Join<Request, Waypoint> waypointJoin) {
        return cb.function("jsonb_extract_path_text", String.class,
                waypointJoin.get(Waypoint_.addressInfo), cb.literal("city"));
    }

    private static Predicate buildDateRangePredicate(CriteriaBuilder cb, Path<LocalDate> datePath,
                                                     LocalDate start, LocalDate end) {
        Predicate pred = cb.conjunction();

        if (start != null) {
            pred = cb.and(pred, cb.greaterThanOrEqualTo(datePath, start));
        }
        if (end != null) {
            pred = cb.and(pred, cb.lessThanOrEqualTo(datePath, end));
        }

        return pred;
    }

    private static LocalDateTime toStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    private static LocalDateTime toEndOfDay(LocalDate localDate) {
        return localDate != null ? localDate.atTime(LocalTime.MAX) : null;
    }

    private static Specification<Request> dateBetween(SingularAttribute<Request, LocalDateTime> parameter,
                                                     LocalDateTime from, LocalDateTime to) {
        return (root, query, builder) -> {
            if (from != null && to != null) {
                return builder.between(root.get(parameter), from, to);
            } else if (from != null) {
                return builder.greaterThanOrEqualTo(root.get(parameter), from);
            } else if (to != null) {
                return builder.lessThanOrEqualTo(root.get(parameter), to);
            } else {
                return builder.conjunction(); // true
            }
        };
    }
}
