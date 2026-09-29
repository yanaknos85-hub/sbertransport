package ru.sberbank.ditsib.transport.tariff.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.PublicTariffRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.database.model.PublicTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.NewPublicTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.PublicTariffDTO;

import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
@DisplayName("Проверка контроллера тарифов на общественный транспорт")
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test", "kafka"})
class PublicTariffControllerTest extends KafkaTest {
    
    private static final UUID ORG_ID = UUID.randomUUID();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private PublicTariffRepository publicTariffRepository;
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private GeoZoneRepository geoZoneRepository;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    private final UUID samara = UUID.randomUUID();
    protected UUID regionId2 = UUID.randomUUID();
    
    @Transactional
    @BeforeEach
    void init() {
        var organization = Instancio.of(Organization.class)
                .set(field(Organization::getId), ORG_ID)
                .set(field(Organization::getDigitId), 1L)
                .set(field(Organization::isActive), true)
                .create();
        organizationRepository.save(organization);
        
        
        geoZoneRepository.save(new GeoZone(samara, "регион1", 1 + "", null));
        geoZoneRepository.save(new GeoZone(regionId2, "регион1", 2 + "", null));

        final var savedTariff = PublicTariff.builder()
                .organization(organization)
                .transportType(TransportTypeEnum.PUBLIC)
                .regionId(samara).humanReadableId("PT-001").active(true)
                .metroAvailability(true).tramAvailability(true)
                .trolleybusAvailability(true).busAvailability(true)
                .metroTicketCost(1000).tramTicketCost(1000)
                .trolleybusTicketCost(1000).busTicketCost(1000).build();
        publicTariffRepository.save(savedTariff);
    }
    
