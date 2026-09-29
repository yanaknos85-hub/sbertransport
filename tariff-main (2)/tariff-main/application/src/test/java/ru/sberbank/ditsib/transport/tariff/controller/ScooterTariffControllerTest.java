package ru.sberbank.ditsib.transport.tariff.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.tariff.messaging.TariffMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.ScooterTariffRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.BaseTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.database.model.ScooterTariff;
import ru.sberbank.ditsib.transport.tariff.dto.NewScooterTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.ScooterTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.TimedTariffParamsDTO;
import ru.sberbank.ditsib.transport.tariff.dto.mapper.EntityDTOMapper;

import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SuppressWarnings({ "OptionalGetWithoutIsPresent" })
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@Transactional
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка контроллера тарифов самоката")
@ActiveProfiles({"test", "kafka"})
class ScooterTariffControllerTest extends KafkaTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private EntityDTOMapper mapper;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ScooterTariffRepository scooterTariffRepository;
    
    private NewScooterTariffDTO testNewScooterTariffDTO1;
    
    protected Organization organization1;
    
    protected Organization organization2;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    @BeforeEach
    public void prepareData() {
        organization1 = Instancio.of(Organization.class)
                .set(field(Organization::getDigitId), 1L)
                .create();
        organization2 = Instancio.of(Organization.class)
                .set(field(Organization::getDigitId), 2L)
                .create();
        organizationRepository.saveAndFlush(organization1);
        organizationRepository.saveAndFlush(organization2);
    
        testNewScooterTariffDTO1 = NewScooterTariffDTO.builder()
                                                      .regionId(Collections.singleton(UUID.randomUUID()))
                                                      .organizationId(organization1.getId())
                                                      .transportType(TransportTypeEnum.SCOOTER)
    
                                                      .rideCostPerKm(100)
                                                      .rideCostPerMin(200)
                                                      .bookingCost(300)
                                                      .timedTariffParams(TimedTariffParamsDTO.builder()
                                                                                             .coefWorkDayMorning(1.)
                                                                                             .coefWorkDayNoon(2.)
                                                                                             .coefWorkDayEvening(3.)
                                                                                             .coefWorkDayNight(4.1)
                                                                                             .coefDayOff(5.1)
                                                                                             .build())
                                                      .coefInsurance(1.)
                                                      .build();
    }
    
    
    @Test
    @DisplayName("Добавление")
    @WithMockUser(roles = "GUEST")
    @Disabled("Требуется актуализация")
    void add() throws Exception {
        
        var request = objectMapper.writeValueAsString(testNewScooterTariffDTO1);
        
        var response = mockMvc.perform(
                                      post("/" + TransportTypeEnum.SCOOTER.getName().toLowerCase(Locale.ROOT)).contentType(MediaType.APPLICATION_JSON)
                                                                                                              .content(request))
                              .andExpect(status().isOk());
        
        assertThat(scooterTariffRepository.count()).isEqualTo(1);
        
        ScooterTariffDTO
                actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                ScooterTariffDTO.class);
        var expected = scooterTariffRepository.findAll().getFirst();
        
        assertNotNull(expected.getId());
        assertEquals(expected.getId(), actual.getId());
        assertNotNull(expected.getHumanReadableId());
        assertEquals(expected.getHumanReadableId(), actual.getHumanReadableId());
        assertNotNull(expected.getRegionId());
        assertTrue(actual.getRegionId().contains(expected.getRegionId()));
        assertNotNull(expected.getOrganization().getId());
        assertEquals(expected.getOrganization().getId(), actual.getOrganizationId());
        assertNotNull(expected.getRideCostPerKm());
        assertEquals(expected.getRideCostPerKm(), actual.getRideCostPerKm());
        assertNotNull(expected.getRideCostPerMin());
        assertEquals(expected.getRideCostPerMin(), actual.getRideCostPerMin());
        assertNotNull(expected.getBookingCost());
        assertEquals(expected.getBookingCost(), actual.getBookingCost());
        
        assertNotNull(expected.getTimedTariffParams());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayMorning(),
                     actual.getTimedTariffParams().getCoefWorkDayMorning());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayNoon(),
                     actual.getTimedTariffParams().getCoefWorkDayNoon());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayEvening(),
                     actual.getTimedTariffParams().getCoefWorkDayEvening());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayNight(),
                     actual.getTimedTariffParams().getCoefWorkDayNight());
        assertEquals(expected.getTimedTariffParams().getCoefDayOff(),
                     actual.getTimedTariffParams().getCoefDayOff());
        
        assertNotNull(expected.getCoefInsurance());
        assertEquals(expected.getCoefInsurance(), actual.getCoefInsurance());
        
        var message = consumeMessage("service.tariff", TariffMessage.class);
        var foundTariff = scooterTariffRepository.findById(actual.getId()).get();
        assertEquals(foundTariff.getHumanReadableId(), actual.getHumanReadableId());
        assertEquals(message.humanReadableId(), actual.getHumanReadableId());
        assertEquals(message.organizationId(), actual.getOrganizationId());
        assertTrue(message.humanReadableId().contains("TF-"));
        
    }
    
    
    @Test
    @DisplayName("Изменение")
    @WithMockUser(roles = "GUEST")
    @Disabled("Требуется актуализация")
    void edit() throws Exception {
        ScooterTariff saved = scooterTariffRepository.save(mapper.newDtoToScooterTariff(testNewScooterTariffDTO1));
        
        var expected = testNewScooterTariffDTO1.toBuilder()
                                               .rideCostPerKm(101)
                                               .rideCostPerMin(201)
                                               .bookingCost(301)
                                               .timedTariffParams(TimedTariffParamsDTO.builder()
                                                                                      .coefWorkDayMorning(1.1)
                                                                                      .coefWorkDayNoon(2.1)
                                                                                      .coefWorkDayEvening(3.1)
                                                                                      .coefWorkDayNight(4.1)
                                                                                      .coefDayOff(5.1)
                                                                                      .build())
                                               .coefInsurance(1.1)
                                               .build();
        
        var request = objectMapper.writeValueAsString(expected);
        
        mockMvc.perform(
                       put("/" + TransportTypeEnum.SCOOTER.getName().toLowerCase(Locale.ROOT) + "/" + saved.getId())
                               .contentType(MediaType.APPLICATION_JSON).content(request))
               .andExpect(status().isOk());
        
        ScooterTariff actual = scooterTariffRepository.findById(saved.getId()).get();
        
        assertNotNull(actual.getId());
        assertNotNull(actual.getHumanReadableId());
        assertNotNull(expected.getRegionId());
        assertTrue(expected.getRegionId().contains(actual.getRegionId()));
        assertNotNull(actual.getOrganization().getId());
        assertEquals(expected.getOrganizationId(), actual.getOrganization().getId());
        assertNotNull(expected.getRideCostPerKm());
        assertEquals(expected.getRideCostPerKm(), actual.getRideCostPerKm());
        assertNotNull(expected.getRideCostPerMin());
        assertEquals(expected.getRideCostPerMin(), actual.getRideCostPerMin());
        assertNotNull(expected.getBookingCost());
        assertEquals(expected.getBookingCost(), actual.getBookingCost());
        
        assertNotNull(expected.getTimedTariffParams());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayMorning());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayNoon());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayEvening());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayNight());
        assertNotNull(expected.getTimedTariffParams().getCoefDayOff());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayMorning(),
                     actual.getTimedTariffParams().getCoefWorkDayMorning());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayNoon(),
                     actual.getTimedTariffParams().getCoefWorkDayNoon());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayEvening(),
                     actual.getTimedTariffParams().getCoefWorkDayEvening());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayNight(),
                     actual.getTimedTariffParams().getCoefWorkDayNight());
        assertEquals(expected.getTimedTariffParams().getCoefDayOff(),
                     actual.getTimedTariffParams().getCoefDayOff());
        
        assertNotNull(expected.getCoefInsurance());
        assertEquals(expected.getCoefInsurance(), actual.getCoefInsurance());
        
        var message = consumeMessage("service.tariff", TariffMessage.class);
        assertEquals(saved.getHumanReadableId(), message.humanReadableId());
        assertEquals(saved.getOrganization().getId(), message.organizationId());
        assertTrue(message.humanReadableId().contains("TF-"));
    }
    
    @Test
    @DisplayName("Получение")
    @WithMockUser(roles = "GUEST")
    void getScooter() throws Exception {
        
        var expected = testNewScooterTariffDTO1;
        ScooterTariff saved = scooterTariffRepository.save(mapper.newDtoToScooterTariff(testNewScooterTariffDTO1));
        var request = objectMapper.writeValueAsString(expected);
        
        var response = mockMvc.perform(
                get("/" + TransportTypeEnum.SCOOTER.getName().toLowerCase(Locale.ROOT) + "/" +
                    saved.getId()).contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isOk());
        
        ScooterTariffDTO
                actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                ScooterTariffDTO.class);
        
        assertNotNull(actual.getId());
        assertNotNull(expected.getOrganizationId());
        assertEquals(expected.getOrganizationId(), actual.getOrganizationId());
        assertNotNull(expected.getRideCostPerKm());
        assertEquals(expected.getRideCostPerKm(), actual.getRideCostPerKm());
        assertNotNull(expected.getRideCostPerMin());
        assertEquals(expected.getRideCostPerMin(), actual.getRideCostPerMin());
        assertNotNull(expected.getBookingCost());
        assertEquals(expected.getBookingCost(), actual.getBookingCost());
        
        assertNotNull(expected.getTimedTariffParams());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayMorning());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayNoon());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayEvening());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayNight());
        assertNotNull(expected.getTimedTariffParams().getCoefDayOff());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayMorning(),
                     actual.getTimedTariffParams().getCoefWorkDayMorning());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayNoon(),
                     actual.getTimedTariffParams().getCoefWorkDayNoon());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayEvening(),
                     actual.getTimedTariffParams().getCoefWorkDayEvening());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayNight(),
                     actual.getTimedTariffParams().getCoefWorkDayNight());
        assertEquals(expected.getTimedTariffParams().getCoefDayOff(),
                     actual.getTimedTariffParams().getCoefDayOff());
        
        
        assertNotNull(expected.getCoefInsurance());
        assertEquals(expected.getCoefInsurance(), actual.getCoefInsurance());
    }
    
    @Test
    @DisplayName("Удаление")
    @WithMockUser(roles = "GUEST")
    @Disabled("Требуется актуализация")
    void deleteItem() throws Exception {
        var id = scooterTariffRepository.saveAndFlush(mapper.newDtoToScooterTariff(testNewScooterTariffDTO1)).getId();
        assertThat(scooterTariffRepository.count()).isEqualTo(1);
        
        mockMvc.perform(delete("/" + TransportTypeEnum.SCOOTER.getName().toLowerCase(Locale.ROOT) + "/" + id.toString()))
               .andExpect(status().isOk());
        
        assertThat(scooterTariffRepository.findAll().stream().filter(BaseTariff::isActive).collect(
                Collectors.toList())).size().isEqualTo(0);
        var message = consumeMessage("service.tariff", TariffMessage.class);
        assertThat(message.getId()).isEqualTo(id);
        assertThat(message.deleted()).isTrue();
    }
    
    @Test
    @DisplayName("Удаление несуществующего")
    @WithMockUser(roles = "GUEST")
    void delete_noExists() throws Exception {
        mockMvc.perform(
                       delete("/" + TransportTypeEnum.SCOOTER.getName().toLowerCase(Locale.ROOT) + "/" + UUID.randomUUID()))
               .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Просмотр всех")
    @WithMockUser(roles = "GUEST")
    void getAll() throws Exception {
        for (var i = 0; i < 10; i++) {
            scooterTariffRepository.save(mapper.newDtoToScooterTariff(
                    testNewScooterTariffDTO1.toBuilder()
                                            .regionId(Collections.singleton(UUID.randomUUID()))
                                            .rideCostPerKm(200 * i)
                                            .rideCostPerMin(300 * i)
                                            .bookingCost(400 * i)
                                            .build()));
        }
        
        var response =
                mockMvc.perform(get("/" + TransportTypeEnum.SCOOTER.getName().toLowerCase(Locale.ROOT)))
                       .andExpect(status().isOk());
        
        response.andExpect(jsonPath("$.length()").value(scooterTariffRepository.count()));
        
        for (var i = 0; i < scooterTariffRepository.count(); i++) {
            var expected = scooterTariffRepository.findAll().get(i);
            
            response.andExpect(jsonPath("$[" + i + "].id").value(expected.getId().toString()))
                    .andExpect(jsonPath("$[" + i + "].regionId").value(expected.getRegionId().toString()))
                    .andExpect(jsonPath("$[" + i + "].transportType")
                                       .value(expected.getTransportType().toString()))
                    .andExpect(jsonPath("$[" + i + "].rideCostPerKm").value(expected.getRideCostPerKm().toString()))
                    .andExpect(jsonPath("$[" + i + "].rideCostPerMin")
                                       .value(expected.getRideCostPerMin().toString()))
                    .andExpect(jsonPath("$[" + i + "].bookingCost").value(expected.getBookingCost().toString()));
        }
    }
}
