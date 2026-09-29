package ru.sberbank.ditsib.transport.srm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.constants.PointMatchingType;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.srm.model.SrmRequestDTO;
import ru.sber.transport.srm.model.SrmWaypointPostDTO;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.TAXI;

@SpringBootTest
@Transactional
@EmbeddedPostgres
@AutoConfigureMockMvc
@EnableWireMock({
        @ConfigureWireMock(name = "gis-feign-server", property = "spring.cloud.openfeign.client.config.gis-feign-client.url"),
        @ConfigureWireMock(name = "gis-async-feign-server", property = "spring.cloud.openfeign.client.config.gis-async-feign-client.url"),
        @ConfigureWireMock(name = "gis-async-result-feign-server", property = "spring.cloud.openfeign.client.config.gis-async-result-feign-client.url"),
        @ConfigureWireMock(name = "sowa-feign-server", property = "gisdata.sowaUrl")
})
class SrmIntegrationTest {

    public static final String ROLE_ADMIN_CORP_CLIENT = "ROLE_ADMIN_CORP_CLIENT";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AuthorizationManager<?> manager;

    @Test
    @Sql("/script/tariff.sql")
    void addNew_sync() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT);
        var tariffId = UUID.fromString("c44aafcb-fbef-4994-b471-5ce8164ecbad");
        var waypointsSize = 3;
        var request = Instancio.of(SrmRequestDTO.class)
                .set(field(SrmRequestDTO::getTransportType), TAXI)
                .set(field(SrmRequestDTO::getTariffId), tariffId)
                .set(field(SrmRequestDTO::getRequiredPassengers), 4)
                .set(field(SrmRequestDTO::getPointMatchingType), PointMatchingType.MATCH_POINTS_FALSE)
                .set(field(SrmRequestDTO::getPickupTime), ZonedDateTime.now().plusHours(5))
                .set(field(SrmRequestDTO::getWaypoints), List.of(Instancio.of(SrmWaypointPostDTO.class)
                                .set(field(SrmWaypointPostDTO::getId), UUID.fromString("31aa8ed5-5679-3695-a8c5-52c5e05cfea8"))
                                .set(field(SrmWaypointPostDTO::getLatitude), 44.93847545175141)
                                .set(field(SrmWaypointPostDTO::getLongitude), 34.07262756028693)
                                .create(),
                        Instancio.of(SrmWaypointPostDTO.class)
                                .set(field(SrmWaypointPostDTO::getId), UUID.fromString("72116028-face-3586-a3b3-6c3c7f7ff771"))
                                .set(field(SrmWaypointPostDTO::getLatitude), 44.96761772790355)
                                .set(field(SrmWaypointPostDTO::getLongitude), 34.08081345831346)
                                .create(),
                        Instancio.of(SrmWaypointPostDTO.class)
                                .set(field(SrmWaypointPostDTO::getId), UUID.fromString("545c320b-0440-3a3d-b4ae-e114f029f24c"))
                                .set(field(SrmWaypointPostDTO::getLatitude), 44.96197809370496)
                                .set(field(SrmWaypointPostDTO::getLongitude), 34.1452914751356)
                                .create()
                ))
                .create();
        mockMvc.perform(post("/srm/addNew")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.oldId").isNotEmpty())
                .andExpect(jsonPath("$.timeZone").value(request.getTimeZone()))
                .andExpect(jsonPath("$.transportType").value(request.getTransportType().getName()))
                .andExpect(jsonPath("$.pointMatchingType").value(request.getPointMatchingType().toString()))
                .andExpect(jsonPath("$.rideCost").isNotEmpty())
                .andExpect(jsonPath("$.rideDistance").isNotEmpty())
                .andExpect(jsonPath("$.rideTime").isNotEmpty())
                .andExpect(jsonPath("$.waypoints.length()").value(waypointsSize))
                .andExpect(jsonPath("$.waypointsFinal.length()").value(waypointsSize))
                .andExpect(jsonPath("$.requestKpiList.length()").value(1))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.requestKpiList[0].id").isNotEmpty())
                .andExpect(jsonPath("$.requestKpiList[0].pickupTime").isNotEmpty())
                .andExpect(jsonPath("$.requestKpiList[0].requiredPassengers").value(request.getRequiredPassengers()))
                .andExpect(jsonPath("$.requestKpiList[0].cargoExpress").isBoolean());
    }

    @Test
    @Sql("/script/tariff.sql")
    void addNew_async() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT);
        var tariffId = UUID.fromString("c44aafcb-fbef-4994-b471-5ce8164ecbad");
        var waypointsSize = 30;
        var request = Instancio.of(SrmRequestDTO.class)
                .set(field(SrmRequestDTO::getTransportType), TAXI)
                .set(field(SrmRequestDTO::getTariffId), tariffId)
                .set(field(SrmRequestDTO::getRequiredPassengers), 4)
                .set(field(SrmRequestDTO::getPickupTime), ZonedDateTime.now().plusHours(5))
                .set(field(SrmRequestDTO::getPointMatchingType), PointMatchingType.MATCH_POINTS_FALSE)
                .set(field(SrmRequestDTO::getWaypoints), Instancio.ofList(SrmWaypointPostDTO.class)
                        .size(waypointsSize)
                        .create())
                .create();
        mockMvc.perform(post("/srm/addNew")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.oldId").isNotEmpty())
                .andExpect(jsonPath("$.timeZone").value(request.getTimeZone()))
                .andExpect(jsonPath("$.transportType").value(request.getTransportType().getName()))
                .andExpect(jsonPath("$.pointMatchingType").value(request.getPointMatchingType().toString()))
                .andExpect(jsonPath("$.rideCost").isNotEmpty())
                .andExpect(jsonPath("$.rideDistance").isNotEmpty())
                .andExpect(jsonPath("$.rideTime").isNotEmpty())
                .andExpect(jsonPath("$.waypoints.length()").value(waypointsSize))
                .andExpect(jsonPath("$.waypointsFinal.length()").value(waypointsSize))
                .andExpect(jsonPath("$.requestKpiList.length()").value(1))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.requestKpiList[0].id").isNotEmpty())
                .andExpect(jsonPath("$.requestKpiList[0].pickupTime").isNotEmpty())
                .andExpect(jsonPath("$.requestKpiList[0].requiredPassengers").value(request.getRequiredPassengers()))
                .andExpect(jsonPath("$.requestKpiList[0].cargoExpress").isBoolean());
    }
}