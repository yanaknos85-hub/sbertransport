package ru.sber.transport.token_generator.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import ru.sber.transport.token_generator.grpc.dto.GrpcDto;
import ru.sber.transport.token_generator.grpc.providers.RolesProvider;
import ru.sber.transport.token_generator.grpc.service.JwtGeneratingServiceGrpc;
import ru.sber.transport.token_generator.messaging.providers.AccountsProvider;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

import static lombok.AccessLevel.PACKAGE;

@Slf4j
@RequiredArgsConstructor
@GrpcService
@Setter(PACKAGE)
class JwtGeneratorServiceImpl extends JwtGeneratingServiceGrpc.JwtGeneratingServiceImplBase {

    private final JwtEncoder jwtEncoder;

    private final RolesProvider rolesProvider;

    private final AccountsProvider accountsProvider;

    @Value("${jwt.kid:jwt}")
    private String kid;

    @Value("${jwt.algo:RS512}")
    private SignatureAlgorithm algorithm;

    @Override
    public void generate(GrpcDto.UserRequest request, StreamObserver<GrpcDto.JwtData> responseObserver) {
        try {
            var expiration = request.getExpiration();
            var id = request.getId();
            var issuedAt = request.getIssuedAt();
            var subject = request.getSubject();
            var roles = request.getRoleList().stream().toList();
            if (roles.isEmpty()) {
                roles = new ArrayList<>(accountsProvider.findRoles(id));
            }
            log.info("Generating a new token for %s with id %s".formatted(subject, id));
            log.info("with roles: %s".formatted(String.join(", ", roles)));

            var header = JwsHeader.with(algorithm).type("JWT").keyId(kid).build();
            var claims = JwtClaimsSet.builder()
                    .id(id)
                    .expiresAt(Instant.ofEpochSecond(expiration.getSeconds(), expiration.getNanos()))
                    .notBefore(Instant.ofEpochSecond(issuedAt.getSeconds(), issuedAt.getNanos()))
                    .issuedAt(OffsetDateTime.now(ZoneOffset.UTC).toInstant())
                    .subject(subject)
                    .claim("roles", roles)
                    .claim("data_master", rolesProvider.isDataMaster(roles))
                    .build();
            var parameters = JwtEncoderParameters.from(header, claims);

            var token = jwtEncoder.encode(parameters).getTokenValue();

            responseObserver.onNext(GrpcDto.JwtData.newBuilder().setValue(token).build());
        } catch (Exception e) {
            log.error("Generating token failed", e);
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }
}
