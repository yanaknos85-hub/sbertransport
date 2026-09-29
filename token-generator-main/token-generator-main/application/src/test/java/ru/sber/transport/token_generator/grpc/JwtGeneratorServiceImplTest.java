package ru.sber.transport.token_generator.grpc;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.token_generator.grpc.dto.GrpcDto;
import ru.sber.transport.token_generator.grpc.providers.RolesProvider;
import ru.sber.transport.token_generator.messaging.providers.AccountsProvider;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_token_generator")
@DisplayName("Проверка генерации токена")
class JwtGeneratorServiceImplTest {

    private final JwtEncoder jwtEncoder = mock(JwtEncoder.class);

    private final RolesProvider roleProvider = mock(RolesProvider.class);

    private final AccountsProvider accountsProvider = mock(AccountsProvider.class);

    private final JwtGeneratorServiceImpl generatorService = new JwtGeneratorServiceImpl(jwtEncoder, roleProvider, accountsProvider);

    @BeforeEach
    void setup() {
        generatorService.setAlgorithm(SignatureAlgorithm.RS512);
        generatorService.setKid("jwt");
    }

    @Test
    @DisplayName("Генерация. Не МД")
    void test_generate_noSmd() {
        var issued = OffsetDateTime.now(ZoneOffset.UTC).toInstant();
        var expiration = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).toInstant();
        var request = GrpcDto.UserRequest.newBuilder()
                .setId(Instancio.create(String.class))
                .setExpiration(Timestamp.newBuilder().setSeconds(expiration.getEpochSecond()).setNanos(expiration.getNano()).build())
                .setIssuedAt(Timestamp.newBuilder().setSeconds(issued.getEpochSecond()).setNanos(issued.getNano()).build())
                .setSubject(Instancio.create(String.class))
                .addAllRole(Instancio.ofList(String.class).create())
                .build();
        var actualList = new ArrayList<GrpcDto.JwtData>();
        var completed = new AtomicBoolean(false);
        var errors = new AtomicReference<Throwable>();
        var observer = new StreamObserver<GrpcDto.JwtData>() {

            @Override
            public void onNext(GrpcDto.JwtData jwtData) {
                actualList.add(jwtData);
            }

            @Override
            public void onError(Throwable throwable) {
                errors.set(throwable);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        };

        when(jwtEncoder.encode(any())).thenAnswer(inv -> {
            var params = inv.getArgument(0, JwtEncoderParameters.class);
            var claims = params.getClaims();
            return Jwt.withTokenValue("token").jti(claims.getId()).expiresAt(claims.getExpiresAt()).notBefore(claims.getNotBefore())
                    .issuedAt(claims.getIssuedAt()).subject(claims.getSubject()).claim("roles", claims.getClaim("roles"))
                    .claim("data_master", claims.getClaim("data_master"))
                    .headers(map -> map.putAll(Objects.requireNonNull(params.getJwsHeader()).getHeaders())).build();
        });
        when(roleProvider.isDataMaster(anyList())).thenReturn(false);

        generatorService.generate(request, observer);

        var parametersCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(parametersCaptor.capture());

        assertThat(errors.get()).isNull();
        assertThat(completed).isTrue();
        assertThat(actualList.get(0).getValue()).isEqualTo("token");
        var parameters = parametersCaptor.getValue();
        assertThat(parameters.getClaims().getId()).isEqualTo(request.getId());
        assertThat(parameters.getClaims().getExpiresAt().getEpochSecond()).isEqualTo(request.getExpiration().getSeconds());
        assertThat(parameters.getClaims().getExpiresAt().getNano()).isEqualTo(request.getExpiration().getNanos());
        assertThat(parameters.getClaims().getNotBefore().getEpochSecond()).isEqualTo(request.getIssuedAt().getSeconds());
        assertThat(parameters.getClaims().getNotBefore().getNano()).isEqualTo(request.getIssuedAt().getNanos());
        assertThat(parameters.getClaims().getSubject()).isEqualTo(request.getSubject());
        assertThat(parameters.getClaims().<List<String>>getClaim("roles")).hasSameElementsAs(request.getRoleList().stream().toList());
        assertThat(parameters.getClaims().<Boolean>getClaim("data_master")).isFalse();
        assert parameters.getJwsHeader() != null;
        assertThat(parameters.getJwsHeader().getAlgorithm()).isEqualTo(SignatureAlgorithm.RS512);
        assertThat(parameters.getJwsHeader().getType()).isEqualTo("JWT");
        assertThat(parameters.getJwsHeader().getKeyId()).isEqualTo("jwt");
    }

