package ru.sber.transport.request.external.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.qameta.allure.Feature;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.request.external.business.TripOrdersService;
import ru.sber.transport.request.external.model.FileData;
import ru.sber.transport.web.api.ExternalRequestFilesApi;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка делегата файлов")
class RequestFileDelegateImplTest {

    private final TripOrdersService tripOrdersService = mock(TripOrdersService.class);

    private final EmployeeOrganizationFunction employeeOrganizationFunction = mock(EmployeeOrganizationFunction.class);

    private final ExternalRequestFilesApi delegate = new RequestFileDelegateImpl(tripOrdersService, employeeOrganizationFunction);

    @Test
    @DisplayName("Проверка привязки файла")
    void test_attachFile() throws IOException, ExecutionException, InterruptedException {
        final var dir = Files.createDirectories(Path.of("target", "test"));
        final var file = dir.resolve("file.txt");
        Files.write(file, "test file".getBytes());
        final var orderId = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        final var multipartFile = new MockMultipartFile("file", "file.txt", "text/plain", Files.readAllBytes(new File("target/test/file.txt").toPath()));

        delegate.postFile(orderId, multipartFile).get();

        final var pathCaptor = ArgumentCaptor.forClass(Path.class);
        verify(tripOrdersService).attachFile(eq(organizationId), eq(orderId), eq("file.txt"), eq("text/plain"), pathCaptor.capture());

        assertThat(Files.readString(pathCaptor.getValue())).isEqualTo("test file");
        Files.delete(file);
        Files.delete(dir);
    }

    @Test
    @DisplayName("Проверка получения файла")
    void test_getFile() throws IOException, ExecutionException, InterruptedException {
        final var dir = Files.createDirectories(Path.of("target", "test"));
        final var file = dir.resolve("file.txt");
        Files.write(file, "test file".getBytes());
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        final var requestId = UUID.randomUUID();
        final var data = new FileData() {
            @Override
            public Path getPath() {
                return file;
            }

            @Override
            public long getFrom() {
                return 0;
            }

            @SneakyThrows
            @Override
            public long getTo() {
                return Files.size(file);
            }

            @SneakyThrows
            @Override
            public long getSize() {
                return Files.size(file);
            }

            @Override
            public String getFileName() {
                return "file.txt";
            }

            @Override
            public String getContentType() {
                return "text/plain";
            }
        };

        when(tripOrdersService.getFile(organizationId, requestId, 0, -1)).thenReturn(data);

        final var response = delegate.getFile(requestId, Optional.empty()).get();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        //noinspection DataFlowIssue
        assertThat(response.getBody().getContentAsByteArray()).isEqualTo(Files.readAllBytes(file));
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_RANGE)).isNull();
        assertThat(response.getHeaders().getContentDisposition().isAttachment()).isTrue();
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("file.txt");
        assertThat(response.getHeaders().getContentType()).hasToString("text/plain");
        assertThat(response.getHeaders().getContentLength()).isEqualTo(9);
        Files.delete(file);
        Files.delete(dir);
    }

    @Test
    @DisplayName("Проверка получения части файла")
    void test_getFile_part() throws IOException, ExecutionException, InterruptedException {
        final var dir = Files.createDirectories(Path.of("target", "test"));
        final var file = dir.resolve("file.txt");
        Files.write(file, "test file".getBytes());
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        final var requestId = UUID.randomUUID();
        final var data = new FileData() {
            @Override
            public Path getPath() {
                return file;
            }

            @Override
            public long getFrom() {
                return 3L;
            }

            @SneakyThrows
            @Override
            public long getTo() {
                return 5L;
            }

            @SneakyThrows
            @Override
            public long getSize() {
                return Files.size(file);
            }

            @Override
            public String getFileName() {
                return "file.txt";
            }

            @Override
            public String getContentType() {
                return "text/plain";
            }
        };

        when(tripOrdersService.getFile(organizationId, requestId, 3, 5)).thenReturn(data);

        final var response = delegate.getFile(requestId, Optional.of("bytes 3-5")).get();

        assertThat(response.getStatusCode().value()).isEqualTo(206);
        //noinspection DataFlowIssue
        assertThat(response.getBody().getContentAsByteArray()).isEqualTo(Files.readAllBytes(file));
        //noinspection DataFlowIssue
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_RANGE).getFirst()).isEqualTo("bytes 3-5/9");
        assertThat(response.getHeaders().getContentDisposition().isAttachment()).isTrue();
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("file.txt");
        assertThat(response.getHeaders().getContentType()).hasToString("text/plain");
        assertThat(response.getHeaders().getContentLength()).isEqualTo(9);
        Files.delete(file);
        Files.delete(dir);
    }

    @Test
    @DisplayName("Проверка получения последней части файла")
    void test_getFile_last_part() throws IOException, ExecutionException, InterruptedException {
        final var dir = Files.createDirectories(Path.of("target", "test"));
        final var file = dir.resolve("file.txt");
        Files.write(file, "test file".getBytes());
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        final var requestId = UUID.randomUUID();
        final var data = new FileData() {
            @Override
            public Path getPath() {
                return file;
            }

            @Override
            public long getFrom() {
                return 3L;
            }

            @SneakyThrows
            @Override
            public long getTo() {
                return Files.size(file);
            }

            @SneakyThrows
            @Override
            public long getSize() {
                return Files.size(file);
            }

            @Override
            public String getFileName() {
                return "file.txt";
            }

            @Override
            public String getContentType() {
                return "text/plain";
            }
        };

        when(tripOrdersService.getFile(organizationId, requestId, 3, 9)).thenReturn(data);

        final var response = delegate.getFile(requestId, Optional.of("bytes 3-9")).get();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        //noinspection DataFlowIssue
        assertThat(response.getBody().getContentAsByteArray()).isEqualTo(Files.readAllBytes(file));
        //noinspection DataFlowIssue
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_RANGE).getFirst()).isEqualTo("bytes 3-9/9");
        assertThat(response.getHeaders().getContentDisposition().isAttachment()).isTrue();
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("file.txt");
        assertThat(response.getHeaders().getContentType()).hasToString("text/plain");
        assertThat(response.getHeaders().getContentLength()).isEqualTo(9);
        Files.delete(file);
        Files.delete(dir);
    }

    @Test
    @DisplayName("Проверка удаления файла")
    void test_delete() throws ExecutionException, InterruptedException {
        final var requestId = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        delegate.deleteFile(requestId).get();

        verify(tripOrdersService).deleteFile(organizationId, requestId);
    }

}