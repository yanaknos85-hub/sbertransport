package ru.sber.transport.cargo.exchange.request.service;

import ru.sber.transport.cargo.exchange.request.database.model.User;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyDto;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyShortDto;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления откликами грузоперевозчиков на заявки.
 * Предоставляет методы для создания, получения, принятия и отмены откликов.
 */
public interface ReplyService {

    /**
     * Добавляет новый отклик грузоперевозчика к заявке.
     *
     * @param requestId идентификатор заявки, на который даётся отклик
     * @param dto данные отклика грузоперевозчика (информация об авто, водителе, стоимости и т.д.)
     * @param user пользователь (грузоперевозчик), отправляющий отклик
     * @throws IllegalArgumentException если один из параметров недействителен
     * @throws ru.sber.transport.cargo.exchange.request.exception.RequestNotFoundException если заявка с указанным ID не найдена
     * @throws ru.sber.transport.cargo.exchange.request.exception.ReplyDuplicatedException если грузоперевозчик уже отклинулся на эту заявку
     */
    void add(UUID requestId, CarrierReplyDto dto, User user);

    /**
     * Возвращает список краткой информации обо всех откликах для указанной заявки.
     *
     * @param requestId идентификатор заявки
     * @return список кратких DTO ответов ({@link CarrierReplyShortDto})
     * @throws ru.sber.transport.cargo.exchange.request.exception.RequestNotFoundException если заявка с указанным ID не найдена
     */
    List<CarrierReplyShortDto> getAllByRequest(UUID requestId);

    /**
     * Принимает конкретный отклик грузоперевозчика.
     * После принятия заявка меняет статус на "Грузоперевозчик выбран". Принятый отклик помечается как выбранный.
     *
     * @param replyId идентификатор отклика грузоперевозчика
     * @throws ru.sber.transport.cargo.exchange.request.exception.ReplyNotFoundException если отклик с указанным ID не найден
     * @throws ru.sber.transport.cargo.exchange.request.exception.ReplyRequestIncorrectStatusException если заявка не в статусе "Опубликована"
     */
    void accept(UUID replyId);

    /**
     * Отзыв отклика грузоперевозчиком.
     * Может быть вызван только грузоперевозчиком, который откликнулся на заявку
     *
     * @param requestId идентификатор заявки
     * @param userOrganizationId идентификатор организации пользователя (грузоперевозчика)
     * @throws ru.sber.transport.cargo.exchange.request.exception.ReplyNotFoundException если отклик не найден
     * @throws ru.sber.transport.cargo.exchange.request.exception.ReplyRequestIncorrectStatusException
     * если заявка в статусе "Идёт погрузка" и выше или в статусе "Черновик"
     */
    void cancelByCarrier(UUID requestId, UUID userOrganizationId);

    /**
     * Отзыв подтверждения отклика грузовладельцем.
     * Может быть вызван только грузовладельцем, создавшим заявку.
     *
     * @param replyId идентификатор отклика
     * @param user пользователь (грузовладельцем), отзывающий отклик
     * @throws ru.sber.transport.cargo.exchange.request.exception.ReplyNotFoundException если отклик не найден
     * @throws ru.sber.transport.cargo.exchange.request.exception.AccessDeniedException если пользователь не является владельцем заявки
     * @throws ru.sber.transport.cargo.exchange.request.exception.ReplyRequestIncorrectStatusException если заявка не в стутсе "Грузоперевозчик выбран"
     */
    void cancelByShipper(UUID replyId, User user);
}