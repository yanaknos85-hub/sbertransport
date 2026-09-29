package ru.sberbank.ditsib.transport.handlers;

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
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.constant.Role;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForTaxiRepository;
import ru.sberbank.ditsib.transport.request.exceptions.CarLocationRequestWrongStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@EmbeddedPostgres
@AutoConfigureMockMvc
@SpringBootTest(classes = RequestApplication.class)
class RequestExceptionHandlerTest extends KafkaTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RequestForTaxiRepository requestForTaxiRepository;

    @MockitoBean
    private AuthorizationManager<?> manager;

    private static final String REQUEST_ID = "7ffd0e5c-b2d9-4c5c-b206-804c34c87617";
    private static final String MSG = "Невозможно получить местоположение автомобиля. Заявка requestId=%s в статусе %s";

    @Test
    @SneakyThrows
    @Transactional
    @Sql(scripts = {
            "/scripts/basic_corp_structure.sql",
            "/scripts/request_for_taxi.sql"
    })
    void getCarLocation() {
        AuthorizeUtils.authorize(manager, Role.ROLE_EMPLOYEE_CORP_CLIENT.name());

        var request = requestForTaxiRepository.findById(UUID.fromString(REQUEST_ID))
                .orElseThrow(() -> new JUnitException("Request for taxi not found"));

        request.setStatus(TripRequestStatus.REPAIR_FINISHED);

        requestForTaxiRepository.saveAndFlush(request);

        mockMvc.perform(get("/car-location?requestId=" + REQUEST_ID)
                        .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_EMPLOYEE_CORP_CLIENT.name()))))
                .andExpect(status().isConflict())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(CarLocationRequestWrongStatusException.class))
                .andExpect(result -> assertThat(MSG.formatted(REQUEST_ID, TripRequestStatus.REPAIR_FINISHED.getDescription()))
                        .isEqualTo(
                                Optional.ofNullable(result.getResolvedException())
                                        .orElseThrow(() -> new JUnitException("Exception not found"))
                                        .getMessage())
                );
    }
}
