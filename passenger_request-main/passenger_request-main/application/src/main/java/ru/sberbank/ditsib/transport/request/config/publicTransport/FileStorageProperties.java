package ru.sberbank.ditsib.transport.request.config.publicTransport;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file")
@Getter
@Setter
public class FileStorageProperties {
    
    private String uploadDir;
}
