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
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.tariff.messaging.TariffMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.PersonalTariffRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.BaseTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.database.model.PersonalTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.PublicTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.*;
import ru.sberbank.ditsib.transport.tariff.dto.mapper.EntityDTOMapper;

import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
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
@DisplayName("Проверка контроллера тарифов личных авто")
@MockitoBean(types = JwtDecoder.class)
@Import(LiquibaseAutoConfiguration.class)
@ActiveProfiles({"test", "kafka"})
class PersonalTariffControllerTest extends KafkaTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private EntityDTOMapper mapper;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PersonalTariffRepository personalTariffRepository;
    
    @Autowired
    private GeoZoneRepository geoZoneRepository;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    private NewPersonalTariffDTO testNewPersonalTariff1;
    
    protected Organization organization1;
    protected Organization organization2;
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @MockitoBean
    private RestTemplate restTemplate;
    
    
    private final UUID regionId = UUID.randomUUID();
    protected final UUID regionId2 = UUID.randomUUID();
    
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
        
        geoZoneRepository.save(new GeoZone(regionId, "Регион1", 1 + "", null));
        geoZoneRepository.save(new GeoZone(regionId2, "Регион2", 1 + "", null));
        
        testNewPersonalTariff1 = NewPersonalTariffDTO.builder()
                                                     .regionId(Collections.singleton(regionId))
                                                     .organizationId(organization2.getId())
                                                     .rideCostPerKm(500)
                                                     .rideCostPerMin(100)
                                                     .transportType(TransportTypeEnum.PERSONAL)
                                                     .seasonalCoefficient(.5)
                                                     .seasonStart(LocalDate.now())
                                                     .seasonEnd(LocalDate.now().plusMonths(1))
                                                     .coopTariffParams(
                                                             CoopTariffParamsDTO.builder()
                                                                                .minCancelTimeMin(45)
                                                                                .distanceDeviationKm(5d)
                                                                                .savingsDeviationPct(15d)
                                                                                .timeDeviationMin(5).build())
                                                     .engineTariffParams(EngineTariffParamsDTO.builder()
                                                                                              .coefEngine1_6(1.)
                                                                                              .coefEngine1_6_to_2_0(1.1)
                                                                                              .coefEngine2_0_to_2_5(1.2)
                                                                                              .build())
                                                     .timedTariffParams(TimedTariffParamsDTO.builder()
                                                                                            .coefDayOff(1.5)
                                                                                            .coefWorkDayMorning(1.5)
                                                                                            .coefWorkDayNoon(1.)
                                                                                            .coefWorkDayEvening(1.5)
                                                                                            .coefWorkDayNight(1.)
                                                                                            .build())
                                                     .suburbTariffParams(SuburbTariffParamsDTO.builder()
                                                                                              .costPerKmInterRegion(
                                                                                                      5000)
                                                                                              .costPerMinSuburb(2000)
                                                                                              .costPerMinInterRegion(
                                                                                                      7000)
                                                                                              .costPerMinInterRegion(
                                                                                                      1000)
                                                                                              .build())
                                                     .build();
        
        when(restTemplate.postForEntity(any(), any(),any())).thenReturn(null);
    }
    
    @Test
    @DisplayName("Изменение")
    @WithMockUser(roles = "GUEST")
    void edit() throws Exception {
        var saved = personalTariffRepository.save(mapper.newDtoToPersonalTariff(testNewPersonalTariff1));
        
        var expected = testNewPersonalTariff1.toBuilder()
                                             .seasonalCoefficient(.25)
                                             .seasonStart(LocalDate.now())
                                             .seasonEnd(LocalDate.now().plusMonths(1))
                                             .build();
        
        var request = objectMapper.writeValueAsString(expected);
        
        mockMvc.perform(
                       put("/" + TransportTypeEnum.PERSONAL.getName().toLowerCase(Locale.ROOT) + "/" + saved.getId())
                               .contentType(MediaType.APPLICATION_JSON).content(request))
               .andExpect(status().isOk());
        
        PersonalTariff actual = personalTariffRepository.findById(saved.getId()).get();
        
        assertEquals(expected.getSeasonalCoefficient(), actual.getSeasonalCoefficient());
        assertEquals(expected.getSeasonStart(), actual.getSeasonStart());
        assertEquals(expected.getSeasonEnd(), actual.getSeasonEnd());
        assertNotNull(actual.getTransportType());
        assertEquals(saved.getHumanReadableId(), actual.getHumanReadableId());
        
        var message = consumeMessage("service.tariff", TariffMessage.class);
        assertEquals(saved.getHumanReadableId(), message.humanReadableId());
        assertEquals(saved.getOrganization().getId(), message.organizationId());
        assertTrue(message.humanReadableId().contains("TF-"));
    }
    
    @Test
    @DisplayName("Добавление")
    @WithMockUser(roles = "GUEST")
    void add() throws Exception {
        
        var expected = testNewPersonalTariff1;
        
        var request = objectMapper.writeValueAsString(expected);
        
        var response = mockMvc.perform(
                                      post("/" + TransportTypeEnum.PERSONAL.getName().toLowerCase(Locale.ROOT)).contentType(MediaType.APPLICATION_JSON)
                                                                                                               .content(request))
                              .andExpect(status().isOk());
        
        assertThat(personalTariffRepository.count()).isEqualTo(1);
        
        PersonalTariffDTO
                actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                PersonalTariffDTO.class);
        
        
        assertEquals(expected.getSeasonalCoefficient(), actual.getSeasonalCoefficient());
        assertEquals(expected.getSeasonStart(), actual.getSeasonStart());
        assertEquals(expected.getSeasonEnd(), actual.getSeasonEnd());
        
        
        var message = consumeMessage("service.tariff", TariffMessage.class);
        var foundTariff = personalTariffRepository.findById(actual.getId()).get();
        assertEquals(foundTariff.getHumanReadableId(), actual.getHumanReadableId());
        assertEquals(message.humanReadableId(), actual.getHumanReadableId());
        assertEquals(message.organizationId(), actual.getOrganizationId());
        assertTrue(message.humanReadableId().contains("TF-"));
        
    }
    
    @Test
    @DisplayName("Получение")
    @WithMockUser(roles = "GUEST")
    void getTaxi() throws Exception {
        
        var expected = testNewPersonalTariff1;
        var saved = personalTariffRepository.save(mapper.newDtoToPersonalTariff(testNewPersonalTariff1));
        var request = objectMapper.writeValueAsString(expected);
        
        var response = mockMvc.perform(
                get("/" + TransportTypeEnum.PERSONAL.getName().toLowerCase(Locale.ROOT) + "/" +
                    saved.getId()).contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isOk());
        
        PersonalTariffDTO
                actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                PersonalTariffDTO.class);
        
        assertEquals(expected.getSeasonalCoefficient(), actual.getSeasonalCoefficient());
        assertEquals(expected.getSeasonStart(), actual.getSeasonStart());
        assertEquals(expected.getSeasonEnd(), actual.getSeasonEnd());
    }
    
    @Test
    @DisplayName("Удаление")
    @WithMockUser(roles = "GUEST")
    @Disabled("Требуется актуализация")
    void deleteItem() throws Exception {
        var id = personalTariffRepository.saveAndFlush(mapper.newDtoToPersonalTariff(testNewPersonalTariff1)).getId();
        assertThat(personalTariffRepository.count()).isEqualTo(1);
        
        mockMvc.perform(delete("/" + TransportTypeEnum.PERSONAL.getName().toLowerCase(Locale.ROOT) + "/" + id.toString()))
               .andExpect(status().isOk());
        
        assertThat(personalTariffRepository.findAll().stream().filter(BaseTariff::isActive).collect(
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
                       delete("/" + TransportTypeEnum.PERSONAL.getName().toLowerCase(Locale.ROOT) + "/" + UUID.randomUUID()))
               .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Просмотр всех")
    @WithMockUser(roles = "GUEST")
    void getAll() throws Exception {
        for (var i = 0; i < 10; i++) {
            personalTariffRepository.save(mapper.newDtoToPersonalTariff(
                    testNewPersonalTariff1.toBuilder()
                                          .regionId(Collections.singleton(UUID.randomUUID()))
                                          .rideCostPerKm(200 * i)
                                          .rideCostPerMin(300 * i)
                                          .rideCostPerMin(100 * i)
                                          .seasonalCoefficient(0.1 * i + .001)
                                          .seasonStart(LocalDate.now().plusDays(i))
                                          .seasonEnd(LocalDate.now().plusMonths(i).plusDays(1))
                                          .build()));
        }
        
        var response =
                mockMvc.perform(get("/" + TransportTypeEnum.PERSONAL.getName().toLowerCase(Locale.ROOT)))
                       .andExpect(status().isOk());
        
        response.andExpect(jsonPath("$.length()").value(personalTariffRepository.count()));
        
        for (var i = 0; i < personalTariffRepository.count(); i++) {
            var expected = personalTariffRepository.findAll().get(i);
            
            response.andExpect(jsonPath("$[" + i + "].id").value(expected.getId().toString()))
                    .andExpect(jsonPath("$[" + i + "].regionId").value(expected.getRegionId().toString()))
                    .andExpect(jsonPath("$[" + i + "].transportType")
                                       .value(expected.getTransportType().toString()))
                    .andExpect(jsonPath("$[" + i + "].rideCostPerKm").value(String.valueOf(expected.getRideCostPerKm())))
                    .andExpect(jsonPath("$[" + i + "].seasonalCoefficient")
                                       .value(String.valueOf(expected.getSeasonalCoefficient())))
                    .andExpect(jsonPath("$[" + i + "].seasonStart").value(expected.getSeasonStart().toString()))
                    .andExpect(jsonPath("$[" + i + "].seasonEnd").value(expected.getSeasonEnd().toString()));
            
            
        }
    }
}
