package ru.sberbank.ditsib.transport.vehicle.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sberbank.ditsib.transport.vehicle.BaseIntegrationTest;
import ru.sberbank.ditsib.transport.vehicle.constants.Role;
import ru.sberbank.ditsib.transport.vehicle.database.model.Transport;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.IndicatorDateInfo;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.Indicators;
import ru.sberbank.ditsib.transport.vehicle.exception.IndicatorsException;
import ru.sberbank.ditsib.transport.vehicle.exception.NotAllowedUserException;
import ru.sberbank.ditsib.transport.vehicle.exception.StatusException;
import ru.sberbank.ditsib.transport.vehicle.messaging.sender.TransportSender;

import java.nio.charset.StandardCharsets;
import java.time.Month;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/scripts/vehicle_integration_test.sql")
@Sql("/scripts/transport_integration_test.sql")
@Sql("/scripts/indicators_integration_test.sql")
class IndicatorsTest extends BaseIntegrationTest {
    
    @MockitoBean
    private TransportSender transportSender;
    private static final String ROOT_PATH = "/indicators";
    private static final UUID EMPLOYEE_1 = fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8");
    private static final UUID EMPLOYEE_2 = fromString("01fab58c-ac99-45ef-a83e-f05b5a39d88d");
    private static final UUID TRANSPORT_1 = fromString("bc3f3b07-f136-4270-af0b-ff84af381ac4");
    private static final UUID TRANSPORT_2 = fromString("b83c1f6b-c9ab-4652-8f07-99aa1fa048d1");
    private static final UUID TRANSPORT_3 = fromString("9ad7733b-c506-4984-b893-56d7022b705b");
    
