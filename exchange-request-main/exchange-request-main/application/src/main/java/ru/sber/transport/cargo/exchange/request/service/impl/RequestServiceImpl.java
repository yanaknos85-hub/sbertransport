package ru.sber.transport.cargo.exchange.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.cargo.exchange.request.database.dao.ReplyRepository;
import ru.sber.transport.cargo.exchange.request.database.dao.RequestHistoryRepository;
import ru.sber.transport.cargo.exchange.request.database.dao.RequestRepository;
import ru.sber.transport.cargo.exchange.request.database.model.Request;
import ru.sber.transport.cargo.exchange.request.database.model.RequestHistory;
import ru.sber.transport.cargo.exchange.request.database.model.Request_;
import ru.sber.transport.cargo.exchange.request.database.model.User;
import ru.sber.transport.cargo.exchange.request.dto.*;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.enums.Role;
import ru.sber.transport.cargo.exchange.request.exception.RequestNotFoundException;
import ru.sber.transport.cargo.exchange.request.exception.StatusNotTransitionException;
import ru.sber.transport.cargo.exchange.request.mapper.RequestMapper;
import ru.sber.transport.cargo.exchange.request.service.RequestService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static ru.sber.transport.cargo.exchange.request.enums.RequestStatus.DRAFT;

/**
 * Реализация сервиса для управления заявками.
 * Содержит бизнес-логику по работе с сущностью Request.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    public static final String REQUEST_NOT_FOUND_OR_NO_ACCESS_MESSAGE = "Заявка не найдена или у вас нет прав на её просмотр";

    private static final Sort REQUEST_SORT_BY_HUMAN_READABLE_ID = Sort.by(DESC, Request_.HUMAN_READABLE_ID);

    private final RequestRepository requestRepository;

    private final ReplyRepository replyRepository;

    private final RequestHistoryRepository requestHistoryRepository;

    private final RequestMapper requestMapper;

    /**
     * Возвращает список всех заявок, принадлежащих пользователю с указанным идентификатором.
     *
     * @param ownerId идентификатор пользователя — владельца заявок
     * @return список заявок, отсортированный по дате создания (сначала новые)
     */
    @Override
    public List<Request> getByOwnerId(UUID ownerId) {
        if (ownerId == null) {
            log.warn("Попытка получить заявки с ownerId = null");
            throw new IllegalArgumentException("Идентификатор владельца (ownerId) не может быть null");
        }

        log.debug("Запрос на получение заявок для владельца: {}", ownerId);
        List<Request> requests = requestRepository.findByOwnerId(ownerId);

        log.debug("Найдено {} заявок для владельца: {}", requests.size(), ownerId);
        return requests;
    }

    /**
     * Возвращает заявку по её идентификатору, только если она принадлежит указанному владельцу.
     *
     * @param id      идентификатор заявки
     * @param ownerId идентификатор владельца (пользователя)
     * @return найденная заявка
     * @throws IllegalArgumentException если входные данные null
     * @throws RequestNotFoundException если заявка не найдена или не принадлежит владельцу
     */
    @Override
    @Transactional(readOnly = true)
    public Request getByIdAndUserId(UUID id, UUID ownerId) {
        if (id == null) {
            log.warn("Попытка получить заявку с id = null");
            throw new IllegalArgumentException("Идентификатор заявки (id) не может быть null");
        }
        if (ownerId == null) {
            log.warn("Попытка получить заявку с ownerId = null");
            throw new IllegalArgumentException("Идентификатор владельца (ownerId) не может быть null");
        }

        log.debug("Поиск заявки с id={} для владельца={}", id, ownerId);

        return requestRepository.getByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> {
                    log.warn("Заявка с id={} не найдена или не принадлежит владельцу={}", id, ownerId);
                    return new RequestNotFoundException(
                            RequestServiceImpl.REQUEST_NOT_FOUND_OR_NO_ACCESS_MESSAGE
                    );
                });
    }


    /**
     * Возвращает заявку по её идентификатору, только если она принадлежит указанному владельцу.
     *
     * @param id идентификатор заявки
     * @return найденная заявка
     * @throws IllegalArgumentException если входные данные null
     * @throws RequestNotFoundException если заявка не найдена или не принадлежит владельцу
     */
    @Override
    @Transactional(readOnly = true)
    public Request getById(UUID id) {
        if (id == null) {
            log.warn("Попытка получить заявку с id = null");
            throw new IllegalArgumentException("Идентификатор заявки (id) не может быть null");
        }

        log.debug("Поиск заявки с id={} ", id);

        return requestRepository.findFullRequestById(id)
                .orElseThrow(() -> {
                    log.warn("Заявка с id={} не найдена", id);
                    return new RequestNotFoundException(
                            RequestServiceImpl.REQUEST_NOT_FOUND_OR_NO_ACCESS_MESSAGE
                    );
                });
    }

    @Transactional(readOnly=true)
    @Override
    public Page<MarketplaceRequestDto> searchPublishedRequests(MarketplaceRequestFilterDto searchDto, UUID organizationId) {
        Page<Request> requestSearch = doSearchPublishedRequests(searchDto,
                getPageRequest(searchDto));
        List<MarketplaceRequestDto> routelistDtoList = requestSearch.stream()
                .map(r -> requestMapper.toSummaryDto(r, organizationId))
                .toList();
        log.debug("REQUEST_SERVICE количество строк = {}", routelistDtoList.size());
        return new PageImpl<>(
                routelistDtoList,
                requestSearch.getPageable(),
                requestSearch.getTotalElements()
        );
    }

    @Override
    public Page<ShipperRequestDto> searchShipperRequestsByOrganizationId(ShipperRequestFilterDto searchDto) {
        Page<Request> requestSearch = doSearchShippersRequests(searchDto,
                getPageRequest(searchDto));
        List<ShipperRequestDto> routelistDtoList = requestSearch.stream()
                .map(requestMapper::toShipperRequestDto)
                .toList();
        log.debug("REQUEST_SERVICE количество строк = {}", routelistDtoList.size());
        return new PageImpl<>(
                routelistDtoList,
                requestSearch.getPageable(),
                requestSearch.getTotalElements()
        );
    }

    @Override
    public Page<CarrierRequestDto> searchRequestsCarrierOrganizationId(CarrierRequestFilterDto searchDto, UUID organizationId) {
        Page<Request> requestSearch = doSearchCarriersRequests(searchDto, organizationId,
                getPageRequest(searchDto));
        List<CarrierRequestDto> routelistDtoList = requestSearch.stream()
                .map(requestMapper::toCarrierRequest)
                .toList();
        log.debug("REQUEST_SERVICE количество строк = {}", routelistDtoList.size());
        return new PageImpl<>(
                routelistDtoList,
                requestSearch.getPageable(),
                requestSearch.getTotalElements()
        );
    }

    private Page<Request> doSearchPublishedRequests(MarketplaceRequestFilterDto searchDto, Pageable pageable) {

        Specification<Request> spec = RequestSearchSpecImpl.getPublishedRequestsBySpec(searchDto);

        Specification<Request> publishedSpec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), RequestStatus.PUBLISHED);
        Specification<Request> finalSpec = spec.and(publishedSpec);

        return requestRepository.findAll(finalSpec, pageable);
    }

    private Page<Request> doSearchShippersRequests(ShipperRequestFilterDto searchDto, Pageable pageable) {
        Specification<Request> spec = RequestSearchSpecImpl.getShipperRequestsBySpec(searchDto);

        return requestRepository.findAll(spec, pageable);
    }

    private Page<Request> doSearchCarriersRequests(CarrierRequestFilterDto searchDto, UUID carrierOrganizationId, Pageable pageable) {
        Specification<Request> spec = RequestSearchForCarrierSpecImpl.getRequestSearchForCarrierBySpec(
                        searchDto, carrierOrganizationId);

        return requestRepository.findAll(spec, pageable);
    }

    private PageRequest getPageRequest(MarketplaceRequestFilterDto searchDto) {
        if (searchDto.getPageSetting() == null) {
            return PageRequest.of(0, 10, REQUEST_SORT_BY_HUMAN_READABLE_ID);
        }
        return PageRequest.of(searchDto.getPageSetting().getPage(), searchDto.getPageSetting().getSize(), REQUEST_SORT_BY_HUMAN_READABLE_ID);
    }

    private PageRequest getPageRequest(ShipperRequestFilterDto searchDto) {
        if (searchDto.getPageSetting() == null) {
            return PageRequest.of(0, 10, REQUEST_SORT_BY_HUMAN_READABLE_ID);
        }
        return PageRequest.of(searchDto.getPageSetting().getPage(), searchDto.getPageSetting().getSize(), searchDto.getSort());
    }

    private PageRequest getPageRequest(CarrierRequestFilterDto searchDto) {
        if (searchDto.getPageSetting() == null) {
            return PageRequest.of(0, 10, REQUEST_SORT_BY_HUMAN_READABLE_ID);
        }
        return PageRequest.of(searchDto.getPageSetting().getPage(), searchDto.getPageSetting().getSize(), searchDto.getSort());
    }

    @Override
    @Transactional
    public Request save(Request request) {
        Optional.ofNullable(request.getSpecialConditions())
                .ifPresent(specialConditions -> specialConditions.setRequest(request));
        Optional.ofNullable(request.getCargoDetails())
                .ifPresent(cargoDetails -> cargoDetails.setRequest(request));
        Optional.ofNullable(request.getVehicleRequirements())
                .ifPresent(vehicleRequirements -> vehicleRequirements.setRequest(request));
        Optional.ofNullable(request.getWaypoints())
                .ifPresent(waypoints -> waypoints
                        .forEach(waypoint -> waypoint.setRequest(request)));
        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public long deleteExpiredDrafts() {
        return requestRepository.deleteByStatusAndExpiresAtBefore(DRAFT, LocalDateTime.now());
    }

    @Override
    @Transactional
    public StatusResponseDto changeStatus(UUID requestId, RequestStatus status, User user, Role role) {
        var request = getById(requestId);
        checkNewStatus(request, status, role);

        var prevStatus = request.getStatus();
        var savedRequest = requestRepository.save(processChangeStatus(request, status));

        var history = RequestHistory.builder()
                .changeDate(LocalDateTime.now(ZoneOffset.UTC))
                .status(status)
                .comment("Изменение статуса")
                .initiatorId(user.getId())
                .request(savedRequest)
                .initiatorRole(role)
                .build();
        requestHistoryRepository.save(history);

        afterProcess(savedRequest);

        return StatusResponseDto.builder()
                .success(true)
                .request(StatusResponseDto.RequestStatusChangeInfo.builder()
                        .requestId(requestId)
                        .previousStatus(StatusResponseDto.StatusInfo.builder()
                                .name(prevStatus)
                                .code(prevStatus.getCode())
                                .build())
                        .currentStatus(StatusResponseDto.StatusInfo.builder()
                                .name(status)
                                .code(status.getCode())
                                .build())
                        .build())
                .build();
    }

    private void checkNewStatus(Request request, RequestStatus newStatus, Role role) {
        if (!request.getStatus().canTransitionTo(newStatus, role)) {
            throw new StatusNotTransitionException("Переход из статуса "
                    + request.getStatus() + " в статус " + newStatus + "  невозможен для " + role.getDisplayNameRP() + ".");
        }
    }

    private Request processChangeStatus(Request request, RequestStatus status) {
        request.setStatus(status);

        if (RequestStatus.CANCELLED_BY_CUSTOMER == status) {
            clearReplyInRequest(request);
        }

        return request;
    }

    private void clearReplyInRequest(Request request) {
        request.setCarrierOrganizationId(null);
        request.setCarrierInfo(null);
    }

    private void afterProcess(Request request) {
        if (RequestStatus.CANCELLED_BY_CUSTOMER == request.getStatus() || RequestStatus.CONFIRMED == request.getStatus()) {
            replyRepository.deleteAllByRequestId(request.getId());
        }
    }
}


