package ru.sber.transport.cargo.exchange.request.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.cargo.exchange.request.database.model.User;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyDto;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyShortDto;
import ru.sber.transport.cargo.exchange.request.enums.Role;
import ru.sber.transport.cargo.exchange.request.service.ReplyService;
import ru.sber.transport.cargo.exchange.request.service.ReplyValidationService;
import ru.sber.transport.cargo.exchange.request.service.UserService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReplyControllerImpl.class)
class ReplyControllerImplTest {
    private static final String URI = "/reply";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReplyService replyService;

    @MockitoBean
    private ReplyValidationService replyValidationService;

    @MockitoBean
    private UserService userService;

    private UUID requestId;
    private UUID replyId;
    private CarrierReplyDto requestDto;
    private CarrierReplyShortDto shortDto;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        replyId = UUID.randomUUID();

        // DTO для запроса
        requestDto = new CarrierReplyDto(
                new CarrierReplyDto.Auto("Volvo FH16", "Volvo", "FH16", "2020", "16","A123AA777"),
                new CarrierReplyDto.Trailer("КрАЗ Т-2020", "КрАЗ", "Т-2020", "B456BB777"),
                new CarrierReplyDto.Driver("Иван Иванов", "+79161234567", "1234 567890"),
                50000.0,
                null,
                "Готов к отправке"
        );

        // Короткий DTO для ответа
        shortDto = new CarrierReplyShortDto(
                replyId,
                "ТрансЛогистик",
                4.5,
                "+79991234567",
                new CarrierReplyShortDto.Auto("Volvo 2020", "FH16"),
                50000.0,
                "Готов к отправке"
        );
    }

    @Test
    void add_ShouldReturn200_WhenValid() throws Exception {
        // given
        doNothing().when(replyValidationService).validateAdd(any(), any());
        doNothing().when(replyService).add(any(), any(), any());

        // when & then
        mockMvc.perform(post("%s/%s".formatted(URI, requestId.toString()))
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(buildJwt(Role.SHIPPER)))
                .andExpect(status().isOk());

        verify(replyValidationService).validateAdd(eq(requestId), any(JwtAuthenticationToken.class));
        verify(replyService).add(eq(requestId), eq(requestDto), any());
    }

    @Test
    void getAllByRequest_ShouldReturnList_WhenShipper() throws Exception {
        // given
        when(replyService.getAllByRequest(any())).thenReturn(List.of(shortDto));

        // when & then
        mockMvc.perform(get("%s/%s/%s".formatted(URI, "list", requestId.toString()))
                        .with(buildJwt(Role.SHIPPER)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(shortDto))));

        verify(replyValidationService).validateGetAll(any(JwtAuthenticationToken.class));
        verify(replyService).getAllByRequest(requestId);
    }

    @Test
    void accept_ShouldReturn200_WhenShipper() throws Exception {
        // given
        doNothing().when(replyValidationService).validateAccept(any());
        doNothing().when(replyService).accept(any());

        // when & then
        mockMvc.perform(put("%s/%s".formatted(URI, replyId.toString()))
                        .with(buildJwt(Role.SHIPPER)))
                .andExpect(status().isOk());

        verify(replyValidationService).validateAccept(any());
        verify(replyService).accept(replyId);
    }

    @Test
    void cancelByCarrier_ShouldReturn200_WhenCarrier() throws Exception {
        // given
        doNothing().when(replyValidationService).validateCancel(any());
        doNothing().when(replyService).cancelByCarrier(any(), any());

        when(userService.findOrganizationIdByToken(any())).thenReturn(UUID.randomUUID());

        // when & then
        mockMvc.perform(delete("%s/%s/%s".formatted(URI, requestId.toString(), "carrier"))
                        .with(buildJwt(Role.CARRIER)))
                .andExpect(status().isOk());

        verify(replyValidationService).validateCancel(any());
        verify(userService).findOrganizationIdByToken(any());
        verify(replyService).cancelByCarrier(eq(requestId), any(UUID.class));
    }

    @Test
    void cancelByShipper_ShouldReturn200_WhenShipper() throws Exception {
        // given
        doNothing().when(replyValidationService).validateCancel(any());
        doNothing().when(replyService).cancelByShipper(any(), any());

        when(userService.findUserByToken(any())).thenReturn(new User());

        // when & then
        mockMvc.perform(delete("%s/%s/%s".formatted(URI, replyId.toString(), "shipper"))
                        .with(buildJwt(Role.SHIPPER)))
                .andExpect(status().isOk());

        verify(replyValidationService).validateCancel(any());
        verify(userService).findUserByToken(any());
        verify(replyService).cancelByShipper(eq(replyId), any());
    }

    @Test
    void add_ShouldReturn403_WhenNotAuthenticated() throws Exception {
        // when & then
        mockMvc.perform(post("%s/%s".formatted(URI, requestId.toString()))
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void add_ShouldReturn400_WhenInvalidJson() throws Exception {
        // when & then
        mockMvc.perform(post("%s/%s".formatted(URI, requestId.toString()))
                        .content("{invalid-json}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(buildJwt(Role.CARRIER)))
                .andExpect(status().isBadRequest());
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor buildJwt(Role role) {
        return jwt().jwt(builder -> builder.jti("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380023"))
                .authorities(new SimpleGrantedAuthority(role.name()));
    }
}