    @Test
    @DisplayName("Получение года и месяца внесения показателей")
    @SneakyThrows
    void getIndicatorsDateInfo() {
        Mockito.reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ENGINEER_CORP_CLIENT.name());
        var contentAsString = mockMvc.perform(get(ROOT_PATH + "/" + TRANSPORT_1)
                                                      .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                                                 .authorities(new SimpleGrantedAuthority(Role.ROLE_ENGINEER_CORP_CLIENT.name())))
                                                      .contentType(MediaType.APPLICATION_JSON))
                                     .andExpect(status().isOk())
                                     .andReturn()
                                     .getResponse()
                                     .getContentAsString(StandardCharsets.UTF_8);
        var result1 = objectMapper.readValue(contentAsString, IndicatorDateInfo.class);
        assertEquals(2023, result1.year());
        assertEquals(Month.MARCH, result1.month());
        mockMvc.perform(get(ROOT_PATH + "/" + TRANSPORT_1)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_2.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ENGINEER_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andExpect(result -> assertInstanceOf(NotAllowedUserException.class, result.getResolvedException()))
               .andExpect(result -> assertThat(Optional.ofNullable(result.getResolvedException()).orElseThrow().getMessage()).
                       contains("Попытка получить доступ к данным организаций без необходимых прав, " +
                                "организации:[fc73b25b-9564-4560-98b5-abc0f16af9b2], " +
                                "пользователь:01fab58c-ac99-45ef-a83e-f05b5a39d88d, " +
                                "организация пользователя:3cbe0b6b-fe04-4a28-83e8-184f0f331bff"));
        mockMvc.perform(get(ROOT_PATH + "/" + TRANSPORT_2)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ENGINEER_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andExpect(result -> assertInstanceOf(StatusException.class, result.getResolvedException()))
               .andExpect(result -> assertThat(Optional.ofNullable(result.getResolvedException()).orElseThrow().getMessage()).
                       contains("Транспорт в статусе:Выведен из эксплуатации"));
        contentAsString = mockMvc.perform(get(ROOT_PATH + "/" + TRANSPORT_3)
                                                  .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                                             .authorities(new SimpleGrantedAuthority(Role.ROLE_ENGINEER_CORP_CLIENT.name())))
                                                  .contentType(MediaType.APPLICATION_JSON))
                                 .andExpect(status().isOk())
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString(StandardCharsets.UTF_8);
        var result3 = objectMapper.readValue(contentAsString, IndicatorDateInfo.class);
        assertEquals(2024, result3.year());
        assertEquals(Month.JUNE, result3.month());
    }
    
    @Test
    @DisplayName("Внесение показателей по выбранному транспорту")
    @SneakyThrows
    void updateIndicators() {
        Mockito.reset(manager);
        doNothing().when(transportSender).send(Mockito.any(Transport.class), anyBoolean());
        AuthorizeUtils.authorize(manager, Role.ROLE_ENGINEER_CORP_CLIENT.name());
        var dto1 = new Indicators(2024, Month.MAY, 300, 500);
        mockMvc.perform(patch(ROOT_PATH + "/" + TRANSPORT_1)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ENGINEER_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto1)))
               .andExpect(status().isBadRequest())
               .andExpect(result2 -> assertInstanceOf(IndicatorsException.class, result2.getResolvedException()))
               .andExpect(result2 -> assertThat(Optional.ofNullable(result2.getResolvedException()).orElseThrow().getMessage()).
                       contains("Необходимо внести данные за год:2023, месяц:3"));
        var dto2 = new Indicators(2023, Month.MARCH, 300, 500);
        mockMvc.perform(patch(ROOT_PATH + "/" + TRANSPORT_1)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ENGINEER_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto2)))
               .andExpect(status().isOk());
        mockMvc.perform(patch(ROOT_PATH + "/" + TRANSPORT_1)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_2.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ENGINEER_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto2)))
               .andExpect(status().isBadRequest())
               .andExpect(result -> assertInstanceOf(NotAllowedUserException.class, result.getResolvedException()))
               .andExpect(result -> assertThat(Optional.ofNullable(result.getResolvedException()).orElseThrow().getMessage()).
                       contains("Попытка получить доступ к данным организаций без необходимых прав, " +
                                "организации:[fc73b25b-9564-4560-98b5-abc0f16af9b2], " +
                                "пользователь:01fab58c-ac99-45ef-a83e-f05b5a39d88d, " +
                                "организация пользователя:3cbe0b6b-fe04-4a28-83e8-184f0f331bff"));
        var dto3 = new Indicators(2023, Month.MAY, 300, 500);
        mockMvc.perform(patch(ROOT_PATH + "/" + TRANSPORT_2)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ENGINEER_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto3)))
               .andExpect(status().isBadRequest())
               .andExpect(result -> assertInstanceOf(StatusException.class, result.getResolvedException()))
               .andExpect(result -> assertThat(Optional.ofNullable(result.getResolvedException()).orElseThrow().getMessage()).
                       contains("Транспорт в статусе:Выведен из эксплуатации"));
        var dto4 = new Indicators(2023, Month.MARCH, 300, 500);
        mockMvc.perform(patch(ROOT_PATH + "/" + TRANSPORT_3)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ENGINEER_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto4)))
               .andExpect(status().isBadRequest())
               .andExpect(result2 -> assertInstanceOf(IndicatorsException.class, result2.getResolvedException()))
               .andExpect(result2 -> assertThat(Optional.ofNullable(result2.getResolvedException()).orElseThrow().getMessage()).
                       contains("Необходимо внести данные за год:2024, месяц:6"));
        var dto5 = new Indicators(2024, Month.JUNE, 300, 500);
        mockMvc.perform(patch(ROOT_PATH + "/" + TRANSPORT_3)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ENGINEER_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto5)))
               .andExpect(status().isOk());
        var dto6 = new Indicators(2024, Month.JULY, 400, 350);
        mockMvc.perform(patch(ROOT_PATH + "/" + TRANSPORT_3)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ENGINEER_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto6)))
               .andExpect(status().isBadRequest())
               .andExpect(result2 -> assertInstanceOf(IndicatorsException.class, result2.getResolvedException()))
               .andExpect(result2 -> assertThat(Optional.ofNullable(result2.getResolvedException()).orElseThrow().getMessage()).
                       contains("Показания одометра не могут быть меньше, чем ранее внесенные в систему"));
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Получение показателя одометра")
    void getIndicatorValue() {
        Mockito.reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_DRIVER.name());
        mockMvc.perform(get(ROOT_PATH + "/history/9ad7733b-c506-4984-b893-56d7022b705b")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_DRIVER.name()))))
               .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(14000));
    }
}