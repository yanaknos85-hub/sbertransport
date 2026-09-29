package ru.sber.transport.request.external.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.system.ApplicationTemp;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.request.external.business.TripOrdersService;
import ru.sber.transport.web.api.ExternalRequestFilesApi;

/**
 * Делегат для работы с файлами заявок на поездки
 */
@RequiredArgsConstructor
public class RequestFileDelegateImpl implements ExternalRequestFilesApi {

    private final TripOrdersService tripOrdersService;

    private final EmployeeOrganizationFunction employeeOrganizationFunction;

    private final File tempDir = new ApplicationTemp().getDir("files");

    @SuppressWarnings("java:S6300")
    @Override
    public CompletableFuture<ResponseEntity<Void>> postFile(UUID requestId, MultipartFile file) {
        final var employeeOrganization = getEmployeeOrganization();
        final var path = tempDir.toPath().resolve(UUID.randomUUID().toString());
        try (final var fis = file.getInputStream();
             final var fos = new FileOutputStream(path.toAbsolutePath().toString())) {
            final var bufferSize = 1024;
            var buffer = new byte[Math.min(bufferSize, fis.available())];
            while (buffer.length > 0) {
                //noinspection ResultOfMethodCallIgnored
                fis.read(buffer, 0, buffer.length);
                fos.write(buffer, 0, buffer.length);
                buffer = new byte[Math.min(bufferSize, fis.available())];
            }
            fos.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return CompletableFuture.supplyAsync(() -> {
            final var originalFilename = file.getOriginalFilename();
            tripOrdersService.attachFile(employeeOrganization, requestId, originalFilename, Optional.ofNullable(file.getContentType()).orElseGet(() -> getContentType(originalFilename)), path);
            return ResponseEntity.accepted().build();
        });
    }

    @Override
    public CompletableFuture<ResponseEntity<Resource>> getFile(UUID requestId, Optional<String> contentRange) {
        final var employeeOrganization = getEmployeeOrganization();
        return CompletableFuture.supplyAsync(() -> {
            var from = 0;
            var to = -1;
            if (contentRange.isPresent()) {
                final var contentParts = contentRange.get().split(" ")[1].split("-");
                from = Integer.parseInt(contentParts[0]);
                to = Integer.parseInt(contentParts[1]);
            }
            final var path = tripOrdersService.getFile(employeeOrganization, requestId, from, to);
            final var status = path.getTo() != path.getSize() ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;
            final var range = to == -1 ? null : String.format("bytes %d-%d/%d", from, to, path.getSize());

            var responseBuilder = ResponseEntity.status(status);
            if (to > -1) {
                responseBuilder = responseBuilder.header(HttpHeaders.CONTENT_RANGE, range);
            }
            return responseBuilder
                    .header(HttpHeaders.CONTENT_TYPE, path.getContentType())
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(path.getSize()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(path.getFileName()).build().toString())
                    .body(new FileSystemResource(path.getPath()));
        });
    }

    @Override
    public CompletableFuture<ResponseEntity<Void>> deleteFile(UUID requestId) {
        final var employeeOrganization = getEmployeeOrganization();
        return CompletableFuture.supplyAsync(() -> {
            tripOrdersService.deleteFile(employeeOrganization, requestId);
            return ResponseEntity.noContent().build();
        });
    }

    @SneakyThrows(IOException.class)
    private String getContentType(String originalFilename) {
        return Files.probeContentType(Path.of(originalFilename));
    }

    private @Nullable UUID getEmployeeOrganization() {
        return ControllerUtils.isDataMaster() ? null : employeeOrganizationFunction.apply(ControllerUtils.currentUser());
    }
}
