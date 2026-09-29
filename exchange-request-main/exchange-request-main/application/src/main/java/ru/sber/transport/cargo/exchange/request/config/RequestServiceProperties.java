package ru.sber.transport.cargo.exchange.request.config;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "exchange.request.draft")
@Setter
@Getter
@Valid
public class RequestServiceProperties {

    private Integer expiredDays = 30;

    private String prefix = "ОР";
}
