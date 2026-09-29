package ru.sber.transport.request.external.web.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.business.providers.TripOrdersMetaProvider;
import ru.sber.transport.request.external.business.TripOrdersService;
import ru.sber.transport.request.external.model.EditTripOrderData;
import ru.sber.transport.request.external.model.Modifiable;
import ru.sber.transport.request.external.model.State;
import ru.sber.transport.request.external.model.TestTripOrder;
import ru.sber.transport.request.external.model.triporder.TripOrderCreateDTO;
import ru.sber.transport.web.api.ExternalRequestCommandApi;
import ru.sber.transport.web.model.Assessments;
import ru.sber.transport.web.model.EditExternalRequest;
import ru.sber.transport.web.model.NewExternalRequest;
import ru.sber.transport.web.model.Patch;
import ru.sber.transport.web.model.PatchInner;

@SuppressWarnings("unchecked")
@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка делегата заявок")
class RequestCommandDelegateImplTest {

    private final TripOrdersService tripOrdersService = mock(TripOrdersService.class);

    private final TripOrdersMetaProvider tripOrdersMetaProvider = mock(TripOrdersMetaProvider.class);

    private final EmployeeOrganizationFunction employeeOrganizationFunction = mock(EmployeeOrganizationFunction.class);

    private final ExternalRequestCommandApi controller = new RequestCommandDelegateImpl(tripOrdersService,
        tripOrdersMetaProvider, employeeOrganizationFunction, new ObjectMapper());

    @Test
    @DisplayName("Проверка добавления заявки на поездку.")
    void test_add() throws ExecutionException, InterruptedException {
        final var request = Instancio.create(NewExternalRequest.class);
        final var testTripOrder = Instancio.create(TestTripOrder.class);
        final var modifiable = Instancio.create(TestModifiable.class);
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);
        when(tripOrdersService.create(eq(userId), any(TripOrderCreateDTO.class))).thenReturn(testTripOrder);
        when(tripOrdersMetaProvider.meta(organizationId, testTripOrder.getId())).thenReturn(modifiable);

        final var actualResponse = controller.add(request).get();

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(actualResponse.getHeaders().getETag()).isEqualTo("\"%s\"".formatted(modifiable.hash()));
        assertThat(actualResponse.getHeaders().getLastModified()).isEqualTo(modifiable.modifiedAt().truncatedTo(ChronoUnit.SECONDS).toInstant().toEpochMilli());
        assertThat(actualResponse.getHeaders().getLocation()).isEqualTo(URI.create("/" + testTripOrder.getId()));

        final var actual = actualResponse.getBody();

        assertThat(actual).isNotNull();

