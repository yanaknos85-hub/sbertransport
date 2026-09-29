package ru.sber.transport.users.controller.impl;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.files.grpc.model.FileMeta;
import ru.sber.transport.users.service.FileService;
import ru.sber.transport.web.api.AvatarsApi;
import ru.sber.transport.web.api.UserAvatarApi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of controller for working with users.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class UserControllerImpl implements AvatarsApi, UserAvatarApi {

    private final FileService fileService;

    @Setter
    @Value("#{'${avatar.allowedTypes:}'.split(',')}")
    private String[] allowedTypes;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return AvatarsApi.super.getRequest();
    }

    @SneakyThrows(IOException.class)
    @Override
    public ResponseEntity<Void> upload(MultipartFile file) {
        var authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var contentType = file.getContentType();
        try (var stream = file.getInputStream()) {
            if (!List.of(allowedTypes).contains(contentType)) {
                throw new IllegalArgumentException(contentType);
            }
            fileService.upload(authentication.getToken().getId(), stream, contentType, stream.available());
            return ResponseEntity.accepted().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()).build();
        }
    }

    @Override
    public ResponseEntity<Resource> download(String contentRange) {
        var authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return getOfUser(UUID.fromString(authentication.getToken().getId()), contentRange);
    }

    @Override
    public ResponseEntity<Resource> getOfUser(UUID id, String contentRange) {
        try {
            var meta = fileService.meta(id.toString());
            if (contentRange != null && contentRange.startsWith("bytes")) {
                if (!contentRange.contains(" ")) {
                    return createResponse(meta, null);
                } else {
                    var start = Integer.parseInt(contentRange.replace("bytes", "").trim().split("-")[0]);
                    var end = Integer.parseInt(contentRange.split("-")[1]);
                    return createResponse(meta, start, end, fileService.download(meta, start, end));
                }
            } else {
                return createResponse(meta, fileService.download(meta));
            }
        } catch (RuntimeException e) {
            if (e.getCause() instanceof StatusRuntimeException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }

    @Override
    public ResponseEntity<Void> avatarDelete() {
        var authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        fileService.delete(authentication.getToken().getId());
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Resource> createResponse(FileMeta meta, byte[] bytes) {
        var status = HttpStatusCode.valueOf(200);
        var response = ResponseEntity.status(status)
            .contentType(meta.contentType());
        if (bytes != null) {
            return response
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(bytes.length))
                .body(new InputStreamResource(new ByteArrayInputStream(bytes)));
        } else {
            return response.header(HttpHeaders.CONTENT_RANGE, "bytes 0-0/%s".formatted(meta.size())).build();
        }
    }

    private ResponseEntity<Resource> createResponse(FileMeta meta, int start, int end, byte[] bytes) {
        var status = end >= meta.size() ? HttpStatusCode.valueOf(200) : HttpStatusCode.valueOf(206);
        var response = ResponseEntity.status(status)
            .contentType(meta.contentType())
            .header(HttpHeaders.CONTENT_RANGE, createRange(start, end, meta.size()));
        if (bytes != null) {
            return response.body(new InputStreamResource(new ByteArrayInputStream(bytes)));
        }
        return response.build();
    }

    private String createRange(int start, int end, long size) {
        return "bytes %s-%s/%s".formatted(start, end, size);
    }
}
