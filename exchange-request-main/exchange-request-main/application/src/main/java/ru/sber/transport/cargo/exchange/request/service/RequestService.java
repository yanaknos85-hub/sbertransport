package ru.sber.transport.cargo.exchange.request.service;

import org.springframework.data.domain.Page;
import ru.sber.transport.cargo.exchange.request.database.model.Request;
import ru.sber.transport.cargo.exchange.request.database.model.User;
import ru.sber.transport.cargo.exchange.request.dto.*;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.enums.Role;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления заявками (Request).
 * Предоставляет бизнес-логику для работы с заявками: получение, создание, обновление и фильтрация.
 */
public interface RequestService {

    /**
     * Возвращает список всех заявок, принадлежащих пользователю с указанным идентификатором.
     *
     * @param ownerId идентификатор пользователя — владельца заявок
     * @return список заявок, отсортированный по дате создания (сначала новые)
     */
    List<Request> getByOwnerId(UUID ownerId);

    /**
     * Возвращает заявку по её идентификатору, только если она принадлежит указанному владельцу.
     * Используется для защиты от несанкционированного доступа к чужим заявкам.
     *
     * @param id      идентификатор заявки
     * @param ownerId идентификатор владельца (пользователя)
     * @return найденная заявка
     * @throws IllegalArgumentException если заявка не найдена или не принадлежит владельцу
     */
    Request getByIdAndUserId(UUID id, UUID ownerId);

    /**
     * Возвращает заявку по её идентификатору, только если она принадлежит указанному владельцу.
     * Используется для защиты от несанкционированного доступа к чужим заявкам.
     *
     * @param id идентификатор заявки
     * @return найденная заявка
     * @throws IllegalArgumentException если заявка не найдена или не принадлежит владельцу
     */
    Request getById(UUID id);


    /**
     * Возращает пагинированный список опубликованных заявок.
     *
     * @param searchDto      RequestSearchDto
     * @param organizationId UUID
     * @return Page<MarketplaceRequesSummaryDto>
     */
    Page<MarketplaceRequestDto> searchPublishedRequests(MarketplaceRequestFilterDto searchDto, UUID organizationId);


    /**
     * Возращает пагинированный список заявок грузовладельца и его организации.
     *
     * @param searchDto RequestSearchDto
     * @return Page<ShipperRequestSummaryDto>
     */
    Page<ShipperRequestDto> searchShipperRequestsByOrganizationId(ShipperRequestFilterDto searchDto);

    /**
     * Возращает пагинированный список заявок грузоперевозчика и его организации.
     *
     * @param searchDto RequestSearchDto
     * @return Page<ShipperRequestSummaryDto>
     */
    Page<CarrierRequestDto> searchRequestsCarrierOrganizationId(
            CarrierRequestFilterDto searchDto,
            UUID orgId);

    /**
     * Сохраняет заявку в базе данных.
     *
     * @param request Request
     */
    Request save(Request request);

    long deleteExpiredDrafts();

    StatusResponseDto changeStatus(UUID id, RequestStatus status, User user, Role role);
}