        assertSoftly(soft -> {
            soft.assertThat(actual.getId()).isEqualTo(testTripOrder.getId());
            soft.assertThat(actual.getPassengerId()).isEqualTo(testTripOrder.getPassenger().getId());
            soft.assertThat(actual.getHumanReadableId()).isEqualTo(testTripOrder.getHumanReadableId());
            soft.assertThat(actual.getTripDate()).isEqualTo(testTripOrder.getDate());
            soft.assertThat(actual.getPurposeId()).isEqualTo(testTripOrder.getPurposeId());
            soft.assertThat(actual.getStatus().name()).isEqualTo(testTripOrder.getStatus().name());
            soft.assertThat(actual.getComment()).isEqualTo(testTripOrder.getComment());
            soft.assertThat(actual.getWaypoints()).hasSameSizeAs(testTripOrder.getWaypoints());
        });
    }

    @Test
    @DisplayName("Проверка редактирования заявки на поездку. Объект изменился")
    void test_edit_modified_defined() throws ExecutionException, InterruptedException {
        final var request = Instancio.create(EditExternalRequest.class);
        final var requestId = UUID.randomUUID();
        final var modifiable = Instancio.of(TestModifiable.class)
                .set(Select.field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-22T00:00:00+03:00"))
                .create();
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(modifiable);

        final var actualResponse = controller.edit(request, requestId, Optional.of(OffsetDateTime.parse("2020-02-20T00:00:00+03:00"))).get();

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.PRECONDITION_FAILED);
    }

    @Test
    @DisplayName("Проверка редактирования заявки на поездку. Объект не изменился")
    void test_edit_unmodified_defined() throws ExecutionException, InterruptedException {
        final var request = new EditExternalRequest();
        request.setStatus(Instancio.create(ru.sber.transport.web.model.State.class));
        request.setFactCost(Instancio.create(BigDecimal.class));
        request.setAssessments(Instancio.create(Assessments.class));

        final var requestId = UUID.randomUUID();
        final var modifiable = Instancio.of(TestModifiable.class)
                .set(Select.field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-20T00:00:00+03:00"))
                .create();
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(modifiable);

        final var actualResponse = controller.edit(request, requestId, Optional.of(OffsetDateTime.parse("2020-02-22T00:00:00+03:00"))).get();
        final var dataCaptor = ArgumentCaptor.forClass(EditTripOrderData.class);
        final var fieldsCaptor = ArgumentCaptor.forClass(Set.class);

        verify(tripOrdersService).edit(eq(userId), eq(false), eq(organizationId), eq(requestId), dataCaptor.capture(), fieldsCaptor.capture());

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(actualResponse.getHeaders().getETag()).isEqualTo("\"%s\"".formatted(modifiable.hash()));
        assertThat(actualResponse.getHeaders().getLastModified()).isEqualTo(modifiable.modifiedAt().truncatedTo(ChronoUnit.SECONDS).toInstant().toEpochMilli());

        final var capturedData = dataCaptor.getValue();
        assertSoftly(it -> {
            it.assertThat(capturedData.getActual().getCost()).isEqualTo(request.getFactCost());
            it.assertThat(capturedData.getStatus().name()).isEqualTo(request.getStatus().name());
            it.assertThat(capturedData.getReason()).isEqualTo(request.getReason());
            it.assertThat(capturedData.getAssessments().getService().getRating()).isEqualTo(request.getAssessments().getService().getRating().byteValue());
            it.assertThat(capturedData.getAssessments().getService().getComment()).isEqualTo(request.getAssessments().getService().getComment());
        });
    }

    @Test
    @DisplayName("Проверка редактирования заявки на поездку")
    void test_edit() throws ExecutionException, InterruptedException {
        final var request = new EditExternalRequest();
        request.setStatus(Instancio.create(ru.sber.transport.web.model.State.class));
        request.setFactCost(Instancio.create(BigDecimal.class));
        request.setAssessments(Instancio.create(Assessments.class));
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        final var requestId = UUID.randomUUID();
        final var modifiable = Instancio.of(TestModifiable.class)
                .set(Select.field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-20T00:00:00+03:00"))
                .create();

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(modifiable);

        final var actualResponse = controller.edit(request, requestId, Optional.empty()).get();

        final var dataCaptor = ArgumentCaptor.forClass(EditTripOrderData.class);

        verify(tripOrdersService).edit(eq(userId), eq(false), eq(organizationId), eq(requestId), dataCaptor.capture(), eq(Set.of("=status", "=factCost", "=reason", "=receiptLink")));

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(actualResponse.getHeaders().getETag()).isEqualTo("\"%s\"".formatted(modifiable.hash()));
        assertThat(actualResponse.getHeaders().getLastModified()).isEqualTo(modifiable.modifiedAt().truncatedTo(ChronoUnit.SECONDS).toInstant().toEpochMilli());

        final var capturedData = dataCaptor.getValue();
        assertSoftly(it -> {
            it.assertThat(capturedData.getActual().getCost()).isEqualTo(request.getFactCost());
            it.assertThat(capturedData.getStatus().name()).isEqualTo(request.getStatus().name());
            it.assertThat(capturedData.getReason()).isEqualTo(request.getReason());
            it.assertThat(capturedData.getAssessments().getService().getRating()).isEqualTo(request.getAssessments().getService().getRating().byteValue());
            it.assertThat(capturedData.getAssessments().getService().getComment()).isEqualTo(request.getAssessments().getService().getComment());
        });
    }

    @Test
    @DisplayName("Проверка частичного редактирования заявки на поездку. Объект изменился")
    void test_edit_partially_modified_defined() throws ExecutionException, InterruptedException {
        final var request = Instancio.create(Patch.class);
        final var requestId = UUID.randomUUID();
        final var modifiable = Instancio.of(TestModifiable.class)
                .set(Select.field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-22T00:00:00+03:00"))
                .create();
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(modifiable);

        final var actualResponse = controller.editPartially(request, requestId, Optional.of(OffsetDateTime.parse("2020-02-20T00:00:00+03:00"))).get();

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.PRECONDITION_FAILED);
    }

    @Test
    @DisplayName("Проверка частичного редактирования заявки на поездку. Объект не изменился")
    void test_edit_partially_unmodified_defined() throws ExecutionException, InterruptedException {
        final var request = new Patch();
        final var patch = new PatchInner(PatchInner.OpEnum.REPLACE, "/status");
        patch.setValue(ru.sber.transport.web.model.State.CONFIRMATION);
        request.add(patch);

        final var requestId = UUID.randomUUID();
        final var modifiable = Instancio.of(TestModifiable.class)
                .set(Select.field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-20T00:00:00+03:00"))
                .create();
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(modifiable);

        final var actualResponse = controller.editPartially(request, requestId, Optional.of(OffsetDateTime.parse("2020-02-22T00:00:00+03:00"))).get();
        final var dataCaptor = ArgumentCaptor.forClass(EditTripOrderData.class);
        final var fieldsCaptor = ArgumentCaptor.forClass(Set.class);

        verify(tripOrdersService).edit(eq(userId), eq(false), eq(organizationId), eq(requestId), dataCaptor.capture(), fieldsCaptor.capture());

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(actualResponse.getHeaders().getETag()).isEqualTo("\"%s\"".formatted(modifiable.hash()));
        assertThat(actualResponse.getHeaders().getLastModified()).isEqualTo(modifiable.modifiedAt().truncatedTo(ChronoUnit.SECONDS).toInstant().toEpochMilli());

        final var capturedData = dataCaptor.getValue();
        assertSoftly(it -> {
            it.assertThat(capturedData.getStatus().name()).isEqualTo(State.CONFIRMATION.name());
            it.assertThat(fieldsCaptor.getValue()).containsExactly("=status");
        });
    }

    @Test
    @DisplayName("Проверка частичного редактирования заявки на поездку. Установка оценки")
    void test_edit_partially_set_assessment() throws ExecutionException, InterruptedException {
        final var request = new Patch();
        final var patch = new PatchInner(PatchInner.OpEnum.REPLACE, "/assessments");
        patch.setValue("""
                {
                  "service": {
                    "rating": 5,
                    "comment": "Good!"
                  }
                }
                """);
        request.add(patch);

        final var requestId = UUID.randomUUID();
        final var modifiable = Instancio.of(TestModifiable.class)
                .set(Select.field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-20T00:00:00+03:00"))
                .create();
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(modifiable);

        final var actualResponse = controller.editPartially(request, requestId, Optional.of(OffsetDateTime.parse("2020-02-22T00:00:00+03:00"))).get();
        final var dataCaptor = ArgumentCaptor.forClass(EditTripOrderData.class);
        final var fieldsCaptor = ArgumentCaptor.forClass(Set.class);

        verify(tripOrdersService).edit(eq(userId), eq(false), eq(organizationId), eq(requestId), dataCaptor.capture(), fieldsCaptor.capture());

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(actualResponse.getHeaders().getETag()).isEqualTo("\"%s\"".formatted(modifiable.hash()));
        assertThat(actualResponse.getHeaders().getLastModified()).isEqualTo(modifiable.modifiedAt().truncatedTo(ChronoUnit.SECONDS).toInstant().toEpochMilli());

        final var capturedData = dataCaptor.getValue();
        assertSoftly(it -> {
            it.assertThat(capturedData.getAssessments().getService().getRating()).isEqualTo((byte) 5);
            it.assertThat(capturedData.getAssessments().getService().getComment()).isEqualTo("Good!");
            it.assertThat(fieldsCaptor.getValue()).containsExactly("=assessments.service.rating", "=assessments.service.comment");
        });
    }

    @Test
    @DisplayName("Проверка частичного редактирования заявки на поездку. Установка оценки сервиса")
    void test_edit_partially_set_assessment_service() throws ExecutionException, InterruptedException {
        final var request = new Patch();
        final var patch = new PatchInner(PatchInner.OpEnum.REPLACE, "/assessments/service");
        patch.setValue("""
                {
                "rating": 4,
                "comment": "Awesome!"
                }
                """);
        request.add(patch);

        final var requestId = UUID.randomUUID();
        final var modifiable = Instancio.of(TestModifiable.class)
                .set(Select.field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-20T00:00:00+03:00"))
                .create();
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(modifiable);

        final var actualResponse = controller.editPartially(request, requestId, Optional.of(OffsetDateTime.parse("2020-02-22T00:00:00+03:00"))).get();
        final var dataCaptor = ArgumentCaptor.forClass(EditTripOrderData.class);
        final var fieldsCaptor = ArgumentCaptor.forClass(Set.class);

        verify(tripOrdersService).edit(eq(userId), eq(false), eq(organizationId), eq(requestId), dataCaptor.capture(), fieldsCaptor.capture());

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(actualResponse.getHeaders().getETag()).isEqualTo("\"%s\"".formatted(modifiable.hash()));
        assertThat(actualResponse.getHeaders().getLastModified()).isEqualTo(modifiable.modifiedAt().truncatedTo(ChronoUnit.SECONDS).toInstant().toEpochMilli());

        final var capturedData = dataCaptor.getValue();
        assertSoftly(it -> {
            it.assertThat(capturedData.getAssessments().getService().getRating()).isEqualTo((byte) 4);
            it.assertThat(capturedData.getAssessments().getService().getComment()).isEqualTo("Awesome!");
            it.assertThat(fieldsCaptor.getValue()).containsExactly("=assessments.service.rating", "=assessments.service.comment");
        });
    }

    @Test
    @DisplayName("Проверка частичного редактирования заявки на поездку. Установка рейтинга сервиса")
    void test_edit_partially_set_assessment_rating() throws ExecutionException, InterruptedException {
        final var request = new Patch();
        final var patch = new PatchInner(PatchInner.OpEnum.ADD, "/assessments/service/rating");
        patch.setValue(3);
        request.add(patch);

        final var requestId = UUID.randomUUID();
        final var modifiable = Instancio.of(TestModifiable.class)
                .set(Select.field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-20T00:00:00+03:00"))
                .create();
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(modifiable);

        final var actualResponse = controller.editPartially(request, requestId, Optional.of(OffsetDateTime.parse("2020-02-22T00:00:00+03:00"))).get();
        final var dataCaptor = ArgumentCaptor.forClass(EditTripOrderData.class);
        final var fieldsCaptor = ArgumentCaptor.forClass(Set.class);

        verify(tripOrdersService).edit(eq(userId), eq(false), eq(organizationId), eq(requestId), dataCaptor.capture(), fieldsCaptor.capture());

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(actualResponse.getHeaders().getETag()).isEqualTo("\"%s\"".formatted(modifiable.hash()));
        assertThat(actualResponse.getHeaders().getLastModified()).isEqualTo(modifiable.modifiedAt().truncatedTo(ChronoUnit.SECONDS).toInstant().toEpochMilli());

        final var capturedData = dataCaptor.getValue();
        assertSoftly(it -> {
            it.assertThat(capturedData.getAssessments().getService().getRating()).isEqualTo((byte) 3);
            it.assertThat(fieldsCaptor.getValue()).containsExactly("+assessments.service.rating");
        });
    }

    @Test
    @DisplayName("Проверка частичного редактирования заявки на поездку. Установка комментария по оценке")
    void test_edit_partially_set_assessment_comment() throws ExecutionException, InterruptedException {
        final var request = new Patch();
        final var patch = new PatchInner(PatchInner.OpEnum.ADD, "/assessments/service/comment");
        patch.setValue("Poor");
        request.add(patch);

        final var requestId = UUID.randomUUID();
        final var modifiable = Instancio.of(TestModifiable.class)
                .set(Select.field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-20T00:00:00+03:00"))
                .create();
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(modifiable);

        final var actualResponse = controller.editPartially(request, requestId, Optional.of(OffsetDateTime.parse("2020-02-22T00:00:00+03:00"))).get();
        final var dataCaptor = ArgumentCaptor.forClass(EditTripOrderData.class);
        final var fieldsCaptor = ArgumentCaptor.forClass(Set.class);

        verify(tripOrdersService).edit(eq(userId), eq(false), eq(organizationId), eq(requestId), dataCaptor.capture(), fieldsCaptor.capture());

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(actualResponse.getHeaders().getETag()).isEqualTo("\"%s\"".formatted(modifiable.hash()));
        assertThat(actualResponse.getHeaders().getLastModified()).isEqualTo(modifiable.modifiedAt().truncatedTo(ChronoUnit.SECONDS).toInstant().toEpochMilli());

        final var capturedData = dataCaptor.getValue();
        assertSoftly(it -> {
            it.assertThat(capturedData.getAssessments().getService().getComment()).isEqualTo("Poor");
            it.assertThat(fieldsCaptor.getValue()).containsExactly("+assessments.service.comment");
        });
    }

    @Test
    @DisplayName("Проверка частичного редактирования заявки на поездку")
    void test_edit_partially() throws ExecutionException, InterruptedException {
        final var request = new Patch();
        final var patch = new PatchInner(PatchInner.OpEnum.ADD, "/status");
        patch.setValue(ru.sber.transport.web.model.State.CONFIRMATION);
        request.add(patch);
        final var patch1 = new PatchInner(PatchInner.OpEnum.REMOVE, "/reason");
        request.add(patch1);

        final var requestId = UUID.randomUUID();
        final var modifiable = Instancio.of(TestModifiable.class)
                .set(Select.field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-20T00:00:00+03:00"))
                .create();
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(modifiable);

        final var actualResponse = controller.editPartially(request, requestId, Optional.of(OffsetDateTime.parse("2020-02-22T00:00:00+03:00"))).get();

        final var dataCaptor = ArgumentCaptor.forClass(EditTripOrderData.class);
        final var fieldsCaptor = ArgumentCaptor.forClass(Set.class);

        verify(tripOrdersService).edit(eq(userId), eq(false), eq(organizationId), eq(requestId), dataCaptor.capture(), fieldsCaptor.capture());

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(actualResponse.getHeaders().getETag()).isEqualTo("\"%s\"".formatted(modifiable.hash()));
        assertThat(actualResponse.getHeaders().getLastModified()).isEqualTo(modifiable.modifiedAt().truncatedTo(ChronoUnit.SECONDS).toInstant().toEpochMilli());

        final var capturedData = dataCaptor.getValue();
        assertSoftly(it -> {
            it.assertThat(capturedData.getStatus().name()).isEqualTo(State.CONFIRMATION.name());
            it.assertThat(capturedData.getReason()).isNull();
            it.assertThat(fieldsCaptor.getValue()).contains("+status", "-reason");
        });
    }

    @Test
    @DisplayName("Проверка удаление заявки на поездку. Объект изменился")
    void test_delete_modified_defined() throws ExecutionException, InterruptedException {
        final var requestId = UUID.randomUUID();
        final var modifiable = Instancio.of(TestModifiable.class)
                .set(Select.field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-22T00:00:00+03:00"))
                .create();
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(modifiable);

        final var actualResponse = controller.delete(requestId, Optional.of(OffsetDateTime.parse("2020-02-20T00:00:00+03:00"))).get();

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.PRECONDITION_FAILED);
    }

    @Test
    @DisplayName("Проверка удаления заявки на поездку. Объект не изменился")
    void test_delete_unmodified_defined() throws ExecutionException, InterruptedException {
        final var requestId = UUID.randomUUID();
        final var modifiable = Instancio.of(TestModifiable.class)
                .set(Select.field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-20T00:00:00+03:00"))
                .create();
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(modifiable);

        final var actualResponse = controller.delete(requestId, Optional.of(OffsetDateTime.parse("2020-02-22T00:00:00+03:00"))).get();

        verify(tripOrdersService).delete(organizationId, requestId);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Проверка удаления заявки на поездку")
    void test_delete() throws ExecutionException, InterruptedException {
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);
        final var requestId = UUID.randomUUID();
        final var modifiable = Instancio.of(TestModifiable.class)
                .set(Select.field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-20T00:00:00+03:00"))
                .create();

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(modifiable);

        final var actualResponse = controller.delete(requestId, Optional.of(OffsetDateTime.parse("2020-02-22T00:00:00+03:00"))).get();

        verify(tripOrdersService).delete(organizationId, requestId);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private record TestModifiable(String hash, OffsetDateTime modifiedAt) implements Modifiable {
    }
}
