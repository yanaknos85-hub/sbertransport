package ru.sber.transport.cargo.exchange.request.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.cargo.exchange.request.database.dao.ReplyRepository;
import ru.sber.transport.cargo.exchange.request.database.model.CarrierReply;
import ru.sber.transport.cargo.exchange.request.database.model.Request;
import ru.sber.transport.cargo.exchange.request.database.model.User;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyDto;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyShortDto;
import ru.sber.transport.cargo.exchange.request.exception.ReplyNotFoundException;
import ru.sber.transport.cargo.exchange.request.mapper.ReplyMapper;
import ru.sber.transport.cargo.exchange.request.service.ReplyValidationService;
import ru.sber.transport.cargo.exchange.request.service.RequestService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static ru.sber.transport.cargo.exchange.request.enums.RequestStatus.*;

@ExtendWith(MockitoExtension.class)
class ReplyServiceImplTest {

    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private RequestService requestService;
    @Mock
    private ReplyValidationService replyValidationService;
    @Mock
    private ReplyMapper replyMapper;

    @InjectMocks
    private ReplyServiceImpl replyService;

    private UUID requestId;
    private UUID replyId;
    private UUID orgId;
    private Request request;
    private User user;
    private CarrierReplyDto dto;
    private CarrierReply reply;
    private CarrierReplyShortDto shortDto;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        replyId = UUID.randomUUID();
        orgId = UUID.randomUUID();

        request = new Request();
        request.setId(requestId);
        request.setStatus(PUBLISHED);

        user = new User();
        var org = new ru.sber.transport.cargo.exchange.request.database.model.Organization();
        org.setId(orgId);
        user.setOrganization(org);

        dto = new CarrierReplyDto(null, null, null, 10000.0, null, null);
        reply = new CarrierReply();
        reply.setId(replyId);
        reply.setRequest(request);
        reply.setSelected(false);
        reply.setOrganization(org);