    @Test
    @DisplayName("Генерация. МД")
    void test_generate_smd() {
        var issued = OffsetDateTime.now(ZoneOffset.UTC).toInstant();
        var expiration = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).toInstant();
        var request = GrpcDto.UserRequest.newBuilder()
                .setId(Instancio.create(String.class))
                .setExpiration(Timestamp.newBuilder().setSeconds(expiration.getEpochSecond()).setNanos(expiration.getNano()).build())
                .setIssuedAt(Timestamp.newBuilder().setSeconds(issued.getEpochSecond()).setNanos(issued.getNano()).build())
                .setSubject(Instancio.create(String.class))
                .addAllRole(Instancio.ofList(String.class).create())
                .build();
        var actualList = new ArrayList<GrpcDto.JwtData>();
        var completed = new AtomicBoolean(false);
        var errors = new AtomicReference<Throwable>();
        var observer = new StreamObserver<GrpcDto.JwtData>() {

            @Override
            public void onNext(GrpcDto.JwtData jwtData) {
                actualList.add(jwtData);
            }

            @Override
            public void onError(Throwable throwable) {
                errors.set(throwable);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        };

        when(jwtEncoder.encode(any())).thenAnswer(inv -> {
            var params = inv.getArgument(0, JwtEncoderParameters.class);
            var claims = params.getClaims();
            return Jwt.withTokenValue("token").jti(claims.getId()).expiresAt(claims.getExpiresAt()).notBefore(claims.getNotBefore())
                    .issuedAt(claims.getIssuedAt()).subject(claims.getSubject()).claim("roles", claims.getClaim("roles"))
                    .claim("data_master", claims.getClaim("data_master"))
                    .headers(map -> map.putAll(Objects.requireNonNull(params.getJwsHeader()).getHeaders())).build();
        });
        when(roleProvider.isDataMaster(anyList())).thenReturn(true);

        generatorService.generate(request, observer);

        var parametersCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(parametersCaptor.capture());

        assertThat(errors.get()).isNull();
        assertThat(completed).isTrue();
        assertThat(actualList.get(0).getValue()).isEqualTo("token");
        var parameters = parametersCaptor.getValue();
        assertThat(parameters.getClaims().getId()).isEqualTo(request.getId());
        assertThat(parameters.getClaims().getExpiresAt().getEpochSecond()).isEqualTo(request.getExpiration().getSeconds());
        assertThat(parameters.getClaims().getExpiresAt().getNano()).isEqualTo(request.getExpiration().getNanos());
        assertThat(parameters.getClaims().getNotBefore().getEpochSecond()).isEqualTo(request.getIssuedAt().getSeconds());
        assertThat(parameters.getClaims().getNotBefore().getNano()).isEqualTo(request.getIssuedAt().getNanos());
        assertThat(parameters.getClaims().getSubject()).isEqualTo(request.getSubject());
        assertThat(parameters.getClaims().<List<String>>getClaim("roles")).hasSameElementsAs(request.getRoleList().stream().toList());
        assertThat(parameters.getClaims().<Boolean>getClaim("data_master")).isTrue();
        assert parameters.getJwsHeader() != null;
        assertThat(parameters.getJwsHeader().getAlgorithm()).isEqualTo(SignatureAlgorithm.RS512);
        assertThat(parameters.getJwsHeader().getType()).isEqualTo("JWT");
        assertThat(parameters.getJwsHeader().getKeyId()).isEqualTo("jwt");
    }

