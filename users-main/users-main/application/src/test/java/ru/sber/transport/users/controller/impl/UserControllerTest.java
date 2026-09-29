package ru.sber.transport.users.controller.impl;

import com.google.common.net.HttpHeaders;
import io.qameta.allure.Feature;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.files.grpc.model.FileMeta;
import ru.sber.transport.users.service.FileService;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_users")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера пользователей")
@ActiveProfiles("test")
class UserControllerTest {
    
    public static final String IMAGES_AVATARS = "target/test/images/avatars";
    
    private static final String USER_ID = "1a9734e4-15c7-4776-af98-de6d492077dd";
    
    private static final String USER_ROLE = "ROLE_GUEST";
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    protected JwtDecoder jwtDecoder;

    @MockitoBean
    private FileService fileService;
    
    @MockitoBean
    private AuthorizationManager<?> authorizationManager;
    
    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(authorizationManager, USER_ROLE);
    }
    
    private static Stream<Arguments> saveAvatarSource() {
        return Stream.of(
                Arguments.of("image/bmp"),
                Arguments.of("image/gif"),
                Arguments.of("image/ico"),
                Arguments.of("image/jpg"),
                Arguments.of("image/png"),
                Arguments.of("image/svg"),
                Arguments.of("image/webp"),
                Arguments.of("image/tiff")
                        );
    }
    
    @ParameterizedTest
    @DisplayName("Сохранение аватара")
    @MethodSource("saveAvatarSource")
    void should_saveAvatar(String extension) throws Exception {
        var resource = getClass().getClassLoader().getResource("testAvatar/avatar." + extension.split("/")[1]);
        assertThat(resource).isNotNull();
        var fileName = resource.getFile();
        assertThat(fileName).isNotNull();
        
        var file = new MockMultipartFile("avatar", "avatar." + extension.split("/")[1], extension, new FileInputStream(fileName));
        
        mockMvc.perform(multipart("/avatar").file(file)
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority(USER_ROLE))))
               .andExpect(status().isAccepted());

        var inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(fileService).upload(eq(USER_ID), inputStreamCaptor.capture(), eq(extension), eq(new File(fileName).length()));

        var actual = inputStreamCaptor.getValue();
        assertThat(actual).isNotNull();
        assertThat(actual.readAllBytes()).isEqualTo(Files.readAllBytes(new File(resource.getFile()).toPath()));
    }
    
    @Test
    @DisplayName("Сохранение аватара. Не картинка")
    void should_saveAvatar_noImage() throws Exception {
        var resource = getClass().getClassLoader().getResource("application.yml");
        assertThat(resource).isNotNull();
        var fileName = resource.getFile();
        assertThat(fileName).isNotNull();
        
        var file = new MockMultipartFile("avatar", "avatar.yml", "application/yml", new FileInputStream(fileName));

        mockMvc.perform(multipart("/avatar").file(file)
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority(USER_ROLE))))
               .andExpect(status().isUnsupportedMediaType());
    }
    
    @ParameterizedTest
    @DisplayName("Получение аватара")
    @MethodSource("saveAvatarSource")
    void should_downloadAvatar(String extension) throws Exception {
        var root = new File(IMAGES_AVATARS);
        if (!root.isDirectory()) {
            assertThat(root.mkdirs()).isTrue();
        }
    
        var resource = getClass().getClassLoader().getResource("testAvatar/avatar." + extension.split("/")[1]);
        assertThat(resource).isNotNull();
        var fileName = resource.getFile();
        assertThat(fileName).isNotNull();
    
        var srcFile = new File(fileName);
        
        var srcBytes = new byte[0];
        try(var fis = new FileInputStream(srcFile);
        var baos = new ByteArrayOutputStream()) {
            IOUtils.copy(fis, baos);
            srcBytes = baos.toByteArray();
        }
        assertThat(srcBytes).isNotEmpty();
        
        FileUtils.copyFile(srcFile, new File(IMAGES_AVATARS + "/" + USER_ID + ".file"));
        try (var fos = new FileOutputStream(IMAGES_AVATARS + "/" + USER_ID + ".mime")) {
            fos.write((extension).getBytes());
        }

        var meta = new FileMeta(USER_ID, MediaType.parseMediaType(extension), srcFile.length());
        when(fileService.meta(USER_ID)).thenReturn(meta);
        when(fileService.download(meta)).thenReturn(srcBytes);
        
        mockMvc.perform(get("/avatar").with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority(USER_ROLE))))
               .andExpect(status().isOk())
               .andExpect(content().contentType(extension))
               .andExpect(content().bytes(srcBytes));
    }

    @ParameterizedTest
    @DisplayName("Получение мета аватара")
    @MethodSource("saveAvatarSource")
    void should_downloadAvatar_meta(String extension) throws Exception {
        var root = new File(IMAGES_AVATARS);
        if (!root.isDirectory()) {
            assertThat(root.mkdirs()).isTrue();
        }

        var resource = getClass().getClassLoader().getResource("testAvatar/avatar." + extension.split("/")[1]);
        assertThat(resource).isNotNull();
        var fileName = resource.getFile();
        assertThat(fileName).isNotNull();

        var srcFile = new File(fileName);

        var srcBytes = new byte[0];
        try(var fis = new FileInputStream(srcFile);
        var baos = new ByteArrayOutputStream()) {
            IOUtils.copy(fis, baos);
            srcBytes = baos.toByteArray();
        }
        assertThat(srcBytes).isNotEmpty();

        FileUtils.copyFile(srcFile, new File(IMAGES_AVATARS + "/" + USER_ID + ".file"));
        try (var fos = new FileOutputStream(IMAGES_AVATARS + "/" + USER_ID + ".mime")) {
            fos.write((extension).getBytes());
        }

        var meta = new FileMeta(USER_ID, MediaType.parseMediaType(extension), srcFile.length());
        when(fileService.meta(USER_ID)).thenReturn(meta);
        when(fileService.download(meta)).thenReturn(srcBytes);

        mockMvc.perform(get("/avatar")
                .header(HttpHeaders.CONTENT_RANGE, "bytes")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority(USER_ROLE))))
               .andExpectAll(
                   status().isOk(),
                   header().string(HttpHeaders.CONTENT_RANGE, "bytes 0-0/%s".formatted(srcBytes.length))
               );
    }

    @ParameterizedTest
    @DisplayName("Получение части аватара")
    @MethodSource("saveAvatarSource")
    void should_downloadAvatar_part(String extension) throws Exception {
        var root = new File(IMAGES_AVATARS);
        if (!root.isDirectory()) {
            assertThat(root.mkdirs()).isTrue();
        }

        var resource = getClass().getClassLoader().getResource("testAvatar/avatar." + extension.split("/")[1]);
        assertThat(resource).isNotNull();
        var fileName = resource.getFile();
        assertThat(fileName).isNotNull();

        var srcFile = new File(fileName);

        var srcBytes = new byte[100];
        try(var fis = new FileInputStream(srcFile);
        var baos = new ByteArrayOutputStream()) {
            baos.write(fis.readNBytes(100));
            srcBytes = baos.toByteArray();
        }
        assertThat(srcBytes).isNotEmpty();

        FileUtils.copyFile(srcFile, new File(IMAGES_AVATARS + "/" + USER_ID + ".file"));
        try (var fos = new FileOutputStream(IMAGES_AVATARS + "/" + USER_ID + ".mime")) {
            fos.write((extension).getBytes());
        }

        var meta = new FileMeta(USER_ID, MediaType.parseMediaType(extension), srcFile.length());
        when(fileService.meta(USER_ID)).thenReturn(meta);
        when(fileService.download(meta, 0, 100)).thenReturn(srcBytes);

        mockMvc.perform(get("/avatar")
                .header(HttpHeaders.CONTENT_RANGE, "bytes 0-100")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority(USER_ROLE))))
               .andExpectAll(
                   status().isPartialContent(),
                   header().string(HttpHeaders.CONTENT_RANGE, "bytes 0-100/%s".formatted(srcFile.length())),
                   content().bytes(Arrays.copyOf(srcBytes, 100))
               );
    }
    
    @Test
    @DisplayName("Удаление аватара")
    void should_deleteAvatar() throws Exception {
        mockMvc.perform(delete("/avatar")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority(USER_ROLE))))
               .andExpect(status().isNoContent());

        verify(fileService).delete(USER_ID);
    }
}