        shortDto = new CarrierReplyShortDto(replyId, "Перевозчик А", 4.5, "+79991234567",
                new CarrierReplyShortDto.Auto("Фура 5т", "Камаз 1979"), 10000.0, null);
    }

    @Test
    void add_ShouldSaveReply_WhenValid() {
        // given
        when(requestService.getById(requestId)).thenReturn(request);
        when(replyMapper.toModel(requestId, orgId, dto)).thenReturn(reply);

        // when
        replyService.add(requestId, dto, user);

        // then
        verify(replyValidationService).checkRequestStatus(request, PUBLISHED);
        verify(replyValidationService).checkDuplucate(requestId, orgId);
        verify(replyRepository).save(reply);
    }

    @Test
    void getAllByRequest_ShouldReturnAllReplies_WhenStatusPublished() {
        // given
        var replies = List.of(reply);
        var shortDtos = List.of(shortDto);

        when(requestService.getById(requestId)).thenReturn(request);
        when(replyRepository.findAllByRequestId(requestId)).thenReturn(replies);
        when(replyMapper.toShortDtos(replies)).thenReturn(shortDtos);

        // when
        var result = replyService.getAllByRequest(requestId);

        // then
        assertThat(result).isEqualTo(shortDtos);
        verify(replyMapper).toShortDtos(replies);
    }

    @Test
    void getAllByRequest_ShouldFilterSelected_WhenStatusCarrierSelected() {
        // given
        request.setStatus(CARRIER_SELECTED);
        reply.setSelected(true);

        var replies = List.of(reply, new CarrierReply());
        var filteredReplies = List.of(reply);
        var shortDtos = List.of(shortDto);

        when(requestService.getById(requestId)).thenReturn(request);
        when(replyRepository.findAllByRequestId(requestId)).thenReturn(replies);
        when(replyMapper.toShortDtos(filteredReplies)).thenReturn(shortDtos);

        // when
        var result = replyService.getAllByRequest(requestId);

        // then
        assertThat(result).isEqualTo(shortDtos);
        verify(replyMapper).toShortDtos(filteredReplies);
    }

    @Test
    void accept_ShouldSetCarrierAndSelectReply_WhenValid() {
        // given
        when(replyRepository.findById(replyId)).thenReturn(Optional.of(reply));
        when(replyMapper.toDto(reply)).thenReturn(dto);

        // when
        replyService.accept(replyId);

        // then
        assertThat(request.getCarrierOrganizationId()).isEqualTo(orgId);
        assertThat(request.getCarrierInfo()).isEqualTo(dto);
        assertThat(request.getStatus()).isEqualTo(CARRIER_SELECTED);
        assertThat(reply.isSelected()).isTrue();

        verify(requestService).save(request);
        verify(replyRepository).save(reply);
        verify(replyValidationService).checkRequestStatus(request, PUBLISHED);
    }

    @Test
    void accept_ShouldThrow_WhenReplyNotFound() {
        // given
        when(replyRepository.findById(replyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> replyService.accept(replyId))
                .isInstanceOf(ReplyNotFoundException.class);
    }

    @Test
    void cancelByCarrier_ShouldDeleteReplyAndResetRequest_WhenSelected() {
        // given
        reply.setSelected(true);
        request.setStatus(CARRIER_SELECTED);
        request.setCarrierOrganizationId(orgId);

        when(replyRepository.findByRequestIdAndOrganizationId(requestId, orgId)).thenReturn(Optional.of(reply));

        // when
        replyService.cancelByCarrier(requestId, orgId);

        // then
        assertThat(request.getCarrierOrganizationId()).isNull();
        assertThat(request.getCarrierInfo()).isNull();
        assertThat(request.getStatus()).isEqualTo(PUBLISHED);

        verify(replyValidationService).checkRequestStatusIn(request,
                PUBLISHED, CARRIER_SELECTED, CONFIRMED, DEPARTING_TO_LOADING, ARRIVED_AT_LOADING);
        verify(requestService).save(request);
        verify(replyRepository).deleteById(replyId);
    }

    @Test
    void cancelByCarrier_ShouldDeleteReply_WhenNotSelected() {
        // given
        reply.setSelected(false);
        request.setStatus(PUBLISHED);

        when(replyRepository.findByRequestIdAndOrganizationId(requestId, orgId)).thenReturn(Optional.of(reply));

        // when
        replyService.cancelByCarrier(requestId, orgId);

        // then
        verify(replyValidationService).checkRequestStatusIn(request,
                PUBLISHED, CARRIER_SELECTED, CONFIRMED, DEPARTING_TO_LOADING, ARRIVED_AT_LOADING);
        verify(requestService, never()).save(request);
        verify(replyRepository).deleteById(replyId);
    }

    @Test
    void cancelByCarrier_ShouldThrow_WhenReplyNotFound() {
        // given
        reply.setSelected(false);
        request.setStatus(PUBLISHED);

        when(replyRepository.findByRequestIdAndOrganizationId(requestId, orgId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> replyService.cancelByCarrier(requestId, orgId))
                .isInstanceOf(ReplyNotFoundException.class);
    }

    @Test
    void cancelByShipper_ShouldUnselectAndResetRequest_WhenValid() {
        // given
        reply.setSelected(true);
        request.setStatus(CARRIER_SELECTED);
        request.setCarrierOrganizationId(orgId);

        when(replyRepository.findById(replyId)).thenReturn(Optional.of(reply));

        // when
        replyService.cancelByShipper(replyId, user);

        // then
        assertThat(request.getCarrierOrganizationId()).isNull();
        assertThat(request.getCarrierInfo()).isNull();
        assertThat(request.getStatus()).isEqualTo(PUBLISHED);
        assertThat(reply.isSelected()).isFalse();

        verify(requestService).save(request);
        verify(replyRepository).save(reply);
        verify(replyValidationService).checkRequestStatus(request, CARRIER_SELECTED);
    }

    @Test
    void cancelByShipper_ShouldUnselectOnly_WhenNotSelected() {
        // given
        reply.setSelected(false);
        request.setStatus(CARRIER_SELECTED);

        when(replyRepository.findById(replyId)).thenReturn(Optional.of(reply));

        // when
        replyService.cancelByShipper(replyId, user);

        // then
        assertThat(request.getStatus()).isEqualTo(CARRIER_SELECTED); // не сбрасываем статус
        assertThat(reply.isSelected()).isFalse();

        verify(requestService, never()).save(request);
        verify(replyRepository).save(reply);
    }

    @Test
    void cancelByShipper_ShouldThrow_WhenReplyNotFound() {
        // given
        when(replyRepository.findById(replyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> replyService.cancelByShipper(replyId, user))
                .isInstanceOf(ReplyNotFoundException.class);
    }
}