package ru.sberbank.ditsib.transport.srm.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.constants.PointMatchingType;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.srm.model.SrmRequestDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sber.transport.srm.model.SrmWaypointPostDTO;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.srm.config.SrmSettingNames;
import ru.sberbank.ditsib.transport.srm.dao.SrmRequestKpiRepository;
import ru.sberbank.ditsib.transport.srm.dao.SrmSharedRideRepository;
import ru.sberbank.ditsib.transport.srm.dao.TaxiTariffRepository;
import ru.sberbank.ditsib.transport.srm.dto.twogis.TwoGisMatrixRequestDto;
import ru.sberbank.ditsib.transport.srm.dto.twogis.TwoGisMatrixResponseDto;
import ru.sberbank.ditsib.transport.srm.feign.GisFeignClient;
import ru.sberbank.ditsib.transport.srm.model.SrmRequestKpi;
import ru.sberbank.ditsib.transport.srm.model.SrmSharedRide;
import ru.sberbank.ditsib.transport.srm.model.tariff.TaxiTariff;
import ru.sberbank.ditsib.transport.srm.service.SrmSettingService;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@Isolated
@Feature("app_passenger_srm")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@Transactional
@Slf4j
@DisplayName("Проверка совместных поездок MAP")
@MockitoBean(types = JwtDecoder.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class SrmController2Test extends KafkaTest {

    public static final String USER_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";

    @Autowired
    private TaxiTariffRepository taxiTariffRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SrmSharedRideRepository srmSharedRideRepository;
    @Autowired
    SrmRequestKpiRepository srmRequestKpiRepository;
    @Autowired
    private SrmSettingService srmSettingService;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private GisFeignClient gisFeignClient;
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    SrmWaypointPostDTO waypoint_moskva;
    SrmWaypointPostDTO waypoint_piter;
    UUID taxiTariffId = UUID.randomUUID();
    ZonedDateTime plannedDateTime;

    @BeforeEach
    public void init() {
        AuthorizeUtils.authorize(roleCheckService);
        waypoint_moskva = new SrmWaypointPostDTO();
        waypoint_moskva.setLatitude(55.7586389);
        waypoint_moskva.setLongitude(37.6197886);
        waypoint_moskva.setWaitingTime(0);
        waypoint_moskva.setAddress("Moskva");

        waypoint_piter = new SrmWaypointPostDTO();
        waypoint_piter.setLatitude(59.941299);
        waypoint_piter.setLongitude(30.332515);
        waypoint_piter.setWaitingTime(0);
        waypoint_piter.setAddress("Piter");

        int i = 1;
        TaxiTariff taxiTariff = TaxiTariff.builder()
                .id(taxiTariffId)
                .organizationId(UUID.randomUUID())
                .region("MOSKVA")
                .taxiClass(TaxiClass.ECONOMY)
                .maxCapacity(5)
                .contractId(UUID.randomUUID())
                .freeWaitingTime(0)
                .rideCostPerKm(i + 1)
                .rideCostPerMin(i * 2 + 1)
                .minRideDistanceCost(i * 3)
                .minRideTimeCost(i * 4)
                .waitCostPerMin(i)
                .transportType(TransportTypeEnum.TAXI)
                .build();
        taxiTariffRepository.saveAndFlush(taxiTariff);

        srmSettingService.add(SrmSettingNames.DISTANCE_DEVIATION_KM, "1");
        srmSettingService.add(SrmSettingNames.TIME_DEVIATION_MIN, "10");
        srmSettingService.add(SrmSettingNames.SAVING_DEVIATION_PROCENT, "20");
        srmSettingService.add(SrmSettingNames.MIN_CANCEL_TIME_MIN, "10");
        srmSettingService.add(SrmSettingNames.MAX_WAITING_TIME, "600");
        srmSettingService.add(SrmSettingNames.FIND_ALGORITHM, "BATCH");

        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_MONTH, 3);
        plannedDateTime = ZonedDateTime.ofInstant(now.toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
    }

    @Test
    @DisplayName("CRUD совместных поездок matchAllPoints")
    @WithMockUser(value = USER_ID, roles = "GUEST")
    void test_CRUD() throws Exception {
        String data = """
                {
                   "rows":[
                      {
                         "elements":[
                         ]
                      }
                   ]
                }""";
        doReturn(objectMapper.readValue(data, TwoGisMatrixResponseDto.class))
                .when(gisFeignClient).getDistMatrix(anyString(), anyString(), any(TwoGisMatrixRequestDto.class));
        List<SrmSharedRide> sharedRides0 = srmSharedRideRepository.findAll();
        assertEquals(0, sharedRides0.size());

        // make new shared ride from first request
        SrmRequestDTO requestDTO1 = getTaxiRequestDto1();
        log.debug("DEBUGTIME: pickup time 1 = " + requestDTO1.getPickupTime());
        String srmRequestDTOStr = objectMapper.writeValueAsString(requestDTO1);
        ResultActions result = mockMvc.perform(post("/srm/addNew")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(srmRequestDTOStr))
                .andExpect(status().isOk());
        String contentAsString = result.andReturn().getResponse().getContentAsString();
        SrmSharedRideDTO sharedRideDTO = objectMapper.readValue(contentAsString, SrmSharedRideDTO.class);

        List<SrmSharedRide> sharedRides1 = srmSharedRideRepository.findAll();
        assertEquals(1, sharedRides1.size());
        assertEquals(sharedRides1.get(0).getId(), sharedRideDTO.getId());
        assertThat(sharedRides1.get(0).getOldId()).isNotNull();
        assertThat(sharedRides1.get(0).getOldId()).isPositive();
        assertEquals(1, sharedRides1.get(0).getRequestKpiList().size());
        assertEquals(sharedRides1.get(0).getRequestKpiList().get(0).getOrgRequestId(), requestDTO1.getRequestId());
        assertThat(sharedRides1.get(0).getRequestKpiList().get(0).getOrderingIndex()).isNotNull();
        assertEquals(0, sharedRides1.get(0).getRequestKpiList().get(0).getOrderingIndex());

        List<SrmRequestKpi> requestKpiList1 = srmRequestKpiRepository.findAll();
        assertEquals(1, requestKpiList1.size());
        assertEquals(requestKpiList1.get(0).getOrgRequestId(), requestDTO1.getRequestId());
        assertThat(requestKpiList1.get(0).getOldId()).isNotNull();
        assertThat(requestKpiList1.get(0).getOldId()).isPositive();

        // perform get
        String urlGet = "/srm/" + sharedRideDTO.getId();
        mockMvc.perform(get(urlGet))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sharedRideDTO.getId().toString()));

        SrmRequestDTO requestDTO2 = getTaxiRequestDto2();
        requestDTO2.setRequestId(null);
        String srmRequestDTO2Str_emptyId = objectMapper.writeValueAsString(requestDTO2);
        requestDTO2.setRequestId(UUID.randomUUID());
        String srmRequestDTO2Str = objectMapper.writeValueAsString(requestDTO2);
        // get suitable shared rides for second request
        ResultActions result11 = mockMvc.perform(post("/srm/findMatch")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(srmRequestDTO2Str_emptyId))
                .andExpect(status().isOk());
        String contentAsString11 = result11.andReturn().getResponse().getContentAsString();
        List<SrmSharedRideDTO> matchingSharedRideDtoList = objectMapper.readValue(contentAsString11,
                new TypeReference<>() {
                });
        log.debug("DEBUG: matching size = " + matchingSharedRideDtoList.size());


        assertEquals(1, matchingSharedRideDtoList.size());
        UUID matchingRideId = matchingSharedRideDtoList.get(0).getId();

        //prepare second request and join it to shared ride
        ResultActions result2 = mockMvc.perform(post("/srm/joinRequest/" + matchingRideId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(srmRequestDTO2Str))
                .andExpect(status().isOk());
        String contentAsString2 = result2.andReturn().getResponse().getContentAsString();
        SrmSharedRideDTO sharedRideDTO2 = objectMapper.readValue(contentAsString2, SrmSharedRideDTO.class);

        assertEquals(2, sharedRideDTO2.getRequestKpiList().size());
        assertEquals(0, sharedRideDTO2.getRequestKpiList().get(0).getOrderingIndex());
        assertEquals(1, sharedRideDTO2.getRequestKpiList().get(1).getOrderingIndex());

        List<SrmSharedRide> sharedRides2 = srmSharedRideRepository.findAll();
        assertEquals(1, sharedRides2.size());
        SrmSharedRide sharedRide2 = srmSharedRideRepository.getReferenceById(sharedRides2.get(0).getId());
        assertEquals(4, sharedRide2.getWaypoints().size());

        String srmRequestDTO3Str = objectMapper.writeValueAsString(getTaxiRequestDto3());
        ResultActions result21 = mockMvc.perform(post("/srm/findMatch")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(srmRequestDTO3Str))
                .andExpect(status().isOk());
        String contentAsString21 = result21.andReturn().getResponse().getContentAsString();
        List<SrmSharedRideDTO> matchingSharedRideDtoList21 = objectMapper.readValue(contentAsString21,
                new TypeReference<>() {
                });
        assertEquals(1, matchingSharedRideDtoList21.size());
        UUID matchingRideId21 = matchingSharedRideDtoList21.get(0).getId();

        ResultActions result22 = mockMvc.perform(post("/srm/joinRequest/" + matchingRideId21)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(srmRequestDTO3Str))
                .andExpect(status().isOk());
        String contentAsString22 = result22.andReturn().getResponse().getContentAsString();
        SrmSharedRideDTO sharedRideDTO3 = objectMapper.readValue(contentAsString22, SrmSharedRideDTO.class);

        assertEquals(3, sharedRideDTO3.getRequestKpiList().size());
    }

    private SrmRequestDTO getTaxiRequestDto1() {
        List<SrmWaypointPostDTO> waypoints = new ArrayList<>();
        waypoints.add(waypoint_moskva);
        waypoints.add(waypoint_piter);

        SrmRequestDTO requestDTO = new SrmRequestDTO();
        requestDTO.setRequestId(UUID.randomUUID());
        requestDTO.setRequiredPassengers(1);
        requestDTO.setTariffId(taxiTariffId);
        requestDTO.setTransportType(TransportTypeEnum.TAXI);
        requestDTO.setPickupTime(plannedDateTime.plusMinutes(5));
        requestDTO.setPointMatchingType(PointMatchingType.MATCH_ALL_POINTS);
        requestDTO.setTimeZone("Etc/GMT-3");
        requestDTO.setWaypoints(waypoints);

        return requestDTO;
    }

    private SrmRequestDTO getTaxiRequestDto2() {
        List<SrmWaypointPostDTO> waypoints = new ArrayList<>();
        waypoints.add(waypoint_moskva);
        waypoints.add(waypoint_piter);

        SrmRequestDTO requestDTO = new SrmRequestDTO();
        requestDTO.setRequestId(UUID.randomUUID());
        requestDTO.setRequiredPassengers(1);
        requestDTO.setTariffId(taxiTariffId);
        requestDTO.setTransportType(TransportTypeEnum.TAXI);
        requestDTO.setPickupTime(plannedDateTime);
        requestDTO.setPointMatchingType(PointMatchingType.MATCH_ALL_POINTS);
        requestDTO.setTimeZone("Etc/GMT-3");
        requestDTO.setWaypoints(waypoints);

        return requestDTO;
    }

    private SrmRequestDTO getTaxiRequestDto3() {
        List<SrmWaypointPostDTO> waypoints = new ArrayList<>();
        waypoints.add(waypoint_moskva);
        waypoints.add(waypoint_piter);

        SrmRequestDTO requestDTO = new SrmRequestDTO();
        requestDTO.setRequestId(UUID.randomUUID());
        requestDTO.setRequiredPassengers(1);
        requestDTO.setTariffId(taxiTariffId);
        requestDTO.setTransportType(TransportTypeEnum.TAXI);
        requestDTO.setPickupTime(plannedDateTime);
        requestDTO.setPointMatchingType(PointMatchingType.MATCH_ALL_POINTS);
        requestDTO.setTimeZone("Etc/GMT-3");
        requestDTO.setWaypoints(waypoints);

        return requestDTO;
    }
}