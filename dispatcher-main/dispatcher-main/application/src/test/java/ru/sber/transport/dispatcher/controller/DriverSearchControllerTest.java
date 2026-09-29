package ru.sber.transport.dispatcher.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.dispatcher.database.dao.AutoparkRepository;
import ru.sber.transport.dispatcher.database.dao.ContractorRepository;
import ru.sber.transport.dispatcher.database.dao.DriverRepository;
import ru.sber.transport.dispatcher.database.model.Autopark;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.database.model.DriverLicense;
import ru.sber.transport.dispatcher.dto.DriverDTO;
import ru.sber.transport.dispatcher.dto.DriverLicenseDto;
import ru.sber.transport.dispatcher.dto.search.DriverSearchDTO;
import ru.sber.transport.dispatcher.testutils.TestAutoparks;
import ru.sber.transport.dispatcher.testutils.TestContractors;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера водителей")
@MockBean(Key.class)
class DriverSearchControllerTest extends KafkaTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContractorRepository contractorRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private AutoparkRepository autoparkRepository;

    @MockBean
    private AuthorizationManager<?> manager;

    private Contractor contractor;
    private Autopark autopark;
    private Driver driver1;
    private Driver driver2;

    private Driver driver3;
    private Driver driver4;

    @BeforeEach
    public void createRepository() {
        contractor = contractorRepository.save(TestContractors.createTestContractor());
        autopark = autoparkRepository.save(TestAutoparks.createTestAutopark(contractor));

        driver1 = Driver.builder().
                contactPhone("+780090011").
                humanReadableId("DR-0001-001").
                driverLicenseNumber("12 34 1111").
                serviceLicenseNumber("AAA-12-111111").
                firstName("FirstNameOne").
                lastName("LastNameOne").
                patronymic("PatronymicOne").
                passport("1111 1111").
                rating(400).
                active(true).
                build();
        driver1.setContractor(contractor);

        driver2 = Driver.builder().
                contactPhone("+780090012").
                humanReadableId("DR-0001-002").
                driverLicenseNumber("12 34 1112").
                serviceLicenseNumber("AAA-12-222222").
                firstName("FirstNameTwo").
                lastName("LastNameTwo").
                patronymic("PatronymicTwo").
                passport("1111 111Two").
                rating(500).
                active(false).
                build();
        driver2.setContractor(contractor);

        driver3 = Driver.builder().
                contactPhone("+780090013").
                humanReadableId("DR-0001-003").
                driverLicenseNumber("12 34 1113").
                serviceLicenseNumber("AAA-12-333333").
                firstName("FirstNameThree").
                lastName("LastNameThree").
                patronymic("PatronymicThree").
                passport("1111 1113").
                rating(500).
                active(false).
                build();
        driver3.setContractor(contractor);

        driver4 = Driver.builder().
                contactPhone("+780090014").
                humanReadableId("DR-0001-004").
                driverLicenseNumber("12 34 1114").
                serviceLicenseNumber("AAA-12-444444").
                firstName("FirstNameFour").
                lastName("LastNameFour").
                patronymic("PatronymicFour").
                passport("1111 1114").
                rating(500).
                active(false).
                build();
        driver4.setContractor(contractor);

        driverRepository.save(driver1);
        driverRepository.save(driver2);
        driverRepository.save(driver3);
        driverRepository.save(driver4);
    }

    @Data
    public static class DriverContent {
        public List<DriverDTO> content;
    }

    @Test
    @DisplayName("Поиск по человекочитаемому идентификатору водителя")
    void searchByHumanReadableId() throws Exception {
        var response = mockMvc.perform(get("/%s/drivers/?driverHumanId=%s".formatted(contractor.getId(), driver1.getHumanReadableId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<DriverContent>() {
                });

        var content = actual.content;
        assertThat(content).hasSize(1);
        assertThat(content.get(0).id()).isEqualTo(driver1.getId());
    }

    @Test
    @DisplayName("Поиск по человекочитаемому идентификатору водителя - не найдено")
    void searchByHumanReadableIdNotFound() throws Exception {
        var response = mockMvc.perform(get("/%s/drivers/?driverHumanId=XX-YYYY-ZZZ".formatted(contractor.getId().toString()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<DriverContent>() {
                });

        var content = actual.content;
        assertThat(content).isEmpty();
    }

    @Test
    @DisplayName("Поиск по списку автопарков водителя")
    @Disabled("Уточняются БТ по автопаркам")
    void searchByAutoparkSet() throws Exception {
        var response = mockMvc.perform(get(String.format("/%s/drivers?autoparks=%s", contractor.getId(), autopark.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<DriverContent>() {
                });

        var content = actual.content;
        assertThat(content).hasSize(1);
        assertThat(content.get(0).id()).isEqualTo(driver1.getId());
    }

    @Test
    @DisplayName("Поиск по ФИО водителя")
    void searchByFullNameDriver() throws Exception {
        var response = mockMvc.perform(get("/%s/drivers/?driverFullName=%s".formatted(contractor.getId(), driver2.getFirstName() + " " + driver2.getLastName() + " " + driver2.getPatronymic()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<DriverContent>() {
                });

        var content = actual.content;
        assertThat(content).hasSize(1);
        assertThat(content.get(0).id()).isEqualTo(driver2.getId());
    }

    @Test
    @DisplayName("Поиск по фамилии водителя")
    void searchByLastNameDriver() throws Exception {
        var response = mockMvc.perform(get("/%s/drivers/?driverFullName=%s".formatted(contractor.getId(), driver2.getLastName()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<DriverContent>() {
                });

        var content = actual.content;
        assertThat(content).hasSize(1);
        assertThat(content.get(0).id()).isEqualTo(driver2.getId());
    }

    @Test
    @DisplayName("Поиск по имени и фамилии водителя")
    void searchByFirstLastNameDriver() throws Exception {
        var response = mockMvc.perform(get("/%s/drivers/?driverFullName=%s".formatted(contractor.getId(), driver2.getFirstName() + " " + driver2.getLastName()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<DriverContent>() {
                });

        var content = actual.content;
        assertThat(content).hasSize(1);
        assertThat(content.get(0).id()).isEqualTo(driver2.getId());
    }

    @Test
    @DisplayName("Поиск по активному водителю")
    void searchByActiveDriver() throws Exception {
        var response = mockMvc.perform(get("/%s/drivers/?isActive=true".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<DriverContent>() {
                });

        var content = actual.content;
        assertThat(content).hasSize(1);
    }

    @Test
    @DisplayName("Поиск по рейтингу водителя")
    void searchByRatingDriver() throws Exception {
        var response = mockMvc.perform(get("/%s/drivers/?ratingFrom=500".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<DriverContent>() {
                });

        List<DriverDTO> content = actual.content;
        assertThat(content).hasSize(3);
    }

    @Test
    @DisplayName("Поиск по одной категории прав водителя")
    void searchByLicenseClassDriver() throws Exception {
        driver1.getDriverLicenses().add(DriverLicense.A);
        driver2.getDriverLicenses().add(DriverLicense.A);
        driverRepository.save(driver1);
        driverRepository.save(driver2);
        var response = mockMvc.perform(get("/%s/drivers/?driverLicenses=A".formatted(contractor.getId().toString()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<DriverContent>() {
                });

        var content = actual.content;
        assertThat(content).hasSize(2);
    }

    @Test
    @DisplayName("Поиск по нескольким категориям прав водителя")
    void searchBySomeLicenseClassDriver() throws Exception {
        driver1.getDriverLicenses().add(DriverLicense.A);
        driver2.getDriverLicenses().add(DriverLicense.A);
        driver2.getDriverLicenses().add(DriverLicense.B);
        driver3.getDriverLicenses().add(DriverLicense.B);
        driver3.getDriverLicenses().add(DriverLicense.C);
        driver4.getDriverLicenses().add(DriverLicense.C);
        driver4.getDriverLicenses().add(DriverLicense.D);

        driverRepository.save(driver1);
        driverRepository.save(driver2);
        driverRepository.save(driver3);
        driverRepository.save(driver4);
        var response = mockMvc.perform(get("/%s/drivers/?driverLicenses=A&driverLicenses=B".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<DriverContent>() {
                });

        var content = actual.content;
        assertThat(content).hasSize(3);
    }

    @Test
    @DisplayName("Поиск c несуществующим контрагентом")
    void searchWithRandomContractor() throws Exception {
        DriverSearchDTO driverSearchDTO = new DriverSearchDTO();
        driverSearchDTO.setDriverLicenses(new ArrayList<>() {{
            add(DriverLicenseDto.B);
        }});

        String driverSearch = objectMapper.writeValueAsString(driverSearchDTO);

        mockMvc.perform(post(String.format("/%s/drivers", UUID.randomUUID())).content(driverSearch)
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .header("Authorization", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}