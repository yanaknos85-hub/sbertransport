package ru.sber.transport.constants.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.constants.dto.*;
import ru.sberbank.ditsib.transport.constants.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_constants")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера констант")
@SpringBootTest
@MockitoBean(types = JwtDecoder.class)
class ConstantsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Получение всех типов транспорта")
    void test_getTransportTypes() throws Exception {

        var response = mockMvc.perform(
                        get("/transport-types")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt())
                )
                .andExpect(status().isOk()).andReturn();
        List<TransportTypeDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        TransportTypeEnum[] expected = TransportTypeEnum.values();
        assertEquals(expected.length, actual.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].getName(), actual.get(i).name());
            assertEquals(expected[i].getRusName(), actual.get(i).rusName());
        }
    }

    @Test
    @DisplayName("Получение всех опций заявок")
    void test_getRequestOptions() throws Exception {

        var response = mockMvc.perform(
                        get("/request-options")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt())

                )
                .andExpect(status().isOk()).andReturn();
        List<RequestOptionsDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        RequestOptions[] expected = RequestOptions.values();
        assertEquals(expected.length, actual.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].toString(), actual.get(i).name());
            assertEquals(expected[i].getDescription(), actual.get(i).rusName());
        }
    }

    @Test
    @DisplayName("Получение всех статусов заявок")
    void test_getStatuses() throws Exception {

        var response = mockMvc.perform(
                        get("/status")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt())
                )
                .andExpect(status().isOk()).andReturn();
        List<RequestStatusDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        TripRequestStatus[] expected = TripRequestStatus.values();
        assertEquals(expected.length, actual.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].toString(), actual.get(i).name());
            assertEquals(expected[i].getDescription(), actual.get(i).rusName());
        }
    }

    @Test
    @DisplayName("Получение всех статусов заявок такси")
    void test_getTaxiStatuses() throws Exception {

        var response = mockMvc.perform(
                        get("/status/taxi")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt())
                )
                .andExpect(status().isOk()).andReturn();
        List<RequestStatusDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        List<TripRequestStatus> expected = TripRequestStatus.TAXI_STATUSES;
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).toString(), actual.get(i).name());
            assertEquals(expected.get(i).getDescription(), actual.get(i).rusName());
            assertEquals(expected.get(i).isTerminal(), actual.get(i).finalStatus());
            assertEquals(expected.get(i).getColor(), actual.get(i).color());
        }
    }

    @Test
    @DisplayName("Получение всех статусов заявок группового трансфера")
    void test_getGroupTransferStatuses() throws Exception {

        var response = mockMvc.perform(
                        get("/status/group_transfer")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt())
                )
                .andExpect(status().isOk()).andReturn();
        List<RequestStatusDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        List<TripRequestStatus> expected = TripRequestStatus.GROUP_TRANSFER_STATUSES;
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).toString(), actual.get(i).name());
            assertEquals(expected.get(i).getDescription(), actual.get(i).rusName());
            assertEquals(expected.get(i).isTerminal(), actual.get(i).finalStatus());
            assertEquals(expected.get(i).getColor(), actual.get(i).color());
        }
    }

    @Test
    @DisplayName("Получение всех статусов заявок на каршеринг")
    void test_getCarsharinStatuses() throws Exception {

        var response = mockMvc.perform(
                        get("/status/carsharing")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt())
                )
                .andExpect(status().isOk()).andReturn();
        List<RequestStatusDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        List<TripRequestStatus> expected = TripRequestStatus.CARSHARING_STATUSES;
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).toString(), actual.get(i).name());
            assertEquals(expected.get(i).getDescription(), actual.get(i).rusName());
            assertEquals(expected.get(i).isTerminal(), actual.get(i).finalStatus());
            assertEquals(expected.get(i).getColor(), actual.get(i).color());
        }
    }

    @Test
    @DisplayName("Получение всех статусов заявок на грузовом транспорте")
    void test_getCargoStatuses() throws Exception {

        var response = mockMvc.perform(
                        get("/status/cargo")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt())
                )
                .andExpect(status().isOk()).andReturn();
        List<RequestStatusDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        List<TripRequestStatus> expected = TripRequestStatus.CARGO_STATUSES;
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).toString(), actual.get(i).name());
            assertEquals(expected.get(i).getDescription(), actual.get(i).rusName());
            assertEquals(expected.get(i).isTerminal(), actual.get(i).finalStatus());
            assertEquals(expected.get(i).getColor(), actual.get(i).color());
        }
    }

    @Test
    @DisplayName("Получение всех статусов заявок на ЛТ")
    void test_getPersonalStatuses() throws Exception {

        var response = mockMvc.perform(
                        get("/status/personal")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt())
                )
                .andExpect(status().isOk()).andReturn();
        List<RequestStatusDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        List<TripRequestStatus> expected = TripRequestStatus.PERSONAL_STATUSES;
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).toString(), actual.get(i).name());
            assertEquals(expected.get(i).getDescription(), actual.get(i).rusName());
            assertEquals(expected.get(i).isTerminal(), actual.get(i).finalStatus());
            assertEquals(expected.get(i).getColor(), actual.get(i).color());
        }
    }

    @Test
    @DisplayName("Получение всех статусов заявок на ОТ")
    void test_getPublicStatuses() throws Exception {

        var response = mockMvc.perform(
                        get("/status/public")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt())
                )
                .andExpect(status().isOk()).andReturn();
        List<RequestStatusDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        List<TripRequestStatus> expected = TripRequestStatus.PUBLIC_STATUSES;
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).toString(), actual.get(i).name());
            assertEquals(expected.get(i).getDescription(), actual.get(i).rusName());
            assertEquals(expected.get(i).isTerminal(), actual.get(i).finalStatus());
            assertEquals(expected.get(i).getColor(), actual.get(i).color());
        }
    }

    @Test
    @DisplayName("Получение всех Типов компенсации за поездки на общественном транспорте")
    void test_getPublicCompensationTypes() throws Exception {
        var response = mockMvc.perform(get("/public-compensation-types")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt())
                )
                .andExpect(status().isOk()).andReturn();
        List<PublicCompensationTypeDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        List<PublicCompensationType> expected = Arrays.asList(PublicCompensationType.values());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).toString(), actual.get(i).name());
            assertEquals(expected.get(i).getRusName(), actual.get(i).rusName());
        }
    }

    @Test
    @DisplayName("Получение всех типов транспорта")
    void test_getAllTransportTypes() throws Exception {

        var response = mockMvc.perform(
                        get("/transport-types")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt())
                )
                .andExpect(status().isOk()).andReturn();
        List<TransportTypeDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        TransportTypeEnum[] expected = TransportTypeEnum.values();
        assertEquals(expected.length, actual.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].getId(), actual.get(i).id());
            assertEquals(expected[i].getName(), actual.get(i).name());
            assertEquals(expected[i].getRusName(), actual.get(i).rusName());
        }
    }

    @Test
    @DisplayName("Получение всех допустимых значений по информация о собственнике ТС")
    void test_getAllOwnerInfoDTO() throws Exception {

        var response = mockMvc.perform(
                        get("/owner-info")
                                .with(jwt())
                )
                .andExpect(status().isOk()).andReturn();
        List<PersonalCarOwnerInfoEnumDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        PersonalCarOwnerInfo[] expected = PersonalCarOwnerInfo.values();
        assertEquals(expected.length, actual.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].toString(), actual.get(i).name());
            assertEquals(expected[i].getRusName(), actual.get(i).rusName());
        }
    }

    @Test
    @DisplayName("Получение всех типов транспорта")
    void test_getAllPersonalTransportTypes() throws Exception {

        var response = mockMvc.perform(
                        get("/personal-transport-types")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt())
                )
                .andExpect(status().isOk()).andReturn();
        List<PersonalTransportTypeDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        PersonalTransportType[] expected = PersonalTransportType.values();
        assertEquals(expected.length, actual.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].toString(), actual.get(i).name());
            assertEquals(expected[i].getRusName(), actual.get(i).rusName());
        }
    }


    @Test
    @DisplayName("Получение всех типов грузового транспорта")
    void test_getAllCargoTransportTypes() throws Exception {

        var response = mockMvc.perform(
                        get("/cargo-transport-types")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt())
                )
                .andExpect(status().isOk()).andReturn();
        List<CargoTransportTypeDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        var expected = TransportTypeEnum.getByServiceType(TransportServiceType.CARGO_TRANSPORTATION).stream()
                .map(type -> new CargoTransportTypeDTO(type.name(), type.getRusName())).toList();
        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).name(), actual.get(i).name());
            assertEquals(expected.get(i).rusName(), actual.get(i).rusName());
        }
    }
}
