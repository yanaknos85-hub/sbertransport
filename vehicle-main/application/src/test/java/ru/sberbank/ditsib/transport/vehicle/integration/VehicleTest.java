package ru.sberbank.ditsib.transport.vehicle.integration;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.sberbank.ditsib.transport.vehicle.BaseIntegrationTest;
import ru.sberbank.ditsib.transport.vehicle.constants.Role;
import ru.sberbank.ditsib.transport.vehicle.database.model.Vehicle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/scripts/vehicle_integration_test.sql")
class VehicleTest extends BaseIntegrationTest {

    private static final String ROOT_PATH = "/vehicle";

    private static final String LADA_2114 = """
                     {
                      "modelId": "c8489954-548b-4a44-a621-aabce28da500",
                      "manufacturer": "ВАЗ",
                      "ecologicalClass": "4",
                      "enginePower": 240.33,
                      "engineCapacity": 1400,
                      "fuelTankVolume": 50,
                      "fuelTypeIds": ["67a89599-fa56-4329-b671-981f78883319"],
                      "categoryId": "be8abe82-1cbd-4f0e-9b31-2b9db90231df",
                      "driveId": "409570dc-355c-4423-8f53-8ef1f945e345",
                      "mudguardInstalled": false,
                      "spareWheelHolderInstalled": true,
                      "weight": 700,
                      "maxWeight": 900,
                      "height": 1400,
                      "width": 1650,
                      "length": 4122,
                      "serviceIntervalMileage": 10000,
                      "serviceIntervalDays": 180,
                      "serviceAuthorizationDays": 10,
                      "serviceAuthorizationMileage": 2000,
                      "bodyTypeId": "b0a5f7e7-8c63-487e-a452-3bb617a29faa",
                      "transmissionTypeId": "b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a",
                      "frontWheelSizeId": "1fd0a13d-6bd9-4c8c-8bc9-cd029034d4df",
                      "rearWheelSizeId": "1fd0a13d-6bd9-4c8c-8bc9-cd029034d4df",
                      "yearManufactureBegin": 1980,
                      "yearManufactureEnd": null,
                      "engineTypeId": "773b5013-ac57-45e9-9d0b-75221c3ce333",
                      "cityConsumptionRate": 10.5,
                      "countryConsumptionRate": 10.5,
                      "hybridConsumptionRate": 10.5
                    }
            """;
    private static final String CHANGAN_V90 = """
                     {
                      "modelId": "1ba85b47-c1fb-4f36-a9b2-762426fc1fce",
                      "manufacturer": "uncle Lyao",
                      "ecologicalClass": "5",
                      "enginePower": 9999.99,
                      "engineCapacity": 2400,
                      "fuelTankVolume": 55,
                      "fuelTypeIds": ["6a0dc332-9ec0-4bb6-ab75-1c8601564765", "67a89599-fa56-4329-b671-981f78883319"],
                      "categoryId": "be8abe82-1cbd-4f0e-9b31-2b9db90231df",
                      "driveId": "fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632",
                      "mudguardInstalled": true,
                      "spareWheelHolderInstalled": false,
                      "weight": 1100,
                      "maxWeight": 1300,
                      "height": 1600,
                      "width": 1850,
                      "length": 4522,
                      "serviceIntervalMileage": 10000,
                      "serviceIntervalDays": 180,
                      "serviceAuthorizationDays": 10,
                      "serviceAuthorizationMileage": 2000,
                      "bodyTypeId": "16e717da-d462-4d22-b5d2-fbfdcc489445",
                      "transmissionTypeId": "4fcd92db-415c-40b2-ada6-63d9b9409c2a",
                      "frontWheelSizeId": "b7a63d86-9afe-44b6-b672-123d11bb8ae8",
                      "rearWheelSizeId": "b7a63d86-9afe-44b6-b672-123d11bb8ae8",
                      "yearManufactureBegin": 2023,
                      "yearManufactureEnd": 2024,
                      "engineTypeId": "773b5013-ac57-45e9-9d0b-75221c3ce333",
                      "cityConsumptionRate": 10.5,
                      "countryConsumptionRate": 10.5,
                      "hybridConsumptionRate": 10.5
                    }
            """;

