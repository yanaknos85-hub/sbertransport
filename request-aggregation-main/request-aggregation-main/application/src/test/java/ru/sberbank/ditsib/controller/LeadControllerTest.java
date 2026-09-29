package ru.sberbank.ditsib.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.BaseIntegrationTest;
import ru.sberbank.ditsib.dto.GeoAddress;
import ru.sberbank.ditsib.enumerate.PointType;
import ru.sberbank.ditsib.enumerate.TransportClass;
import ru.sberbank.ditsib.enumerate.TransportType;
import ru.sberbank.ditsib.enumerate.TripType;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.enumerate.Role.ROLE_DISPATCHER_SUPPORT_SERVICE;

@DisplayName("Проверка контроллера пользовательских заявок")
@EmbeddedPostgres
@SpringBootTest
@AutoConfigureMockMvc
class LeadControllerTest extends BaseIntegrationTest {

    private static final String CONTROLLER_URL = "/manager/leads";
    private static final String EMPLOYEE_1_ID = "3cd35c19-fd39-413c-99a0-30f35bd642a8";


    private final String leadRequest = """
            {
            	"transportType": "TAXI",
            	"tripType": "DAYTIME_TRIP",
            	"transportClass": "ECONOMY",
            	"comment": "new comment",
            	"departureTime": "%s",
            	"points" : [
            		{
            			"typePoint": "START",
                        "latitude": 55.7558,
            			"longitude": 37.6173,
                        "waypoint": "Moscow, Russia"
                    },
                    {
            			"typePoint": "END",
                        "latitude": 59.9343,
                        "longitude": 30.3351,
                        "waypoint": "Saint Petersburg, Russia"
                    }
            	],
            	"isDriver": false
            }
            """;

    @Test
    @Sql(scripts = "/scripts/basic_corp_structure.sql")
    @DisplayName("Создание пользовательской заявки")
    void createLead() throws Exception {
        var departure = LocalDateTime.now().plusHours(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        mockMvc.perform(post(CONTROLLER_URL + "/{userId}", EMPLOYEE_1_ID)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                        .contentType(APPLICATION_JSON)
                        .content(String.format(leadRequest, departure)))
                .andExpect(status().isOk());

        var leads = leadRepository.findAll();

        assertThat(leads).hasSize(1);
        var savedLead = leads.get(0);
        assertThat(savedLead.getEmployee().getId()).hasToString(EMPLOYEE_1_ID);
        assertThat(savedLead.getTransportClass()).isEqualTo(TransportClass.ECONOMY);
        assertThat(savedLead.getTripType()).isEqualTo(TripType.DAYTIME_TRIP);
        assertThat(savedLead.getTransportType()).isEqualTo(TransportType.TAXI);
        assertThat(savedLead.getComment()).isEqualTo("new comment");
        assertThat(savedLead.isDriver()).isFalse();

        var points = pointLeadRepository.findAll();
        assertThat(points).hasSize(2);
        var point1 = points.get(0);
        var point2 = points.get(1);

        assertThat(point1.getLead().getId()).isEqualTo(savedLead.getId());
        assertThat(point1.getPointNumber()).isEqualTo(1);
        assertThat(point1.getTypePoint()).isEqualTo(PointType.START);

        assertThat(point2.getLead().getId()).isEqualTo(savedLead.getId());
        assertThat(point2.getPointNumber()).isEqualTo(2);
        assertThat(point2.getTypePoint()).isEqualTo(PointType.END);
    }

    @Test
    @DisplayName("Валидация excel файла с пользовательскими заявками")
    @Sql(scripts = "/scripts/basic_corp_structure.sql")
    void validate() throws Exception {
        var file = new MockMultipartFile(
                "file",
                "leads.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                Files.readAllBytes(new ClassPathResource("/load/leads.xlsx").getFile().toPath()));
        var geoAdressFrom = new GeoAddress("Россия", "Москва", "Москва",
                "Пятницкое шоссе", "д.42", null, null, BigDecimal.valueOf(37.6173),
                BigDecimal.valueOf(55.7558));
        var geoAdressTo = new GeoAddress("Россия", "Москва", "Москва",
                "Малый Патриарший переулок", "д.7", null, null,
                BigDecimal.valueOf(37.6189), BigDecimal.valueOf(55.7509));
        doReturn(List.of(geoAdressFrom)).doReturn(List.of(geoAdressTo)).when(geoService).getGeoAddress(anyString());
        var message = "Дата и время заказа должны быть с учетом задержки в 3 часа от текущего времени";

        mockMvc.perform(multipart(CONTROLLER_URL + "/file/validate")
                        .file(file)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].personnelNumber.error").value(false))
                .andExpect(jsonPath("$.content.[0].personnelNumber.value").value("2016497"))
                .andExpect(jsonPath("$.content.[0].fullName.error").value(false))
                .andExpect(jsonPath("$.content.[0].fullName.value").value("Петров Петр"))
                .andExpect(jsonPath("$.content.[0].addressFrom.error").value(false))
                .andExpect(jsonPath("$.content.[0].addressFrom.value").value("Москва, Пятницкое шоссе, д.42"))
                .andExpect(jsonPath("$.content.[0].addressTo.error").value(false))
                .andExpect(jsonPath("$.content.[0].addressTo.value").value("Москва, Малый Патриарший переулок, 7"))
                .andExpect(jsonPath("$.content.[0].orderDate.error").value(true))
                .andExpect(jsonPath("$.content.[0].orderDate.value").value("2025-08-30"))
                .andExpect(jsonPath("$.content.[0].orderDate.errorMessage").value(message))
                .andExpect(jsonPath("$.content.[0].orderTime.error").value(true))
                .andExpect(jsonPath("$.content.[0].orderTime.value").value("19:00:00"))
                .andExpect(jsonPath("$.content.[0].orderTime.errorMessage").value(message))
                .andExpect(jsonPath("$.content.[0].transportType.error").value(false))
                .andExpect(jsonPath("$.content.[0].transportType.value").value(TransportType.TAXI.name()))
                .andExpect(jsonPath("$.content.[0].transportClass.error").value(false))
                .andExpect(jsonPath("$.content.[0].transportClass.value").value(TransportClass.ECONOMY.name()))
                .andExpect(jsonPath("$.content.[0].tripType.error").value(false))
                .andExpect(jsonPath("$.content.[0].tripType.value").value(TripType.DAYTIME_TRIP.name()))
                .andExpect(jsonPath("$.content.[0].addressFromLatitude").value(geoAdressFrom.latitude()))
                .andExpect(jsonPath("$.content.[0].addressFromLongitude").value(geoAdressFrom.longitude()))
                .andExpect(jsonPath("$.content.[0].addressToLatitude").value(geoAdressTo.latitude()))
                .andExpect(jsonPath("$.content.[0].addressToLongitude").value(geoAdressTo.longitude()));
    }

}