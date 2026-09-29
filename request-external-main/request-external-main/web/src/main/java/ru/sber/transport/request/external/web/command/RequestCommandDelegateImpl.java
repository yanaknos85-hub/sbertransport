package ru.sber.transport.request.external.web.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.business.providers.TripOrdersMetaProvider;
import ru.sber.transport.request.external.business.TripOrdersService;
import ru.sber.transport.request.external.model.Tariff;
import ru.sber.transport.request.external.model.triporder.TripOrderCreateDTO;
import ru.sber.transport.request.external.model.waypoint.WaypointDTO;
import ru.sber.transport.request.external.web.model.ListResponseTripOrderData;
import ru.sber.transport.request.external.web.model.WebRequestTripOrderData;
import ru.sber.transport.web.api.ExternalRequestCommandApi;
import ru.sber.transport.web.model.EditExternalRequest;
import ru.sber.transport.web.model.ListExternalRequest;
import ru.sber.transport.web.model.NewExternalRequest;
import ru.sber.transport.web.model.Patch;
import ru.sberbank.utils.reflection.ReflectionUtils;

/**
 * Реализация сервиса для работы с заявками на поездку
 */
@Slf4j
@RequiredArgsConstructor
public class RequestCommandDelegateImpl implements ExternalRequestCommandApi {

    private final TripOrdersService tripOrdersService;

    private final TripOrdersMetaProvider tripOrdersMetaProvider;

    private final EmployeeOrganizationFunction employeeOrganizationFunction;

    private final ObjectMapper objectMapper;

