package ru.sberbank.ditsib.transport.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.constant.Role;
import ru.sberbank.ditsib.transport.request.database.dao.CarLocationTaskRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForTaxiRepository;
import ru.sberbank.ditsib.transport.request.database.model.CarLocationTask;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@EmbeddedPostgres
@AutoConfigureMockMvc
@SpringBootTest(classes = RequestApplication.class)
class CarLocationControllerTest extends KafkaTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RequestForTaxiRepository requestForTaxiRepository;
    @Autowired
    private CarLocationTaskRepository carLocationTaskRepository;

    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private Clock clock;

    private static final LocalDateTime NOW = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private static final String REQUEST_ID = "7ffd0e5c-b2d9-4c5c-b206-804c34c87617";

    @Test
    @SneakyThrows
    @Transactional
    @Sql(scripts = {
            "/scripts/basic_corp_structure.sql",
            "/scripts/request_for_taxi.sql"
    })
    void getCarLocation() {
        AuthorizeUtils.authorize(manager, Role.ROLE_EMPLOYEE_CORP_CLIENT.name());

        doReturn(FIXED_CLOCK.instant()).when(clock).instant();
        doReturn(FIXED_CLOCK.getZone()).when(clock).getZone();

        var request = requestForTaxiRepository.findById(UUID.fromString(REQUEST_ID))
                .orElseThrow(() -> new JUnitException("Request for taxi not found"));

        request.setStatus(TripRequestStatus.TAXI_DRIVER_ON_THE_WAY);
        request.setDesiredDate(NOW.plusMinutes(47));

        requestForTaxiRepository.saveAndFlush(request);

        mockMvc.perform(get("/car-location?requestId=" + REQUEST_ID)
                        .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_EMPLOYEE_CORP_CLIENT.name()))))
                .andExpect(status().isOk());

        var taskList = carLocationTaskRepository.findAllByActiveIsTrue();
        assertThat(taskList).hasSize(1);
        assertThat(taskList.get(0)).extracting(
                CarLocationTask::getRequestId,
                CarLocationTask::getOrderPartnerId,
                CarLocationTask::getCreatedAt,
                CarLocationTask::isActive
        ).containsExactly(
                request.getId(),
                request.getTaxiTrip().getTaxiId(),
                NOW,
                true
        );
    }
}