    @Transactional
    @AfterEach
    void clear() {
        publicTariffRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Создание - успех")
    void add_success() throws Exception {
        NewPublicTariffDTO newDto = NewPublicTariffDTO.builder()
                                                      .regionId(Collections.singleton(UUID.randomUUID()))
                                                      .organizationId(ORG_ID)
                                                      .transportType(TransportTypeEnum.PUBLIC)
                                                      .metroAvailability(false).tramAvailability(false)
                                                      .trolleybusAvailability(true).busAvailability(true)
                                                      .metroTicketCost(1).tramTicketCost(1)
                                                      .trolleybusTicketCost(1).busTicketCost(1).build();
        
        var request = objectMapper.writeValueAsString(newDto);
        
        var response =
                mockMvc.perform(post("/public").contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF8")
                                               .content(request))
                       .andExpect(status().isOk());
        
        var actual =
                objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<PublicTariffDTO>() {
                                       });
        
        assertThat(publicTariffRepository.count()).isEqualTo(2);
        assertThat(actual.getTramAvailability()).isEqualTo(newDto.getTramAvailability());
        assertThat(actual.getMetroAvailability()).isEqualTo(newDto.getMetroAvailability());
        assertThat(actual.getBusTicketCost()).isEqualTo(newDto.getBusTicketCost());
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Создание - ошибки валидации")
    void add_validationException() throws Exception {
        NewPublicTariffDTO incorrectDto = NewPublicTariffDTO.builder()
                                                            .organizationId(ORG_ID)
                                                            .transportType(TransportTypeEnum.PUBLIC)
                                                            .metroAvailability(false).tramAvailability(false)
                                                            .trolleybusAvailability(true).busAvailability(true)
                                                            .metroTicketCost(-1).tramTicketCost(-1)
                                                            .trolleybusTicketCost(-1).busTicketCost(-1).build();
        
        String incorrectRequest = objectMapper.writeValueAsString(incorrectDto);
        Exception ex = mockMvc.perform(post("/public")
                                               .content(incorrectRequest)
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .characterEncoding("UTF8"))
                              .andExpect(status().is4xxClientError()).andReturn().getResolvedException();
        
        assertThat(ex).isInstanceOf(MethodArgumentNotValidException.class);
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Создание проездного - успех ")
    void add_success_test() throws Exception {
        NewPublicTariffDTO incorrectDto = NewPublicTariffDTO.builder()
                                                            .organizationId(ORG_ID)
                                                            .regionId(Collections.singleton(regionId2))
                                                            .transportType(TransportTypeEnum.PUBLIC)
                                                            .metroTicketCost(0)
                                                            .tramTicketCost(0)
                                                            .trolleybusTicketCost(0)
                                                            .busTicketCost(0)
                                                            .metroAvailability(false)
                                                            .tramAvailability(false)
                                                            .trolleybusAvailability(false)
                                                            .busAvailability(true)
                                                            .travelCardBusAvailability(true)
                                                            .travelCardBusCost(11111_00).build();
        
        String incorrectRequest = objectMapper.writeValueAsString(incorrectDto);
        
        mockMvc.perform(post("/public")
                                .content(incorrectRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("UTF8"))
               .andExpect(status().isOk()).andReturn();
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Создание со стоимостью проездного больше допустимой - ошибки валидации ")
    void add_validationException2() throws Exception {
        NewPublicTariffDTO incorrectDto = NewPublicTariffDTO.builder()
                                                            .organizationId(ORG_ID)
                                                            .transportType(TransportTypeEnum.PUBLIC)
                                                            .metroTicketCost(0)
                                                            .tramTicketCost(0)
                                                            .trolleybusTicketCost(0)
                                                            .busTicketCost(0)
                                                            .metroAvailability(false)
                                                            .tramAvailability(false)
                                                            .trolleybusAvailability(false)
                                                            .busAvailability(true)
                                                            .travelCardBusAvailability(true)
                                                            .travelCardBusCost(111111_00).build();
        
        String incorrectRequest = objectMapper.writeValueAsString(incorrectDto);
        
        Exception ex = mockMvc.perform(post("/public")
                                               .content(incorrectRequest)
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .characterEncoding("UTF8"))
                              .andExpect(status().is4xxClientError()).andReturn().getResolvedException();
    
        assertThat(ex).isInstanceOf(MethodArgumentNotValidException.class);
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Создание - несуществующая организация")
    void add_incorrectOrganization() throws Exception {
        NewPublicTariffDTO incorrectDto = NewPublicTariffDTO.builder()
                                                            .organizationId(UUID.randomUUID())
                                                            .regionId(Collections.singleton(samara))
                                                            .transportType(TransportTypeEnum.PUBLIC)
                                                            .metroAvailability(false).tramAvailability(false)
                                                            .trolleybusAvailability(true).busAvailability(true)
                                                            .metroTicketCost(1).tramTicketCost(1)
                                                            .trolleybusTicketCost(1).busTicketCost(1).build();
        
        String incorrectRequest = objectMapper.writeValueAsString(incorrectDto);
        
        Exception ex = mockMvc.perform(post("/public")
                                               .content(incorrectRequest)
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .characterEncoding("UTF8"))
                              .andExpect(status().is4xxClientError()).andReturn().getResolvedException();
    
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Создание - некорректный тип транспорта")
    void add_incorrectTransportType() throws Exception {
        NewPublicTariffDTO incorrectDto = NewPublicTariffDTO.builder()
                                                            .organizationId(UUID.randomUUID())
                                                            .regionId(Collections.singleton(samara))
                                                            .transportType(TransportTypeEnum.SCOOTER)
                                                            .metroAvailability(false).tramAvailability(false)
                                                            .trolleybusAvailability(true).busAvailability(true)
                                                            .metroTicketCost(1).tramTicketCost(1)
                                                            .trolleybusTicketCost(1).busTicketCost(1).build();
        
        String incorrectRequest = objectMapper.writeValueAsString(incorrectDto);
        Exception ex = mockMvc.perform(post("/public")
                                               .content(incorrectRequest)
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .characterEncoding("UTF8"))
                              .andExpect(status().is4xxClientError()).andReturn().getResolvedException();
    
        assertThat(ex).isInstanceOf(EntityNotFoundException.class);
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Редактирование - успех")
    void edit_success() throws Exception {
        PublicTariff tariff = publicTariffRepository.findAll().getFirst();
        UUID tariffId = tariff.getId();
        final boolean oldMetroAvailability = tariff.isMetroAvailability();
        final Integer oldMetroTicketCost = tariff.getMetroTicketCost();
        
        NewPublicTariffDTO dto = NewPublicTariffDTO.builder()
                                                   .regionId(Collections.singleton(UUID.randomUUID()))
                                                   .organizationId(ORG_ID)
                                                   .transportType(TransportTypeEnum.PUBLIC)
                                                   .metroAvailability(false).tramAvailability(false)
                                                   .trolleybusAvailability(true).busAvailability(true)
                                                   .metroTicketCost(1).tramTicketCost(1)
                                                   .trolleybusTicketCost(1).busTicketCost(1).build();
        
        var request = objectMapper.writeValueAsString(dto);
        
        mockMvc.perform(put("/public/" + tariffId).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF8")
                                                  .content(request))
               .andExpect(status().isOk());
        
        var actual = publicTariffRepository.findAll().getFirst();
        
        assertThat(publicTariffRepository.count()).isEqualTo(1);
        assertThat(actual.isMetroAvailability()).isNotEqualTo(oldMetroAvailability);
        assertThat(actual.getMetroTicketCost()).isNotEqualTo(oldMetroTicketCost);
        assertThat(actual.isMetroAvailability()).isEqualTo(dto.getMetroAvailability());
        assertThat(actual.getMetroTicketCost()).isEqualTo(dto.getMetroTicketCost());
        assertThat(actual.getOrganization().getId()).isEqualTo(dto.getOrganizationId());
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Редактирование - ошибки валидации")
    void edit_validationException() throws Exception {
        PublicTariff tariff = publicTariffRepository.findAll().getFirst();
        UUID tariffId = tariff.getId();
        
        NewPublicTariffDTO incorrectDto = NewPublicTariffDTO.builder()
                                                            .organizationId(ORG_ID)
                                                            .regionId(Collections.singleton(samara))
                                                            .transportType(TransportTypeEnum.PUBLIC)
                                                            .metroAvailability(false).tramAvailability(false)
                                                            .trolleybusAvailability(true).busAvailability(true)
                                                            .metroTicketCost(-1).tramTicketCost(-1)
                                                            .trolleybusTicketCost(-1).busTicketCost(-1).build();
        
        var incorrectRequest = objectMapper.writeValueAsString(incorrectDto);
        
        Exception ex = mockMvc.perform(put("/public/" + tariffId)
                                               .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF8")
                                               .content(incorrectRequest))
                              .andExpect(status().is4xxClientError()).andReturn().getResolvedException();
    
        assertThat(ex).isInstanceOf(MethodArgumentNotValidException.class);
        assertEquals(4, ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors().size());
    }
    
    @Disabled("Почему?")
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Редактирование - несуществующая организация")
    void edit_incorrectOrganization() throws Exception {
        //Исходя из кода нельзя редактировать поля: (id, humanReadableId, organization, transportType, serviceType, region, regionId)
        PublicTariff tariff = publicTariffRepository.findAll().getFirst();
        UUID tariffId = tariff.getId();
        
        NewPublicTariffDTO incorrectDto = NewPublicTariffDTO.builder()
                                                            .organizationId(UUID.randomUUID())
                                                            .transportType(TransportTypeEnum.PUBLIC)
                                                            .metroAvailability(false).tramAvailability(false)
                                                            .trolleybusAvailability(true).busAvailability(true)
                                                            .metroTicketCost(1).tramTicketCost(1)
                                                            .trolleybusTicketCost(1).busTicketCost(1).build();
        
        var incorrectRequest = objectMapper.writeValueAsString(incorrectDto);
        
        mockMvc.perform(put("/public/" + tariffId)
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF8")
                                .content(incorrectRequest))
               .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Редактирование - несуществующий id в url")
    void edit_incorrectIdIntoUrl() throws Exception {
        NewPublicTariffDTO incorrectDto = NewPublicTariffDTO.builder()
                                                            .regionId(Collections.singleton(UUID.randomUUID()))
                                                            .organizationId(UUID.randomUUID())
                                                            .transportType(TransportTypeEnum.PUBLIC)
                                                            .metroAvailability(false).tramAvailability(false)
                                                            .trolleybusAvailability(true).busAvailability(true)
                                                            .metroTicketCost(1).tramTicketCost(1)
                                                            .trolleybusTicketCost(1).busTicketCost(1).build();
        
        var incorrectRequest = objectMapper.writeValueAsString(incorrectDto);
        
        Exception ex = mockMvc.perform(put("/public/" + UUID.randomUUID())
                                               .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF8")
                                               .content(incorrectRequest))
                              .andExpect(status().isNotFound()).andReturn().getResolvedException();
    
        assertThat(ex).isInstanceOf(EntityNotFoundException.class);
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Удаление - успех")
    void delete_success() throws Exception {
        PublicTariff tariff = publicTariffRepository.findAll().getFirst();
        UUID tariffId = tariff.getId();
        
        mockMvc.perform(delete("/public/" + tariffId)).andExpect(status().isOk());
        
        var actual = publicTariffRepository.findAll().getFirst();
        
        assertThat(publicTariffRepository.count()).isEqualTo(1);
        assertThat(actual.isActive()).isFalse();
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Удаление - несуществующий id в url")
    void delete_incorrectIdIntoUrl() throws Exception {
        Exception ex = mockMvc.perform(delete("/public/" + UUID.randomUUID()))
                              .andExpect(status().isNotFound()).andReturn().getResolvedException();
    
        assertThat(ex).isInstanceOf(EntityNotFoundException.class);
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Получение конкретного тарифа - успех")
    void get_success() throws Exception {
        PublicTariff tariff = publicTariffRepository.findAll().getFirst();
        UUID tariffId = tariff.getId();
        
        var response = mockMvc.perform(get("/public/" + tariffId)).andExpect(status().isOk());
        
        var actual =
                objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<PublicTariffDTO>() {
                                       });
        
        assertThat(actual.getRegionId().contains(tariff.getRegionId())).isTrue();
        assertThat(actual.getOrganizationId()).isEqualTo(tariff.getOrganization().getId());
        assertThat(actual.getId()).isEqualTo(tariff.getId());
        assertThat(actual.getHumanReadableId()).isEqualTo(tariff.getHumanReadableId());
        assertThat(actual.getTransportType()).isEqualTo(tariff.getTransportType());
        assertThat(actual.getMetroAvailability()).isEqualTo(tariff.isMetroAvailability());
        assertThat(actual.getBusTicketCost()).isEqualTo(tariff.getBusTicketCost());
        assertThat(actual.getTramTicketCost()).isEqualTo(tariff.getTramTicketCost());
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Получение конкретного тарифа - несуществующий id в url")
    void get_incorrectIdIntoUrl() throws Exception {
        Exception ex = mockMvc.perform(get("/public/" + UUID.randomUUID()))
                              .andExpect(status().isNotFound()).andReturn().getResolvedException();
    
        assertThat(ex).isInstanceOf(EntityNotFoundException.class);
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Получение всех тарифов - успех")
    void getAll_success() throws Exception {
        final String humanReadableId = "PT-002";
        //добавим еще один тариф
        Organization organization = organizationRepository.findAll().getFirst();
        
        PublicTariff newTariff = PublicTariff.builder()
                                             .transportType(TransportTypeEnum.PUBLIC)
                                             .organization(organization)
                                             .regionId(UUID.randomUUID()).humanReadableId(humanReadableId).active(true)
                                             .metroAvailability(false).tramAvailability(false)
                                             .trolleybusAvailability(true).busAvailability(true)
                                             .metroTicketCost(0).tramTicketCost(0)
                                             .trolleybusTicketCost(1).busTicketCost(1).build();
        publicTariffRepository.save(newTariff);
        assertThat(publicTariffRepository.count()).isEqualTo(2);
        
        var response = mockMvc.perform(get("/public")).andExpect(status().isOk());
        
        var actualList =
                objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<List<PublicTariffDTO>>() {
                                       });
        
        assertThat(actualList.size()).isEqualTo(2);
        PublicTariffDTO newTariffDTO = actualList.stream().filter(tariff -> tariff.getHumanReadableId().equals(humanReadableId)).findFirst().orElseThrow();
        assertThat(newTariffDTO.getRegionId().contains(newTariff.getRegionId())).isTrue();
        assertThat(newTariffDTO.getOrganizationId()).isEqualTo(newTariff.getOrganization().getId());
        assertThat(newTariffDTO.getId()).isEqualTo(newTariff.getId());
        assertThat(newTariffDTO.getTrolleybusTicketCost()).isEqualTo(newTariff.getTrolleybusTicketCost());
        assertThat(newTariffDTO.getBusAvailability()).isEqualTo(newTariff.isBusAvailability());
        assertThat(newTariffDTO.getMetroAvailability()).isEqualTo(newTariff.isMetroAvailability());
    }
}