    @Override
    public CompletableFuture<ResponseEntity<ListExternalRequest>> add(NewExternalRequest source) {
        final var user = ControllerUtils.currentUser();
        final var employeeOrganization = getEmployeeOrganization();
        final var context = SecurityContextHolder.getContext();
        return CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.setContext(context);
            final var waypoints = source
                    .getWaypoints()
                    .stream()
                    .map(wP -> new WaypointDTO(null,
                            wP.getCountry(),
                            wP.getRegion(),
                            wP.getCity(),
                            wP.getStreet(),
                            wP.getHouse(),
                            wP.getBuilding(),
                            wP.getStructure(),
                            wP.getLatitude(),
                            wP.getLongitude())
                    )
                    .toList();
            final var tariff = Tariff.valueOf(source.getTariff().name());
            final var tripOrder = new TripOrderCreateDTO(
                    source.getTripDate(),
                    waypoints,
                    source.getPurposeId(),
                    tariff, source.getComment(),
                    source.getTaxiCost()
            );

            final var result = tripOrdersService.create(user, tripOrder);
            final var meta = tripOrdersMetaProvider.meta(employeeOrganization, result.getId());
            return ResponseEntity.created(URI.create("/" + result.getId()))
                    .eTag(meta.hash())
                    .lastModified(meta.modifiedAt().toInstant())
                    .body(new ListResponseTripOrderData(result));
        });
    }

    @Override
    public CompletableFuture<ResponseEntity<Void>> edit(EditExternalRequest editExternalRequest, UUID requestId, Optional<OffsetDateTime> ifUnmodifiedSince) {
        final var currentUser = ControllerUtils.currentUser();
        final var force = ControllerUtils.isDataMaster();
        final var employeeOrganization = getEmployeeOrganization();
        final var context = SecurityContextHolder.getContext();
        return CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.setContext(context);
            if (ifUnmodifiedSince.isPresent()) {
                final var meta = tripOrdersMetaProvider.meta(employeeOrganization, requestId);
                if (meta.modifiedAt().isAfter(ifUnmodifiedSince.get())) {
                    return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
                }
            }
            tripOrdersService.edit(currentUser, force, employeeOrganization, requestId, new WebRequestTripOrderData(editExternalRequest), Stream.of("=status", "=factCost", "=reason","=receiptLink").collect(Collectors.toUnmodifiableSet()));
            final var meta = tripOrdersMetaProvider.meta(employeeOrganization, requestId);
            return ResponseEntity.accepted()
                    .eTag(meta.hash())
                    .lastModified(meta.modifiedAt().toInstant())
                    .build();
        });
    }

    @Override
    public CompletableFuture<ResponseEntity<Void>> editPartially(Patch patch, UUID requestId, Optional<OffsetDateTime> ifUnmodifiedSince) {
        final var currentUser = ControllerUtils.currentUser();
        final var force = ControllerUtils.isDataMaster();
        final var employeeOrganization = getEmployeeOrganization();
        final var context = SecurityContextHolder.getContext();
        return CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.setContext(context);
            if (ifUnmodifiedSince.isPresent()) {
                final var meta = tripOrdersMetaProvider.meta(employeeOrganization, requestId);
                if (meta.modifiedAt().isAfter(ifUnmodifiedSince.get())) {
                    return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                            .eTag(meta.hash())
                            .lastModified(meta.modifiedAt().toInstant())
                            .build();
                }
            }
            final var data = new EditExternalRequest();
            final var fields = new HashSet<String>();
            final var parser = new SpelExpressionParser(new SpelParserConfiguration(true, true));
            for (final var field : patch) {
                final var prefix = switch (field.getOp()) {
                    case ADD -> "+";
                    case REPLACE -> "=";
                    case REMOVE -> "-";
                };
                final var path = field.getPath().replaceFirst("/", "").replace("/", ".");
                try {
                    mapValue(path, field.getValue())
                            .forEach(it -> {
                                final var currentPath = it.getKey();
                                parser.parseExpression(currentPath).setValue(data, it.getValue());
                                fields.add(prefix + currentPath);
                            });
                } catch (SpelEvaluationException e) {
                    if (SpelMessage.PROPERTY_OR_FIELD_NOT_WRITABLE.equals(e.getMessageCode())) {
                        log.warn("Field '{}' not found", path);
                    } else {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
            tripOrdersService.edit(currentUser, force, employeeOrganization, requestId, new WebRequestTripOrderData(data), fields);
            final var meta = tripOrdersMetaProvider.meta(employeeOrganization, requestId);
            return ResponseEntity.accepted()
                    .eTag(meta.hash())
                    .lastModified(meta.modifiedAt().toInstant())
                    .build();
        });
    }

    @SneakyThrows(JsonProcessingException.class)
    private List<Map.Entry<String, Object>> mapValue(String field, Object value) {
        Map<String, Object> valueMap = null;
        if (value instanceof String stringValue && stringValue.startsWith("{") && stringValue.trim().endsWith("}")) {
            valueMap = objectMapper.readValue(stringValue, new TypeReference<>() {});
        }
        if (value instanceof Map<?, ?> valueMapObject) {
            valueMap = ReflectionUtils.cast(valueMapObject);
        }
        if (valueMap != null) {
            return valueMap.entrySet()
                    .parallelStream()
                    .map(it -> mapValue(field + "." + it.getKey(), it.getValue()))
                    .flatMap(List::stream)
                    .toList();
        }
        return List.of(new AbstractMap.SimpleEntry<>(field, value));
    }

    @Override
    public CompletableFuture<ResponseEntity<Void>> delete(UUID requestId, Optional<OffsetDateTime> ifUnmodifiedSince) {
        final var employeeOrganization = getEmployeeOrganization();
        final var context = SecurityContextHolder.getContext();
        return CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.setContext(context);
            if (ifUnmodifiedSince.isPresent()) {
                final var meta = tripOrdersMetaProvider.meta(employeeOrganization, requestId);
                if (meta.modifiedAt().isAfter(ifUnmodifiedSince.get())) {
                    return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                            .eTag(meta.hash())
                            .lastModified(meta.modifiedAt().toInstant())
                            .build();
                }
            }
            tripOrdersService.delete(employeeOrganization, requestId);
            return ResponseEntity.noContent().build();
        });
    }

    private @Nullable UUID getEmployeeOrganization() {
        return ControllerUtils.isDataMaster() ? null : employeeOrganizationFunction.apply(ControllerUtils.currentUser());
    }

}
