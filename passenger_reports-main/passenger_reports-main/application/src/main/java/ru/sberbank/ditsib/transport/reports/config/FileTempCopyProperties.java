package ru.sberbank.ditsib.transport.reports.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file")
@Getter
@Setter
public class FileTempCopyProperties {
    
    private String uploadDir;
    
}
