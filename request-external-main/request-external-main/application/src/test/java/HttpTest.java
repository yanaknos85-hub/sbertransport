import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.protobuf.NullValue;
import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.business.providers.AvailableClasses;
import ru.sber.transport.corporate.grpc.service.DelegatesGrpc;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.EmployeesGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.exceptions.dto.ExceptionBody;
import ru.sber.transport.geo_zones.grpc.dto.GeoZonesDescriptor;
import ru.sber.transport.geo_zones.grpc.service.GeoZonesServiceGrpc;
import ru.sber.transport.grpc.test.extension.GrpcCleanupExtension;
import ru.sber.transport.limits.grpc.service.LimitServiceGrpc;
import ru.sber.transport.limits.grpc.service.Limits;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.external.application.Application;
import ru.sber.transport.tariff.external.ExternalTariff;
import ru.sber.transport.tariff.external.PriceDataServiceGrpc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.ALWAYS, classes = Application.class)
@EmbeddedPostgres
@Transactional
@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Тесты HTTP-сервиса")
@ActiveProfiles({"test", "no-grpc"})
@MockitoBean(types = {JwtDecoder.class, DelegatesGrpc.DelegatesStub.class})
public class HttpTest {

    @RegisterExtension
    private final GrpcCleanupExtension grpcCleanupExtension = new GrpcCleanupExtension();

    @MockitoBean
    private EmployeesGrpc.EmployeesBlockingStub employeeStub;

    @MockitoBean
    private OrganizationsGrpc.OrganizationsBlockingStub organizationStub;

    @MockitoBean
    private DepartmentsGrpc.DepartmentsBlockingStub departmentStub;

    @MockitoBean
    private PriceDataServiceGrpc.PriceDataServiceBlockingStub pricesStub;

    @MockitoBean
    private GeoZonesServiceGrpc.GeoZonesServiceBlockingStub geoZonesStub;

    @MockitoBean
    private LimitServiceGrpc.LimitServiceStub limitServiceStub;

    @MockitoBean
    private EmployeeOrganizationFunction employeeFunction;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private AvailableClasses classesProvider;

    @MockitoBean
    private AuthorizationManager<?> authorizationManager;

    @Autowired
    private DSLContext dsl;

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @BeforeEach
    void setup() throws IOException {
        AuthorizeUtils.authorize(authorizationManager);
        final var service = new EmployeesGrpc.EmployeesImplBase() {
            @Override
            public void one(OrganizationsOuterClass.Request request, StreamObserver<OrganizationsOuterClass.Employee> responseObserver) {
                responseObserver.onNext(OrganizationsOuterClass.Employee.newBuilder().setId("18a8b936-7966-4cdd-bc4a-aed3032321b4").build());
                responseObserver.onCompleted();
            }
        };
        grpcCleanupExtension.addService(service);
        when(employeeStub.one(any())).then(inv -> {
            final var employeeResponse = new AtomicReference<OrganizationsOuterClass.Employee>();
            final var semaphore = new Semaphore(0);
            service.one(inv.getArgument(0, OrganizationsOuterClass.Request.class), new StreamObserver<>() {
                @Override
                public void onNext(OrganizationsOuterClass.Employee employee) {
                    employeeResponse.set(employee);
                    semaphore.release();
                }

                @Override
                public void onError(Throwable throwable) {
                }

                @Override
                public void onCompleted() {
                }
            });
            semaphore.acquire();
            return employeeResponse.get();
        });
        when(geoZonesStub.region(any())).thenReturn(GeoZonesDescriptor.Region.newBuilder().setTimeZone("+3").build());
    }