    @Test
    @DisplayName("Генерация. Роли не присланы")
    void test_roles_not_defined() {
        var issued = OffsetDateTime.now(ZoneOffset.UTC).toInstant();
        var expiration = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).toInstant();
        var request = GrpcDto.UserRequest.newBuilder()
                .setId(Instancio.create(String.class))
                .setExpiration(Timestamp.newBuilder().setSeconds(expiration.getEpochSecond()).setNanos(expiration.getNano()).build())
                .setIssuedAt(Timestamp.newBuilder().setSeconds(issued.getEpochSecond()).setNanos(issued.getNano()).build())
                .setSubject(Instancio.create(String.class))
                .addAllRole(List.of())
                .build();
        var actualList = new ArrayList<GrpcDto.JwtData>();
        var completed = new AtomicBoolean(false);
        var errors = new AtomicReference<Throwable>();
        var observer = new StreamObserver<GrpcDto.JwtData>() {

            @Override
            public void onNext(GrpcDto.JwtData jwtData) {
                actualList.add(jwtData);
            }

            @Override
            public void onError(Throwable throwable) {
                errors.set(throwable);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        };

        when(accountsProvider.findRoles(request.getId())).thenReturn(List.of("ROLE_1", "ROLE_2"));
        when(jwtEncoder.encode(any())).thenAnswer(inv -> {
            var params = inv.getArgument(0, JwtEncoderParameters.class);
            var claims = params.getClaims();
            return Jwt.withTokenValue("token").jti(claims.getId()).expiresAt(claims.getExpiresAt()).notBefore(claims.getNotBefore())
                    .issuedAt(claims.getIssuedAt()).subject(claims.getSubject()).claim("roles", claims.getClaim("roles"))
                    .claim("data_master", claims.getClaim("data_master"))
                    .headers(map -> map.putAll(Objects.requireNonNull(params.getJwsHeader()).getHeaders())).build();
        });
        when(roleProvider.isDataMaster(anyList())).thenReturn(true);

        generatorService.generate(request, observer);

        var parametersCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(parametersCaptor.capture());

        assertThat(errors.get()).isNull();
        assertThat(completed).isTrue();
        assertThat(actualList.get(0).getValue()).isEqualTo("token");
        var parameters = parametersCaptor.getValue();
        assertThat(parameters.getClaims().getId()).isEqualTo(request.getId());
        assertThat(parameters.getClaims().getExpiresAt().getEpochSecond()).isEqualTo(request.getExpiration().getSeconds());
        assertThat(parameters.getClaims().getExpiresAt().getNano()).isEqualTo(request.getExpiration().getNanos());
        assertThat(parameters.getClaims().getNotBefore().getEpochSecond()).isEqualTo(request.getIssuedAt().getSeconds());
        assertThat(parameters.getClaims().getNotBefore().getNano()).isEqualTo(request.getIssuedAt().getNanos());
        assertThat(parameters.getClaims().getSubject()).isEqualTo(request.getSubject());
        assertThat(parameters.getClaims().<List<String>>getClaim("roles")).hasSameElementsAs(List.of("ROLE_1", "ROLE_2"));
        assertThat(parameters.getClaims().<Boolean>getClaim("data_master")).isTrue();
        assert parameters.getJwsHeader() != null;
        assertThat(parameters.getJwsHeader().getAlgorithm()).isEqualTo(SignatureAlgorithm.RS512);
        assertThat(parameters.getJwsHeader().getType()).isEqualTo("JWT");
        assertThat(parameters.getJwsHeader().getKeyId()).isEqualTo("jwt");
    }

}