package ru.sber.transport.cargo.exchange.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.cargo.exchange.request.database.dao.ReplyRepository;
import ru.sber.transport.cargo.exchange.request.database.model.CarrierReply;
import ru.sber.transport.cargo.exchange.request.database.model.Request;
import ru.sber.transport.cargo.exchange.request.database.model.User;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyDto;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyShortDto;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.exception.ReplyNotFoundException;
import ru.sber.transport.cargo.exchange.request.mapper.ReplyMapper;
import ru.sber.transport.cargo.exchange.request.service.ReplyService;
import ru.sber.transport.cargo.exchange.request.service.ReplyValidationService;
import ru.sber.transport.cargo.exchange.request.service.RequestService;

import java.util.List;
import java.util.UUID;

import static ru.sber.transport.cargo.exchange.request.enums.RequestStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {
    private final ReplyRepository replyRepository;
    private final RequestService requestService;
    private final ReplyValidationService replyValidationService;
    private final ReplyMapper replyMapper;

    @Override
    @Transactional
    public void add(UUID requestId, CarrierReplyDto dto, User user) {
        var request = requestService.getById(requestId);
        replyValidationService.checkRequestStatus(request, PUBLISHED);
        replyValidationService.checkDuplucate(request.getId(), user.getOrganization().getId());

        replyRepository.save(replyMapper.toModel(request.getId(), user.getOrganization().getId(), dto));
    }

    @Override
    public List<CarrierReplyShortDto> getAllByRequest(UUID requestId) {
        var request = requestService.getById(requestId);

        var replies = replyRepository.findAllByRequestId(requestId);
        if (RequestStatus.CARRIER_SELECTED == request.getStatus()) {
            return replyMapper.toShortDtos(replies.stream().filter(CarrierReply::isSelected).toList());
        }

        return replyMapper.toShortDtos(replies);
    }

    @Override
    @Transactional
    public void accept(UUID replyId) {
        var reply = get(replyId);

        var request = reply.getRequest();
        replyValidationService.checkRequestStatus(request, PUBLISHED);

        request.setCarrierOrganizationId(reply.getOrganization().getId());
        request.setCarrierInfo(replyMapper.toDto(reply));
        request.setStatus(CARRIER_SELECTED);
        requestService.save(request);

        reply.setSelected(true);
        replyRepository.save(reply);
    }

    @Override
    @Transactional
    public void cancelByCarrier(UUID requestId, UUID userOrganizationId) {
        var reply = getByRequest(requestId, userOrganizationId);
        var request = reply.getRequest();
        replyValidationService.checkRequestStatusIn(request,
                PUBLISHED, CARRIER_SELECTED, CONFIRMED, DEPARTING_TO_LOADING, ARRIVED_AT_LOADING);

        doCancel(reply, reply.getRequest(), true);
    }

    @Transactional
    @Override
    public void cancelByShipper(UUID replyId, User user) {
        var reply = get(replyId);
        var request = reply.getRequest();
        replyValidationService.checkRequestStatus(request, CARRIER_SELECTED);
        replyValidationService.checkRequestOwner(request, user);

        doCancel(reply, request, false);
    }

    private void doCancel(CarrierReply reply, Request request, boolean deleteReply) {
        if (RequestStatus.CARRIER_SELECTED == request.getStatus() && reply.isSelected()) {
            request.setCarrierOrganizationId(null);
            request.setCarrierInfo(null);
            request.setStatus(PUBLISHED);
            requestService.save(request);
        }

        if (deleteReply) {
            replyRepository.deleteById(reply.getId());
        } else {
            reply.setSelected(false);
            replyRepository.save(reply);
        }
    }

    private CarrierReply get(UUID replyId) {
        return replyRepository.findById(replyId)
                .orElseThrow(() -> new ReplyNotFoundException("Не найден отклик: %s".formatted(replyId.toString())));
    }

    private CarrierReply getByRequest(UUID requestId, UUID organizationId) {
        return replyRepository.findByRequestIdAndOrganizationId(requestId, organizationId)
                .orElseThrow(() -> new ReplyNotFoundException("Не найден отклик по заявке: %s".formatted(requestId.toString())));
    }
}