    @Test
    @DisplayName("Получение всех заявок")
    void test_getAll() {
        when(classesProvider.exists(any(), any(), any())).thenReturn(true);

        when(jwtDecoder.decode(any())).thenReturn(Jwt.withTokenValue("token").header("algo", "none").jti(UUID.randomUUID().toString()).claim("roles", "[]").build());

        final var entity = RequestEntity
                .get("http://localhost:%s/".formatted(port))
                .header(HttpHeaders.AUTHORIZATION, "Bearer eyJraWQiOiJqd3QiLCJjdHkiOiJhcHBsaWNhdGlvbi9qc29uIiwidHlwIjoiSldUIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJSdWtsZXZpY2hSLVIiLCJyb2xlcyI6WyJST0xFX0VNUExPWUVFX0NPUlBfQ0xJRU5UIiwiUk9MRV9DSElFRl9DT1JQX0NMSUVOVCIsIlJPTEVfQURNSU5fREFUQV9NQVNURVIiLCJST0xFX0FETUlOX0NPUlBfQ0xJRU5UIl0sImlzcyI6IlNiZXJUcmFuc3BvcnQiLCJ0cmFuc3BvcnQiOmZhbHNlLCJkYXRhX21hc3RlciI6dHJ1ZSwicmFuZG9tIjoiYjcyMWJkYTctMDEzZC00NTZkLWJhMTUtMDY4NmE1NDk1M2I1IiwibmJmIjoxNzQ1NjYyMjAzLCJyZXF1ZXN0SWQiOiJiMzQ4MzVmZWFmY2YxZDc0ZmU3Mjk3ZWMzOTUxOGI0OSIsInNjb3BlIjoiRU1QTE9ZRUUiLCJleHAiOjE3NDYyNjc5MDMsImZhY3RvciI6IkJBU0lDIiwiaWF0IjoxNzQ1NjYyMjAzLCJqdGkiOiIxOGE4YjkzNi03OTY2LTRjZGQtYmM0YS1hZWQzMDMyMzIxYjQifQ.Xs6CHzisPYhsE9V_5x2Rf67DFrecRc5Rm-fW1YM1AqW118rRtx-7mOWPeXybiiaYMDh8FT96ok-jnYS7ylkr6m1BTXOzGLLcdWISpzorv5N0Aob3Bax2_ikB5HwRasa0YxNZyaKKiSmGrXpmIfXGc3Wh_D3_wHaeHT02p7D7i_Ih0M3Ki2EymeAKOiT3yX1CqRkj-H7s8APTHV9IzODHCToqmEZI8ba620UW8EsNGlpe12bIrd-WubdQPMolxtwCSR_Rjxs-uCtQFzXnPHfJOHRgQ-0g9LKhJw1dNpxOW3FBkRn-6dedsza3R4sdxAW1gA_8zq-L7PkSHLTpbZMcfnRmJfxy9umF-ohkryxDiuyvsJ1tdNgwhedXI-TW-OP9Y2TOStLPsKgYUkwm4AAuSB7o1UC5IvarKhg6RbF0wApLgI89zUH7M8r01-M5LGk5p8Zfx_e74bZ0tiLKUTjTw2jTPm_Y47lJok5xHrafA3L9ySDaiWAWcb2WN_15RDSXYJnukWsEhGb4Z4CRWerI6_t0MWujqFZYDBivjar7HDGucw24jrjpTPG7dZ30b9rojaN8J5249dAa-E0LhFjMH78ss0Hg8x00MY1adhaKvYmMK1QwQ3SL4_hvtUOw7ZJDxhdqxs1BWtO-7Ni13f50AyhqMEnuQkQug0tBz2eYyc0")
                .build();

        final var response = restTemplate.exchange(entity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        dsl.truncate(Tables.TRIP_ORDER).cascade().execute();
    }

    @Test
    @DisplayName("Создание заявки. Пустая база")
    void test_post_emptyBase() throws IOException {
        when(classesProvider.exists(any(), any(), any())).thenReturn(true);

        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();
        final var positionId = UUID.randomUUID();
        final var departmentId = UUID.randomUUID();
        when(organizationStub.one(any())).thenReturn(OrganizationsOuterClass.Organization.newBuilder().setId(organizationId.toString()).setDigitId(1).build());
        when(departmentStub.one(any())).thenReturn(OrganizationsOuterClass.Department.newBuilder().setId(departmentId.toString()).setHeadId(OrganizationsOuterClass.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build()).build());
        when(employeeStub.one(any())).thenReturn(OrganizationsOuterClass.Employee.newBuilder()
                .setId(userId.toString())
                .setOrganizationId(organizationId.toString())
                .setDepartmentId(departmentId.toString())
                .setPositionId(positionId.toString())
                .build());
        when(pricesStub.request(any())).thenReturn(ExternalTariff.PriceDataResponse.newBuilder().setWaitTime(Instancio.create(Duration.class).toString()).setTime(Instancio.create(Duration.class).toString()).setCost(ExternalTariff.Cost.newBuilder().setIntegerPart(Instancio.create(Long.class)).setFractionPart(Instancio.create(Integer.class)).build()).build());
        when(jwtDecoder.decode(any())).thenReturn(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).claim("roles", "[]").build());

        final var content = """
                {
                  "tripDate": "2024-01-01T10:05:10.023+0300",
                  "waypoints": [
                    {
                      "country": "Россия",
                      "region": "Санкт-Петербург",
                      "city": "Санкт-Петербург",
                      "street": "Подвойского",
                      "house": "31",
                      "structure": "1",
                      "latitude": 55.753930,
                      "longitude": 37.620795
                    },
                    {
                      "country": "Россия",
                      "region": "Санкт-Петербург",
                      "city": "Санкт-Петербург",
                      "street": "Подвойского",
                      "house": "31",
                      "structure": "1",
                      "latitude": 55.612550,
                      "longitude": 37.202390
                    }
                  ],
                  "purposeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "tariff": "ECONOMY"
                }
                """;

        final var entity = RequestEntity
                .post("http://localhost:%s/".formatted(port))
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer eyJraWQiOiJqd3QiLCJjdHkiOiJhcHBsaWNhdGlvbi9qc29uIiwidHlwIjoiSldUIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJSdWtsZXZpY2hSLVIiLCJyb2xlcyI6WyJST0xFX0VNUExPWUVFX0NPUlBfQ0xJRU5UIiwiUk9MRV9DSElFRl9DT1JQX0NMSUVOVCIsIlJPTEVfQURNSU5fREFUQV9NQVNURVIiLCJST0xFX0FETUlOX0NPUlBfQ0xJRU5UIl0sImlzcyI6IlNiZXJUcmFuc3BvcnQiLCJ0cmFuc3BvcnQiOmZhbHNlLCJkYXRhX21hc3RlciI6dHJ1ZSwicmFuZG9tIjoiYjcyMWJkYTctMDEzZC00NTZkLWJhMTUtMDY4NmE1NDk1M2I1IiwibmJmIjoxNzQ1NjYyMjAzLCJyZXF1ZXN0SWQiOiJiMzQ4MzVmZWFmY2YxZDc0ZmU3Mjk3ZWMzOTUxOGI0OSIsInNjb3BlIjoiRU1QTE9ZRUUiLCJleHAiOjE3NDYyNjc5MDMsImZhY3RvciI6IkJBU0lDIiwiaWF0IjoxNzQ1NjYyMjAzLCJqdGkiOiIxOGE4YjkzNi03OTY2LTRjZGQtYmM0YS1hZWQzMDMyMzIxYjQifQ.Xs6CHzisPYhsE9V_5x2Rf67DFrecRc5Rm-fW1YM1AqW118rRtx-7mOWPeXybiiaYMDh8FT96ok-jnYS7ylkr6m1BTXOzGLLcdWISpzorv5N0Aob3Bax2_ikB5HwRasa0YxNZyaKKiSmGrXpmIfXGc3Wh_D3_wHaeHT02p7D7i_Ih0M3Ki2EymeAKOiT3yX1CqRkj-H7s8APTHV9IzODHCToqmEZI8ba620UW8EsNGlpe12bIrd-WubdQPMolxtwCSR_Rjxs-uCtQFzXnPHfJOHRgQ-0g9LKhJw1dNpxOW3FBkRn-6dedsza3R4sdxAW1gA_8zq-L7PkSHLTpbZMcfnRmJfxy9umF-ohkryxDiuyvsJ1tdNgwhedXI-TW-OP9Y2TOStLPsKgYUkwm4AAuSB7o1UC5IvarKhg6RbF0wApLgI89zUH7M8r01-M5LGk5p8Zfx_e74bZ0tiLKUTjTw2jTPm_Y47lJok5xHrafA3L9ySDaiWAWcb2WN_15RDSXYJnukWsEhGb4Z4CRWerI6_t0MWujqFZYDBivjar7HDGucw24jrjpTPG7dZ30b9rojaN8J5249dAa-E0LhFjMH78ss0Hg8x00MY1adhaKvYmMK1QwQ3SL4_hvtUOw7ZJDxhdqxs1BWtO-7Ni13f50AyhqMEnuQkQug0tBz2eYyc0")
                .body(content);

        final var service = new LimitServiceGrpc.LimitServiceImplBase() {

            @Override
            public StreamObserver<Limits.ReserveRequest> reserve(StreamObserver<Limits.Response> responseObserver) {
                return new StreamObserver<>() {

                    @Override
                    public void onNext(Limits.ReserveRequest reserveRequest) {
                        responseObserver.onNext(Limits.Response.newBuilder().setId(reserveRequest.getId()).setStatus(Limits.Status.OK).build());
                        responseObserver.onCompleted();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        responseObserver.onError(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                    }
                };
            }
        };
        final var channel = grpcCleanupExtension.addService(service);
        final var stub = LimitServiceGrpc.newStub(channel);
        when(limitServiceStub.reserve(any())).then(inv -> stub.reserve(inv.getArgument(0)));

        final var initialCount = dsl.fetchCount(Tables.TRIP_ORDER);

        final var response = restTemplate.exchange(entity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(dsl.fetchCount(Tables.TRIP_ORDER)).isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("Создание заявки. Недостаточно лимита")
    void test_post_notSufficient() throws IOException {
        when(classesProvider.exists(any(), any(), any())).thenReturn(true);

        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();
        final var positionId = UUID.randomUUID();
        final var departmentId = UUID.randomUUID();
        when(organizationStub.one(any())).thenReturn(OrganizationsOuterClass.Organization.newBuilder().setId(organizationId.toString()).setDigitId(1).build());
        when(departmentStub.one(any())).thenReturn(OrganizationsOuterClass.Department.newBuilder().setId(departmentId.toString()).setHeadId(OrganizationsOuterClass.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build()).build());
        when(employeeStub.one(any())).thenReturn(OrganizationsOuterClass.Employee.newBuilder()
                .setId(userId.toString())
                .setOrganizationId(organizationId.toString())
                .setDepartmentId(departmentId.toString())
                .setPositionId(positionId.toString())
                .build());
        when(pricesStub.request(any())).thenReturn(ExternalTariff.PriceDataResponse.newBuilder().setWaitTime(Instancio.create(Duration.class).toString()).setTime(Instancio.create(Duration.class).toString()).setCost(ExternalTariff.Cost.newBuilder().setIntegerPart(Instancio.create(Long.class)).setFractionPart(Instancio.create(Integer.class)).build()).build());
        when(jwtDecoder.decode(any())).thenReturn(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).claim("roles", "[]").build());

        final var content = """
                {
                  "tripDate": "2024-01-01T10:05:10.023+0300",
                  "waypoints": [
                    {
                      "country": "Россия",
                      "region": "Санкт-Петербург",
                      "city": "Санкт-Петербург",
                      "street": "Подвойского",
                      "house": "31",
                      "structure": "1",
                      "latitude": 55.753930,
                      "longitude": 37.620795
                    },
                    {
                      "country": "Россия",
                      "region": "Санкт-Петербург",
                      "city": "Санкт-Петербург",
                      "street": "Подвойского",
                      "house": "31",
                      "structure": "1",
                      "latitude": 55.612550,
                      "longitude": 37.202390
                    }
                  ],
                  "purposeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "tariff": "ECONOMY"
                }
                """;

        final var entity = RequestEntity
                .post("http://localhost:%s/".formatted(port))
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer eyJraWQiOiJqd3QiLCJjdHkiOiJhcHBsaWNhdGlvbi9qc29uIiwidHlwIjoiSldUIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJSdWtsZXZpY2hSLVIiLCJyb2xlcyI6WyJST0xFX0VNUExPWUVFX0NPUlBfQ0xJRU5UIiwiUk9MRV9DSElFRl9DT1JQX0NMSUVOVCIsIlJPTEVfQURNSU5fREFUQV9NQVNURVIiLCJST0xFX0FETUlOX0NPUlBfQ0xJRU5UIl0sImlzcyI6IlNiZXJUcmFuc3BvcnQiLCJ0cmFuc3BvcnQiOmZhbHNlLCJkYXRhX21hc3RlciI6dHJ1ZSwicmFuZG9tIjoiYjcyMWJkYTctMDEzZC00NTZkLWJhMTUtMDY4NmE1NDk1M2I1IiwibmJmIjoxNzQ1NjYyMjAzLCJyZXF1ZXN0SWQiOiJiMzQ4MzVmZWFmY2YxZDc0ZmU3Mjk3ZWMzOTUxOGI0OSIsInNjb3BlIjoiRU1QTE9ZRUUiLCJleHAiOjE3NDYyNjc5MDMsImZhY3RvciI6IkJBU0lDIiwiaWF0IjoxNzQ1NjYyMjAzLCJqdGkiOiIxOGE4YjkzNi03OTY2LTRjZGQtYmM0YS1hZWQzMDMyMzIxYjQifQ.Xs6CHzisPYhsE9V_5x2Rf67DFrecRc5Rm-fW1YM1AqW118rRtx-7mOWPeXybiiaYMDh8FT96ok-jnYS7ylkr6m1BTXOzGLLcdWISpzorv5N0Aob3Bax2_ikB5HwRasa0YxNZyaKKiSmGrXpmIfXGc3Wh_D3_wHaeHT02p7D7i_Ih0M3Ki2EymeAKOiT3yX1CqRkj-H7s8APTHV9IzODHCToqmEZI8ba620UW8EsNGlpe12bIrd-WubdQPMolxtwCSR_Rjxs-uCtQFzXnPHfJOHRgQ-0g9LKhJw1dNpxOW3FBkRn-6dedsza3R4sdxAW1gA_8zq-L7PkSHLTpbZMcfnRmJfxy9umF-ohkryxDiuyvsJ1tdNgwhedXI-TW-OP9Y2TOStLPsKgYUkwm4AAuSB7o1UC5IvarKhg6RbF0wApLgI89zUH7M8r01-M5LGk5p8Zfx_e74bZ0tiLKUTjTw2jTPm_Y47lJok5xHrafA3L9ySDaiWAWcb2WN_15RDSXYJnukWsEhGb4Z4CRWerI6_t0MWujqFZYDBivjar7HDGucw24jrjpTPG7dZ30b9rojaN8J5249dAa-E0LhFjMH78ss0Hg8x00MY1adhaKvYmMK1QwQ3SL4_hvtUOw7ZJDxhdqxs1BWtO-7Ni13f50AyhqMEnuQkQug0tBz2eYyc0")
                .body(content);

        final var service = new LimitServiceGrpc.LimitServiceImplBase() {

            @Override
            public StreamObserver<Limits.ReserveRequest> reserve(StreamObserver<Limits.Response> responseObserver) {
                return new StreamObserver<>() {

                    @Override
                    public void onNext(Limits.ReserveRequest reserveRequest) {
                        responseObserver.onNext(Limits.Response.newBuilder().setId(reserveRequest.getId()).setStatus(Limits.Status.NOT_SUFFICIENT).build());
                        responseObserver.onCompleted();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        responseObserver.onError(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                    }
                };
            }
        };
        final var channel = grpcCleanupExtension.addService(service);
        final var stub = LimitServiceGrpc.newStub(channel);
        when(limitServiceStub.reserve(any())).then(inv -> stub.reserve(inv.getArgument(0)));

        final var initialCount = dsl.fetchCount(Tables.TRIP_ORDER);

        try {
            restTemplate.exchange(entity, String.class);
            fail("Expected exception");
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

            final var response = e.getResponseBodyAs(ExceptionBody.class);

            assertThat(response).isNotNull();
            assertThat(response.getMessage()).isEqualTo("Reserve sum too big");
            assertThat(response.getProblems().iterator().next().getConstraints().getFirst().getType()).isEqualTo("NOT_SUFFICIENT");
        }
        assertThat(dsl.fetchCount(Tables.TRIP_ORDER)).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("Создание заявки. Тип транспорта не обслуживается")
    void test_post_transportTypeNotAvailable() throws IOException {
        when(classesProvider.exists(any(), any(), any())).thenReturn(true);

        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();
        final var positionId = UUID.randomUUID();
        final var departmentId = UUID.randomUUID();
        when(organizationStub.one(any())).thenReturn(OrganizationsOuterClass.Organization.newBuilder().setId(organizationId.toString()).setDigitId(1).build());
        when(departmentStub.one(any())).thenReturn(OrganizationsOuterClass.Department.newBuilder().setId(departmentId.toString()).setHeadId(OrganizationsOuterClass.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build()).build());
        when(employeeStub.one(any())).thenReturn(OrganizationsOuterClass.Employee.newBuilder()
                .setId(userId.toString())
                .setOrganizationId(organizationId.toString())
                .setDepartmentId(departmentId.toString())
                .setPositionId(positionId.toString())
                .build());
        when(pricesStub.request(any())).thenReturn(ExternalTariff.PriceDataResponse.newBuilder().setWaitTime(Instancio.create(Duration.class).toString()).setTime(Instancio.create(Duration.class).toString()).setCost(ExternalTariff.Cost.newBuilder().setIntegerPart(Instancio.create(Long.class)).setFractionPart(Instancio.create(Integer.class)).build()).build());
        when(jwtDecoder.decode(any())).thenReturn(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).claim("roles", "[]").build());

        final var content = """
                {
                  "tripDate": "2024-01-01T10:05:10.023+0300",
                  "waypoints": [
                    {
                      "country": "Россия",
                      "region": "Санкт-Петербург",
                      "city": "Санкт-Петербург",
                      "street": "Подвойского",
                      "house": "31",
                      "structure": "1",
                      "latitude": 55.753930,
                      "longitude": 37.620795
                    },
                    {
                      "country": "Россия",
                      "region": "Санкт-Петербург",
                      "city": "Санкт-Петербург",
                      "street": "Подвойского",
                      "house": "31",
                      "structure": "1",
                      "latitude": 55.612550,
                      "longitude": 37.202390
                    }
                  ],
                  "purposeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "tariff": "ECONOMY"
                }
                """;

        final var entity = RequestEntity
                .post("http://localhost:%s/".formatted(port))
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer eyJraWQiOiJqd3QiLCJjdHkiOiJhcHBsaWNhdGlvbi9qc29uIiwidHlwIjoiSldUIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJSdWtsZXZpY2hSLVIiLCJyb2xlcyI6WyJST0xFX0VNUExPWUVFX0NPUlBfQ0xJRU5UIiwiUk9MRV9DSElFRl9DT1JQX0NMSUVOVCIsIlJPTEVfQURNSU5fREFUQV9NQVNURVIiLCJST0xFX0FETUlOX0NPUlBfQ0xJRU5UIl0sImlzcyI6IlNiZXJUcmFuc3BvcnQiLCJ0cmFuc3BvcnQiOmZhbHNlLCJkYXRhX21hc3RlciI6dHJ1ZSwicmFuZG9tIjoiYjcyMWJkYTctMDEzZC00NTZkLWJhMTUtMDY4NmE1NDk1M2I1IiwibmJmIjoxNzQ1NjYyMjAzLCJyZXF1ZXN0SWQiOiJiMzQ4MzVmZWFmY2YxZDc0ZmU3Mjk3ZWMzOTUxOGI0OSIsInNjb3BlIjoiRU1QTE9ZRUUiLCJleHAiOjE3NDYyNjc5MDMsImZhY3RvciI6IkJBU0lDIiwiaWF0IjoxNzQ1NjYyMjAzLCJqdGkiOiIxOGE4YjkzNi03OTY2LTRjZGQtYmM0YS1hZWQzMDMyMzIxYjQifQ.Xs6CHzisPYhsE9V_5x2Rf67DFrecRc5Rm-fW1YM1AqW118rRtx-7mOWPeXybiiaYMDh8FT96ok-jnYS7ylkr6m1BTXOzGLLcdWISpzorv5N0Aob3Bax2_ikB5HwRasa0YxNZyaKKiSmGrXpmIfXGc3Wh_D3_wHaeHT02p7D7i_Ih0M3Ki2EymeAKOiT3yX1CqRkj-H7s8APTHV9IzODHCToqmEZI8ba620UW8EsNGlpe12bIrd-WubdQPMolxtwCSR_Rjxs-uCtQFzXnPHfJOHRgQ-0g9LKhJw1dNpxOW3FBkRn-6dedsza3R4sdxAW1gA_8zq-L7PkSHLTpbZMcfnRmJfxy9umF-ohkryxDiuyvsJ1tdNgwhedXI-TW-OP9Y2TOStLPsKgYUkwm4AAuSB7o1UC5IvarKhg6RbF0wApLgI89zUH7M8r01-M5LGk5p8Zfx_e74bZ0tiLKUTjTw2jTPm_Y47lJok5xHrafA3L9ySDaiWAWcb2WN_15RDSXYJnukWsEhGb4Z4CRWerI6_t0MWujqFZYDBivjar7HDGucw24jrjpTPG7dZ30b9rojaN8J5249dAa-E0LhFjMH78ss0Hg8x00MY1adhaKvYmMK1QwQ3SL4_hvtUOw7ZJDxhdqxs1BWtO-7Ni13f50AyhqMEnuQkQug0tBz2eYyc0")
                .body(content);

        final var service = new LimitServiceGrpc.LimitServiceImplBase() {

            @Override
            public StreamObserver<Limits.ReserveRequest> reserve(StreamObserver<Limits.Response> responseObserver) {
                return new StreamObserver<>() {

                    @Override
                    public void onNext(Limits.ReserveRequest reserveRequest) {
                        responseObserver.onNext(Limits.Response.newBuilder().setId(reserveRequest.getId()).setStatus(Limits.Status.TYPE_NOT_AVAILABLE).build());
                        responseObserver.onCompleted();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        responseObserver.onError(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                    }
                };
            }
        };
        final var channel = grpcCleanupExtension.addService(service);
        final var stub = LimitServiceGrpc.newStub(channel);
        when(limitServiceStub.reserve(any())).then(inv -> stub.reserve(inv.getArgument(0)));

        final var initialCount = dsl.fetchCount(Tables.TRIP_ORDER);

        try {
            restTemplate.exchange(entity, String.class);
            fail("Expected exception");
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

            final var response = e.getResponseBodyAs(ExceptionBody.class);

            assertThat(response).isNotNull();
            final var responseEntity = response.getEntity();
            assertThat(responseEntity).isNotNull();
            assertThat(responseEntity.getId()).isEqualTo("TAXI");
            assertThat(responseEntity.getName()).isEqualTo("TRANSPORT_TYPE");
        }
        assertThat(dsl.fetchCount(Tables.TRIP_ORDER)).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("Создание заявки. Вид услуги не обслуживается")
    void test_post_serviceTypeNotAvailable() throws IOException {
        when(classesProvider.exists(any(), any(), any())).thenReturn(true);

        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();
        final var positionId = UUID.randomUUID();
        final var departmentId = UUID.randomUUID();
        when(organizationStub.one(any())).thenReturn(OrganizationsOuterClass.Organization.newBuilder().setId(organizationId.toString()).setDigitId(1).build());
        when(departmentStub.one(any())).thenReturn(OrganizationsOuterClass.Department.newBuilder().setId(departmentId.toString()).setHeadId(OrganizationsOuterClass.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build()).build());
        when(employeeStub.one(any())).thenReturn(OrganizationsOuterClass.Employee.newBuilder()
                .setId(userId.toString())
                .setOrganizationId(organizationId.toString())
                .setDepartmentId(departmentId.toString())
                .setPositionId(positionId.toString())
                .build());
        when(pricesStub.request(any())).thenReturn(ExternalTariff.PriceDataResponse.newBuilder().setWaitTime(Instancio.create(Duration.class).toString()).setTime(Instancio.create(Duration.class).toString()).setCost(ExternalTariff.Cost.newBuilder().setIntegerPart(Instancio.create(Long.class)).setFractionPart(Instancio.create(Integer.class)).build()).build());
        when(jwtDecoder.decode(any())).thenReturn(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).claim("roles", "[\"ROLE_USER\"]").build());

        final var content = """
                {
                  "tripDate": "2024-01-01T10:05:10.023+0300",
                  "waypoints": [
                    {
                      "country": "Россия",
                      "region": "Санкт-Петербург",
                      "city": "Санкт-Петербург",
                      "street": "Подвойского",
                      "house": "31",
                      "structure": "1",
                      "latitude": 55.753930,
                      "longitude": 37.620795
                    },
                    {
                      "country": "Россия",
                      "region": "Санкт-Петербург",
                      "city": "Санкт-Петербург",
                      "street": "Подвойского",
                      "house": "31",
                      "structure": "1",
                      "latitude": 55.612550,
                      "longitude": 37.202390
                    }
                  ],
                  "purposeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "tariff": "ECONOMY"
                }
                """;

        final var entity = RequestEntity
                .post("http://localhost:%s/".formatted(port))
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer eyJraWQiOiJqd3QiLCJjdHkiOiJhcHBsaWNhdGlvbi9qc29uIiwidHlwIjoiSldUIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJSdWtsZXZpY2hSLVIiLCJyb2xlcyI6WyJST0xFX0VNUExPWUVFX0NPUlBfQ0xJRU5UIiwiUk9MRV9DSElFRl9DT1JQX0NMSUVOVCIsIlJPTEVfQURNSU5fREFUQV9NQVNURVIiLCJST0xFX0FETUlOX0NPUlBfQ0xJRU5UIl0sImlzcyI6IlNiZXJUcmFuc3BvcnQiLCJ0cmFuc3BvcnQiOmZhbHNlLCJkYXRhX21hc3RlciI6dHJ1ZSwicmFuZG9tIjoiYjcyMWJkYTctMDEzZC00NTZkLWJhMTUtMDY4NmE1NDk1M2I1IiwibmJmIjoxNzQ1NjYyMjAzLCJyZXF1ZXN0SWQiOiJiMzQ4MzVmZWFmY2YxZDc0ZmU3Mjk3ZWMzOTUxOGI0OSIsInNjb3BlIjoiRU1QTE9ZRUUiLCJleHAiOjE3NDYyNjc5MDMsImZhY3RvciI6IkJBU0lDIiwiaWF0IjoxNzQ1NjYyMjAzLCJqdGkiOiIxOGE4YjkzNi03OTY2LTRjZGQtYmM0YS1hZWQzMDMyMzIxYjQifQ.Xs6CHzisPYhsE9V_5x2Rf67DFrecRc5Rm-fW1YM1AqW118rRtx-7mOWPeXybiiaYMDh8FT96ok-jnYS7ylkr6m1BTXOzGLLcdWISpzorv5N0Aob3Bax2_ikB5HwRasa0YxNZyaKKiSmGrXpmIfXGc3Wh_D3_wHaeHT02p7D7i_Ih0M3Ki2EymeAKOiT3yX1CqRkj-H7s8APTHV9IzODHCToqmEZI8ba620UW8EsNGlpe12bIrd-WubdQPMolxtwCSR_Rjxs-uCtQFzXnPHfJOHRgQ-0g9LKhJw1dNpxOW3FBkRn-6dedsza3R4sdxAW1gA_8zq-L7PkSHLTpbZMcfnRmJfxy9umF-ohkryxDiuyvsJ1tdNgwhedXI-TW-OP9Y2TOStLPsKgYUkwm4AAuSB7o1UC5IvarKhg6RbF0wApLgI89zUH7M8r01-M5LGk5p8Zfx_e74bZ0tiLKUTjTw2jTPm_Y47lJok5xHrafA3L9ySDaiWAWcb2WN_15RDSXYJnukWsEhGb4Z4CRWerI6_t0MWujqFZYDBivjar7HDGucw24jrjpTPG7dZ30b9rojaN8J5249dAa-E0LhFjMH78ss0Hg8x00MY1adhaKvYmMK1QwQ3SL4_hvtUOw7ZJDxhdqxs1BWtO-7Ni13f50AyhqMEnuQkQug0tBz2eYyc0")
                .body(content);

        final var service = new LimitServiceGrpc.LimitServiceImplBase() {

            @Override
            public StreamObserver<Limits.ReserveRequest> reserve(StreamObserver<Limits.Response> responseObserver) {
                return new StreamObserver<>() {

                    @Override
                    public void onNext(Limits.ReserveRequest reserveRequest) {
                        responseObserver.onNext(Limits.Response.newBuilder().setId(reserveRequest.getId()).setStatus(Limits.Status.SERVICE_NOT_AVAILABLE).build());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        responseObserver.onError(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                    }
                };
            }
        };
        final var channel = grpcCleanupExtension.addService(service);
        final var stub = LimitServiceGrpc.newStub(channel);
        when(limitServiceStub.reserve(any())).then(inv -> stub.reserve(inv.getArgument(0)));

        try {
            restTemplate.exchange(entity, String.class);
        } catch (HttpClientErrorException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            final var description = ex.getResponseBodyAs(ExceptionBody.class);
            assertThat(description).isNotNull();
            final var descriptionEntity = description.getEntity();
            assertThat(descriptionEntity.getId()).isEqualTo("PASSENGER");
            assertThat(descriptionEntity.getName()).isEqualTo("SERVICE_TYPE");
        }

        assertThat(dsl.fetchCount(Tables.TRIP_ORDER)).isZero();
    }

    @Test
    @DisplayName("Создание заявки. Не найдены данные")
    void test_post_dataNotFound() throws IOException {
        when(classesProvider.exists(any(), any(), any())).thenReturn(true);

        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();
        final var positionId = UUID.randomUUID();
        final var departmentId = UUID.randomUUID();
        when(organizationStub.one(any())).thenReturn(OrganizationsOuterClass.Organization.newBuilder().setId(organizationId.toString()).setDigitId(1).build());
        when(departmentStub.one(any())).thenReturn(OrganizationsOuterClass.Department.newBuilder().setId(departmentId.toString()).setHeadId(OrganizationsOuterClass.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build()).build());
        when(employeeStub.one(any())).thenReturn(OrganizationsOuterClass.Employee.newBuilder()
                .setId(userId.toString())
                .setOrganizationId(organizationId.toString())
                .setDepartmentId(departmentId.toString())
                .setPositionId(positionId.toString())
                .build());
        when(pricesStub.request(any())).thenReturn(ExternalTariff.PriceDataResponse.newBuilder().setWaitTime(Instancio.create(Duration.class).toString()).setTime(Instancio.create(Duration.class).toString()).setCost(ExternalTariff.Cost.newBuilder().setIntegerPart(Instancio.create(Long.class)).setFractionPart(Instancio.create(Integer.class)).build()).build());
        when(jwtDecoder.decode(any())).thenReturn(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).claim("roles", "[]").build());

        final var content = """
                {
                  "tripDate": "2024-01-01T10:05:10.023+0300",
                  "waypoints": [
                    {
                      "country": "Россия",
                      "region": "Санкт-Петербург",
                      "city": "Санкт-Петербург",
                      "street": "Подвойского",
                      "house": "31",
                      "structure": "1",
                      "latitude": 55.753930,
                      "longitude": 37.620795
                    },
                    {
                      "country": "Россия",
                      "region": "Санкт-Петербург",
                      "city": "Санкт-Петербург",
                      "street": "Подвойского",
                      "house": "31",
                      "structure": "1",
                      "latitude": 55.612550,
                      "longitude": 37.202390
                    }
                  ],
                  "purposeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "tariff": "ECONOMY"
                }
                """;

        final var entity = RequestEntity
                .post("http://localhost:%s/".formatted(port))
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer eyJraWQiOiJqd3QiLCJjdHkiOiJhcHBsaWNhdGlvbi9qc29uIiwidHlwIjoiSldUIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJSdWtsZXZpY2hSLVIiLCJyb2xlcyI6WyJST0xFX0VNUExPWUVFX0NPUlBfQ0xJRU5UIiwiUk9MRV9DSElFRl9DT1JQX0NMSUVOVCIsIlJPTEVfQURNSU5fREFUQV9NQVNURVIiLCJST0xFX0FETUlOX0NPUlBfQ0xJRU5UIl0sImlzcyI6IlNiZXJUcmFuc3BvcnQiLCJ0cmFuc3BvcnQiOmZhbHNlLCJkYXRhX21hc3RlciI6dHJ1ZSwicmFuZG9tIjoiYjcyMWJkYTctMDEzZC00NTZkLWJhMTUtMDY4NmE1NDk1M2I1IiwibmJmIjoxNzQ1NjYyMjAzLCJyZXF1ZXN0SWQiOiJiMzQ4MzVmZWFmY2YxZDc0ZmU3Mjk3ZWMzOTUxOGI0OSIsInNjb3BlIjoiRU1QTE9ZRUUiLCJleHAiOjE3NDYyNjc5MDMsImZhY3RvciI6IkJBU0lDIiwiaWF0IjoxNzQ1NjYyMjAzLCJqdGkiOiIxOGE4YjkzNi03OTY2LTRjZGQtYmM0YS1hZWQzMDMyMzIxYjQifQ.Xs6CHzisPYhsE9V_5x2Rf67DFrecRc5Rm-fW1YM1AqW118rRtx-7mOWPeXybiiaYMDh8FT96ok-jnYS7ylkr6m1BTXOzGLLcdWISpzorv5N0Aob3Bax2_ikB5HwRasa0YxNZyaKKiSmGrXpmIfXGc3Wh_D3_wHaeHT02p7D7i_Ih0M3Ki2EymeAKOiT3yX1CqRkj-H7s8APTHV9IzODHCToqmEZI8ba620UW8EsNGlpe12bIrd-WubdQPMolxtwCSR_Rjxs-uCtQFzXnPHfJOHRgQ-0g9LKhJw1dNpxOW3FBkRn-6dedsza3R4sdxAW1gA_8zq-L7PkSHLTpbZMcfnRmJfxy9umF-ohkryxDiuyvsJ1tdNgwhedXI-TW-OP9Y2TOStLPsKgYUkwm4AAuSB7o1UC5IvarKhg6RbF0wApLgI89zUH7M8r01-M5LGk5p8Zfx_e74bZ0tiLKUTjTw2jTPm_Y47lJok5xHrafA3L9ySDaiWAWcb2WN_15RDSXYJnukWsEhGb4Z4CRWerI6_t0MWujqFZYDBivjar7HDGucw24jrjpTPG7dZ30b9rojaN8J5249dAa-E0LhFjMH78ss0Hg8x00MY1adhaKvYmMK1QwQ3SL4_hvtUOw7ZJDxhdqxs1BWtO-7Ni13f50AyhqMEnuQkQug0tBz2eYyc0")
                .body(content);

        final var service = new LimitServiceGrpc.LimitServiceImplBase() {

            @Override
            public StreamObserver<Limits.ReserveRequest> reserve(StreamObserver<Limits.Response> responseObserver) {
                return new StreamObserver<>() {

                    @Override
                    public void onNext(Limits.ReserveRequest reserveRequest) {
                        responseObserver.onNext(Limits.Response.newBuilder().setId(reserveRequest.getId()).setStatus(Limits.Status.DATA_NOT_FOUND).build());
                        responseObserver.onCompleted();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        responseObserver.onError(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                    }
                };
            }
        };
        final var channel = grpcCleanupExtension.addService(service);
        final var stub = LimitServiceGrpc.newStub(channel);
        when(limitServiceStub.reserve(any())).then(inv -> stub.reserve(inv.getArgument(0)));

        final var initialCount = dsl.fetchCount(Tables.TRIP_ORDER);

        try {
            restTemplate.exchange(entity, String.class);
            fail("Expected exception");
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

            final var response = e.getResponseBodyAs(ExceptionBody.class);

            assertThat(response).isNotNull();
            final var responseEntity = response.getEntity();
            assertThat(responseEntity).isNotNull();
            assertThat(responseEntity.getName()).isEqualTo("Reserve");
            assertThat(response.getMessage()).isEqualTo("Reserve data not found");
        }
        assertThat(dsl.fetchCount(Tables.TRIP_ORDER)).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("Создание заявки. Пустая база. Есть руководитель")
    void test_post_emptyBase_header() throws IOException {
        when(classesProvider.exists(any(), any(), any())).thenReturn(true);

        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();
        final var departmentId = UUID.randomUUID();
        final var positionId = UUID.randomUUID();
        final var headId = UUID.randomUUID();

        when(employeeFunction.apply(userId)).thenReturn(organizationId);
        when(organizationStub.one(any())).thenReturn(OrganizationsOuterClass.Organization.newBuilder().setId(organizationId.toString()).setDigitId(1).build());
        when(departmentStub.one(any())).thenReturn(OrganizationsOuterClass.Department.newBuilder().setId(departmentId.toString()).setHeadId(OrganizationsOuterClass.NullableString.newBuilder().setValue(headId.toString()).build()).build());
        when(employeeStub.one(any())).thenReturn(OrganizationsOuterClass.Employee.newBuilder()
                .setId(userId.toString())
                .setOrganizationId(organizationId.toString())
                .setDepartmentId(departmentId.toString())
                .setPositionId(positionId.toString())
                .setLastName(Instancio.create(String.class))
                .setFirstName(Instancio.create(String.class))
                .build());
        when(pricesStub.request(any())).thenReturn(ExternalTariff.PriceDataResponse.newBuilder().setWaitTime(Instancio.create(Duration.class).toString()).setTime(Instancio.create(Duration.class).toString()).setCost(ExternalTariff.Cost.newBuilder().setIntegerPart(Instancio.create(Long.class)).setFractionPart(Instancio.create(Integer.class)).build()).build());
        when(jwtDecoder.decode(any())).thenReturn(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).claim("roles", "[]").build());


        final var service = new LimitServiceGrpc.LimitServiceImplBase() {

            @Override
            public StreamObserver<Limits.ReserveRequest> reserve(StreamObserver<Limits.Response> responseObserver) {
                return new StreamObserver<>() {

                    @Override
                    public void onNext(Limits.ReserveRequest reserveRequest) {
                        responseObserver.onNext(Limits.Response.newBuilder().setId(reserveRequest.getId()).setStatus(Limits.Status.OK).build());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        responseObserver.onError(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                    }
                };
            }
        };
        final var channel = grpcCleanupExtension.addService(service);
        final var stub = LimitServiceGrpc.newStub(channel);
        when(limitServiceStub.reserve(any())).then(inv -> stub.reserve(inv.getArgument(0)));

        final var content = """
                {
                  "tripDate": "2024-01-01T10:05:10.023+0300",
                  "waypoints": [
                    {
                      "country": "Россия",
                      "region": "Санкт-Петербург",
                      "city": "Санкт-Петербург",
                      "street": "Подвойского",
                      "house": "31",
                      "structure": "1",
                      "latitude": 55.753930,
                      "longitude": 37.620795
                    },
                    {
                      "country": "Россия",
                      "region": "Санкт-Петербург",
                      "city": "Санкт-Петербург",
                      "street": "Подвойского",
                      "house": "31",
                      "structure": "1",
                      "latitude": 55.612550,
                      "longitude": 37.202390
                    }
                  ],
                  "purposeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "tariff": "ECONOMY"
                }
                """;

        final var entity = RequestEntity
                .post("http://localhost:%s/".formatted(port))
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer eyJraWQiOiJqd3QiLCJjdHkiOiJhcHBsaWNhdGlvbi9qc29uIiwidHlwIjoiSldUIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJSdWtsZXZpY2hSLVIiLCJyb2xlcyI6WyJST0xFX0VNUExPWUVFX0NPUlBfQ0xJRU5UIiwiUk9MRV9DSElFRl9DT1JQX0NMSUVOVCIsIlJPTEVfQURNSU5fREFUQV9NQVNURVIiLCJST0xFX0FETUlOX0NPUlBfQ0xJRU5UIl0sImlzcyI6IlNiZXJUcmFuc3BvcnQiLCJ0cmFuc3BvcnQiOmZhbHNlLCJkYXRhX21hc3RlciI6dHJ1ZSwicmFuZG9tIjoiYjcyMWJkYTctMDEzZC00NTZkLWJhMTUtMDY4NmE1NDk1M2I1IiwibmJmIjoxNzQ1NjYyMjAzLCJyZXF1ZXN0SWQiOiJiMzQ4MzVmZWFmY2YxZDc0ZmU3Mjk3ZWMzOTUxOGI0OSIsInNjb3BlIjoiRU1QTE9ZRUUiLCJleHAiOjE3NDYyNjc5MDMsImZhY3RvciI6IkJBU0lDIiwiaWF0IjoxNzQ1NjYyMjAzLCJqdGkiOiIxOGE4YjkzNi03OTY2LTRjZGQtYmM0YS1hZWQzMDMyMzIxYjQifQ.Xs6CHzisPYhsE9V_5x2Rf67DFrecRc5Rm-fW1YM1AqW118rRtx-7mOWPeXybiiaYMDh8FT96ok-jnYS7ylkr6m1BTXOzGLLcdWISpzorv5N0Aob3Bax2_ikB5HwRasa0YxNZyaKKiSmGrXpmIfXGc3Wh_D3_wHaeHT02p7D7i_Ih0M3Ki2EymeAKOiT3yX1CqRkj-H7s8APTHV9IzODHCToqmEZI8ba620UW8EsNGlpe12bIrd-WubdQPMolxtwCSR_Rjxs-uCtQFzXnPHfJOHRgQ-0g9LKhJw1dNpxOW3FBkRn-6dedsza3R4sdxAW1gA_8zq-L7PkSHLTpbZMcfnRmJfxy9umF-ohkryxDiuyvsJ1tdNgwhedXI-TW-OP9Y2TOStLPsKgYUkwm4AAuSB7o1UC5IvarKhg6RbF0wApLgI89zUH7M8r01-M5LGk5p8Zfx_e74bZ0tiLKUTjTw2jTPm_Y47lJok5xHrafA3L9ySDaiWAWcb2WN_15RDSXYJnukWsEhGb4Z4CRWerI6_t0MWujqFZYDBivjar7HDGucw24jrjpTPG7dZ30b9rojaN8J5249dAa-E0LhFjMH78ss0Hg8x00MY1adhaKvYmMK1QwQ3SL4_hvtUOw7ZJDxhdqxs1BWtO-7Ni13f50AyhqMEnuQkQug0tBz2eYyc0")
                .body(content);

        final var response = restTemplate.exchange(entity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(dsl.fetchCount(Tables.TRIP_ORDER)).isEqualTo(1);
        dsl.truncate(Tables.TRIP_ORDER).cascade().execute();
    }

}