    @Test
    @DisplayName("Повторная вставка идентичного автомобиля должна возвращать ошибку")
    @SneakyThrows
    void preventDuplicates() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LADA_2114))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CHANGAN_V90))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LADA_2114))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Выборка укороченных данных автомобиля должна быть в алфавитном порядке по Марке и модели")
    @SneakyThrows
    void getAllVehicles() {
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LADA_2114))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CHANGAN_V90))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOT_PATH + "/all")
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "pageSetting": {
                                       "page": 0,
                                       "size": 10
                                    }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].brand", contains("Changan", "Лада")))
                .andExpect(jsonPath("$.content[*].model", contains("V90", "2114")))
                .andExpect(jsonPath("$.content[*].enginePower", contains(9999.99, 240.33)))
                .andExpect(jsonPath("$.content[*].engineType", contains("БЕНЗИН", "БЕНЗИН")))
                .andExpect(jsonPath("$.content[*].fuelType", contains("АИ-98, АИ-95+", "АИ-98")))
                .andExpect(jsonPath("$.content[*].fuelTankVolume", contains(55, 50)))
                .andExpect(jsonPath("$.content[*].engineCapacity", contains(2400, 1400)))
                .andExpect(jsonPath("$.content[*].drive", contains("Полный", "Передний")))
                .andExpect(jsonPath("$.content[*].spareWheelHolderInstalled", contains(false, true)))
                .andExpect(jsonPath("$.content[*].mudguardInstalled", contains(true, false)))
                .andExpect(jsonPath("$.content[*].bodyType", contains("Купе", "Седан")))
                .andExpect(jsonPath("$.content[*].transmissionType", contains("МКПП 7", "РКПП 5")))
                .andExpect(jsonPath("$.content[*].dimensions", contains("4522x1850x1600", "4122x1650x1400")))
                .andExpect(jsonPath("$.content[*].weight", contains(1100, 700)))
                .andExpect(jsonPath("$.content[*].manufacturePeriod", contains("2023 - 2024", "1980 - н.в.")));

    }

    @Test
    @DisplayName("Автомобиль имеющий связанные записи не может быть удалена")
    @SneakyThrows
    void deleteEntityWithRelations() {
        var vehicle = Instancio.create(Vehicle.class);
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
        doThrow(new DataIntegrityViolationException("")).when(vehicleRepository).delete(vehicle);
        mockMvc.perform(delete(ROOT_PATH + "/" + vehicle.getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Удаление невозможно в связи с наличием связных записей")));

    }

    @SneakyThrows
    @ParameterizedTest(name = "{0}")
    @MethodSource
    @Sql("/scripts/vehicle_integration_test.sql")
    @Sql("/scripts/vehicle_search_integration_test.sql")
    void searchVehicles(String testCase, String requestPayload, List<ResultMatcher> matchers) {
        var resultActions = mockMvc.perform(post(ROOT_PATH + "/search")
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_DATA_MASTER.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestPayload))
                .andExpect(status().isOk());

        for (var matcher : matchers) {
            resultActions.andExpect(matcher);
        }
    }

    static Stream<Arguments> searchVehicles() {
        return Stream.of(
                Arguments.of("При передаче пустого запроса должны возвращаться все записи",
                        "{}",
                        List.of(jsonPath("$.content", hasSize(4)),
                                jsonPath("$.content[*].id", containsInAnyOrder("748ba8ab-572a-4178-9e05-fae61aefb376",
                                        "748ba8ab-572a-4178-9e05-fae61aefb377",
                                        "400466cb-df0e-40be-8b34-9ff42444637a",
                                        "400466cb-df0e-40be-8b34-9ff42444637b")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("b0a5f7e7-8c63-487e-a452-3bb617a29faa", "16e717da-d462-4d22-b5d2-fbfdcc489445")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8", "773b5013-ac57-45e9-9d0b-75221c3ce333")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a", "4fcd92db-415c-40b2-ada6-63d9b9409c2a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632", "409570dc-355c-4423-8f53-8ef1f945e345")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(2500, 1400)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(9999.99, 240.33)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("1980 - н.в.", "1970 - 2023", "2021 - 2024", "2010 - н.в.")))),

                Arguments.of("Поиск с пагинацией должен находить только указанное количество записей",
                        """
                                   {
                                        "pageSetting": {
                                            "page": 0,
                                            "size": 2
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(2)),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("b0a5f7e7-8c63-487e-a452-3bb617a29faa", "16e717da-d462-4d22-b5d2-fbfdcc489445")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8", "773b5013-ac57-45e9-9d0b-75221c3ce333")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a", "4fcd92db-415c-40b2-ada6-63d9b9409c2a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632", "409570dc-355c-4423-8f53-8ef1f945e345")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(2500, 1400)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(9999.99, 240.33)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("1980 - н.в.", "1970 - 2023", "2021 - 2024", "2010 - н.в.")))),

                Arguments.of("Поиск по бренду",
                        """
                                   {
                                        "vehicle": {
                                            "brand": "63c33e3a-3bf7-471f-9d13-16bcf3abeeb7"
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(2)),
                                jsonPath("$.content[*].id", containsInAnyOrder("400466cb-df0e-40be-8b34-9ff42444637a", "400466cb-df0e-40be-8b34-9ff42444637b")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("b0a5f7e7-8c63-487e-a452-3bb617a29faa")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8", "773b5013-ac57-45e9-9d0b-75221c3ce333")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632", "409570dc-355c-4423-8f53-8ef1f945e345")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(2500)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(9999.99)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("2021 - 2024", "2010 - н.в.")))),

                Arguments.of("Поиск по модели",
                        """
                                   {
                                        "vehicle": {
                                            "model": "c8489954-548b-4a44-a621-aabce28da500"
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(2)),
                                jsonPath("$.content[*].id", containsInAnyOrder("748ba8ab-572a-4178-9e05-fae61aefb376", "748ba8ab-572a-4178-9e05-fae61aefb377")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("16e717da-d462-4d22-b5d2-fbfdcc489445")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8", "773b5013-ac57-45e9-9d0b-75221c3ce333")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("4fcd92db-415c-40b2-ada6-63d9b9409c2a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("409570dc-355c-4423-8f53-8ef1f945e345", "fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(1400)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(240.33)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("1980 - н.в.", "1970 - 2023")))),

                Arguments.of("Поиск по бренду и модели",
                        """
                                   {
                                        "vehicle": {
                                            "brand": "63c33e3a-3bf7-471f-9d13-16bcf3abeeb7",
                                            "model": "1ba85b47-c1fb-4f36-a9b2-762426fc1fce"
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(2)),
                                jsonPath("$.content[*].id", containsInAnyOrder("400466cb-df0e-40be-8b34-9ff42444637a", "400466cb-df0e-40be-8b34-9ff42444637b")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("b0a5f7e7-8c63-487e-a452-3bb617a29faa")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8", "773b5013-ac57-45e9-9d0b-75221c3ce333")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("409570dc-355c-4423-8f53-8ef1f945e345", "fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(2500)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(9999.99)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("2021 - 2024", "2010 - н.в.")))),

                Arguments.of("Поиск по типу двигателя",
                        """
                                   {
                                        "engine": {
                                            "engineType": "773b5013-ac57-45e9-9d0b-75221c3ce333"
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(2)),
                                jsonPath("$.content[*].id", containsInAnyOrder("400466cb-df0e-40be-8b34-9ff42444637b", "748ba8ab-572a-4178-9e05-fae61aefb376")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("b0a5f7e7-8c63-487e-a452-3bb617a29faa", "16e717da-d462-4d22-b5d2-fbfdcc489445")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("773b5013-ac57-45e9-9d0b-75221c3ce333")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a", "4fcd92db-415c-40b2-ada6-63d9b9409c2a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("409570dc-355c-4423-8f53-8ef1f945e345")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(2500, 1400)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(9999.99, 240.33)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("1980 - н.в.", "2010 - н.в.")))),

                Arguments.of("Поиск по приводу",
                        """
                                   {
                                        "engine": {
                                            "drive": "fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632"
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(2)),
                                jsonPath("$.content[*].id", containsInAnyOrder("400466cb-df0e-40be-8b34-9ff42444637a", "748ba8ab-572a-4178-9e05-fae61aefb377")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("b0a5f7e7-8c63-487e-a452-3bb617a29faa", "16e717da-d462-4d22-b5d2-fbfdcc489445")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a", "4fcd92db-415c-40b2-ada6-63d9b9409c2a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(2500, 1400)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(9999.99, 240.33)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("1970 - 2023", "2021 - 2024")))),

                Arguments.of("Поиск по типу двигателя и приводу",
                        """
                                   {
                                        "engine": {
                                            "engineType": "2717f985-4a59-403f-92a2-ba0e20de20a8",
                                            "drive": "fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632"
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(2)),
                                jsonPath("$.content[*].id", containsInAnyOrder("400466cb-df0e-40be-8b34-9ff42444637a", "748ba8ab-572a-4178-9e05-fae61aefb377")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("b0a5f7e7-8c63-487e-a452-3bb617a29faa", "16e717da-d462-4d22-b5d2-fbfdcc489445")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a", "4fcd92db-415c-40b2-ada6-63d9b9409c2a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(2500, 1400)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(9999.99, 240.33)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("1970 - 2023", "2021 - 2024")))),

                Arguments.of("Поиск по всем параметрам",
                        """
                                   {
                                        "engine": {
                                            "engineType": "2717f985-4a59-403f-92a2-ba0e20de20a8",
                                            "drive": "fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632",
                                            "transmissionType": "b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a",
                                            "engineCapacity": 2500,
                                            "enginePower": 9999.99
                                        },
                                        "vehicle": {
                                            "brand": "63c33e3a-3bf7-471f-9d13-16bcf3abeeb7",
                                            "model": "1ba85b47-c1fb-4f36-a9b2-762426fc1fce",
                                            "manufacturePeriod": "2021 - 2024",
                                            "manufactureYear": 2023,
                                            "bodyType": "b0a5f7e7-8c63-487e-a452-3bb617a29faa"
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(1)),
                                jsonPath("$.content[*].id", containsInAnyOrder("400466cb-df0e-40be-8b34-9ff42444637a")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("b0a5f7e7-8c63-487e-a452-3bb617a29faa")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(2500)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(9999.99)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("2021 - 2024")))),

                Arguments.of("Поиск не дал результатов",
                        """
                                   {
                                        "engine": {
                                            "engineType": "2717f985-4a59-403f-92a2-ba0e20de20a9",
                                            "drive": "fe07b7eb-7aa0-4ad0-ae43-d91b8d85d635"
                                        },
                                        "vehicle": {
                                            "brand": "63c33e3a-3bf7-471f-9d13-16bcf3abeeb6",
                                            "model": "1ba85b47-c1fb-4f36-a9b2-762426fc1fc3"
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(0)),
                                jsonPath("$.filters.bodyType", hasSize(0)),
                                jsonPath("$.filters.engineType", hasSize(0)),
                                jsonPath("$.filters.transmissionType", hasSize(0)),
                                jsonPath("$.filters.driveType", hasSize(0)),
                                jsonPath("$.filters.engineCapacity", hasSize(0)),
                                jsonPath("$.filters.enginePower", hasSize(0)),
                                jsonPath("$.filters.manufacturePeriod", hasSize(0)))),

                Arguments.of("Поиск по мощности двигателя",
                        """
                                   {
                                        "engine": {
                                            "enginePower": 9999.99
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(2)),
                                jsonPath("$.content[*].id", containsInAnyOrder("400466cb-df0e-40be-8b34-9ff42444637a", "400466cb-df0e-40be-8b34-9ff42444637b")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("b0a5f7e7-8c63-487e-a452-3bb617a29faa")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8", "773b5013-ac57-45e9-9d0b-75221c3ce333")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632", "409570dc-355c-4423-8f53-8ef1f945e345")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(2500)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(9999.99)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("2021 - 2024", "2010 - н.в.")))),

                Arguments.of("Поиск по трансмиссиии",
                        """
                                   {
                                        "engine": {
                                            "transmissionType": "b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a"
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(2)),
                                jsonPath("$.content[*].id", containsInAnyOrder("400466cb-df0e-40be-8b34-9ff42444637a", "400466cb-df0e-40be-8b34-9ff42444637b")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("b0a5f7e7-8c63-487e-a452-3bb617a29faa")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8", "773b5013-ac57-45e9-9d0b-75221c3ce333")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632", "409570dc-355c-4423-8f53-8ef1f945e345")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(2500)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(9999.99)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("2021 - 2024", "2010 - н.в.")))),

                Arguments.of("Поиск по объему двигателя",
                        """
                                   {
                                        "engine": {
                                            "engineCapacity": 2500
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(2)),
                                jsonPath("$.content[*].id", containsInAnyOrder("400466cb-df0e-40be-8b34-9ff42444637a", "400466cb-df0e-40be-8b34-9ff42444637b")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("b0a5f7e7-8c63-487e-a452-3bb617a29faa")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8", "773b5013-ac57-45e9-9d0b-75221c3ce333")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632", "409570dc-355c-4423-8f53-8ef1f945e345")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(2500)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(9999.99)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("2021 - 2024", "2010 - н.в.")))),

                Arguments.of("Поиск по году производства при вхождении в период с открытой датой",
                        """
                                   {
                                        "vehicle": {
                                            "manufactureYear": 2050
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(2)),
                                jsonPath("$.content[*].id", containsInAnyOrder("748ba8ab-572a-4178-9e05-fae61aefb376", "400466cb-df0e-40be-8b34-9ff42444637b")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("b0a5f7e7-8c63-487e-a452-3bb617a29faa", "16e717da-d462-4d22-b5d2-fbfdcc489445")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("773b5013-ac57-45e9-9d0b-75221c3ce333")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a", "4fcd92db-415c-40b2-ada6-63d9b9409c2a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("409570dc-355c-4423-8f53-8ef1f945e345")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(2500, 1400)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(9999.99, 240.33)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("1980 - н.в.", "2010 - н.в.")))),

                Arguments.of("Поиск по году производства при попадании на дату начала производства",
                        """
                                       {
                                            "vehicle": {
                                            "manufactureYear": 1970
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(1)),
                                jsonPath("$.content[*].id", containsInAnyOrder("748ba8ab-572a-4178-9e05-fae61aefb377")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("16e717da-d462-4d22-b5d2-fbfdcc489445")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("4fcd92db-415c-40b2-ada6-63d9b9409c2a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(1400)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(240.33)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("1970 - 2023")))),

                Arguments.of("Поиск по году производства при попадании на дату окончания производства",
                        """
                                   {
                                        "vehicle": {
                                            "manufactureYear": 2024
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(3)),
                                jsonPath("$.content[*].id", containsInAnyOrder("400466cb-df0e-40be-8b34-9ff42444637b",
                                        "748ba8ab-572a-4178-9e05-fae61aefb376",
                                        "400466cb-df0e-40be-8b34-9ff42444637a")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("b0a5f7e7-8c63-487e-a452-3bb617a29faa", "16e717da-d462-4d22-b5d2-fbfdcc489445")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8", "773b5013-ac57-45e9-9d0b-75221c3ce333")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a", "4fcd92db-415c-40b2-ada6-63d9b9409c2a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632", "409570dc-355c-4423-8f53-8ef1f945e345")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(2500, 1400)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(9999.99, 240.33)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("1980 - н.в.", "2021 - 2024", "2010 - н.в.")))),

                Arguments.of("Поиск по году производства при попадании в период производства с закрытой датой",
                        """
                                   {
                                        "vehicle": {
                                            "manufactureYear": 1975
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(1)),
                                jsonPath("$.content[*].id", containsInAnyOrder("748ba8ab-572a-4178-9e05-fae61aefb377")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("16e717da-d462-4d22-b5d2-fbfdcc489445")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("4fcd92db-415c-40b2-ada6-63d9b9409c2a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(1400)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(240.33)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("1970 - 2023")))),

                Arguments.of("Поиск по типу кузова",
                        """
                                   {
                                        "vehicle": {
                                            "bodyType": "b0a5f7e7-8c63-487e-a452-3bb617a29faa"
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(2)),
                                jsonPath("$.content[*].id", containsInAnyOrder("400466cb-df0e-40be-8b34-9ff42444637a", "400466cb-df0e-40be-8b34-9ff42444637b")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("b0a5f7e7-8c63-487e-a452-3bb617a29faa")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8", "773b5013-ac57-45e9-9d0b-75221c3ce333")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("b4ec0b58-d1b3-45b0-ad36-0cf0bfd50a0a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632", "409570dc-355c-4423-8f53-8ef1f945e345")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(2500)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(9999.99)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("2021 - 2024", "2010 - н.в.")))),

                Arguments.of("Поиск по периоду производства с закрытой датой снятия с производства",
                        """
                                   {
                                        "vehicle": {
                                            "manufacturePeriod": "1970 - 2023"
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(1)),
                                jsonPath("$.content[*].id", contains("748ba8ab-572a-4178-9e05-fae61aefb377")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("16e717da-d462-4d22-b5d2-fbfdcc489445")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("2717f985-4a59-403f-92a2-ba0e20de20a8")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("4fcd92db-415c-40b2-ada6-63d9b9409c2a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("fe07b7eb-7aa0-4ad0-ae43-d91b8d85d632")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(1400)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(240.33)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("1970 - 2023")))),

                Arguments.of("Поиск по периоду производства с закрытой датой снятия с производства",
                        """
                                   {
                                        "vehicle": {
                                            "manufacturePeriod": "1980 - н.в."
                                        }
                                    }
                                """,
                        List.of(jsonPath("$.content", hasSize(1)),
                                jsonPath("$.content[*].id", contains("748ba8ab-572a-4178-9e05-fae61aefb376")),
                                jsonPath("$.filters.bodyType[*].id", containsInAnyOrder("16e717da-d462-4d22-b5d2-fbfdcc489445")),
                                jsonPath("$.filters.engineType[*].id", containsInAnyOrder("773b5013-ac57-45e9-9d0b-75221c3ce333")),
                                jsonPath("$.filters.transmissionType[*].id", containsInAnyOrder("4fcd92db-415c-40b2-ada6-63d9b9409c2a")),
                                jsonPath("$.filters.driveType[*].id", containsInAnyOrder("409570dc-355c-4423-8f53-8ef1f945e345")),
                                jsonPath("$.filters.engineCapacity[*]", containsInAnyOrder(1400)),
                                jsonPath("$.filters.enginePower[*]", containsInAnyOrder(240.33)),
                                jsonPath("$.filters.manufacturePeriod[*]", containsInAnyOrder("1980 - н.в."))))
        );
    }
}
