package ru.sber.transport.push.business.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.push.business.PushSender;
import ru.sber.transport.push.business.dto.PlatformType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Конфигурирование отправителей.
 */
@Configuration
public class ConfigSenders {

    private static final String CLASSPATH = "classpath:";

    @Bean
    Map<PlatformType, PushSender> configureSenders(List<PushSender> senders) {
        return senders.stream().collect(Collectors.toMap(PushSender::type, Function.identity()));
    }

    @SneakyThrows(IOException.class)
    @Bean
    FirebaseMessaging configureFirebase(@Value("${android.credentials.path:changeit}") String credentialsPath,
                                        @Value("${android.appName:[DEFAULT]}") String appName) {
        try (var file = getFile(credentialsPath)) {
            var googleCredentials = GoogleCredentials.fromStream(file);
            var firebaseOptions = FirebaseOptions.builder().setCredentials(googleCredentials).build();
            var app = FirebaseApp.initializeApp(firebaseOptions, appName);
            return FirebaseMessaging.getInstance(app);
        }
    }

    private @NonNull InputStream getFile(String credentialsPath) throws FileNotFoundException {
        if (credentialsPath.startsWith(CLASSPATH)) {
            var resource = getClass().getClassLoader().getResource("");
            assert resource != null;
            var root = resource.getFile();
            return new FileInputStream(credentialsPath.replace(CLASSPATH, root));
        } else {
            return new FileInputStream(credentialsPath);
        }
    }

}
