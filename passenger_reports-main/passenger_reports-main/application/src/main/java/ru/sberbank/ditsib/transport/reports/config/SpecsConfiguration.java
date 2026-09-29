package ru.sberbank.ditsib.transport.reports.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dto.IVisibilityDto;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.service.Mappings;
import ru.sberbank.ditsib.transport.reports.service.ReportsSpecService;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Configuration
class SpecsConfiguration {
    
    @Bean
    Map<TransportTypeEnum, ReportsSpecService<RequestReportDTO>> specs(List<ReportsSpecService<? extends RequestReportDTO>> services) {
        log.debug("Found {} spec-services", services.size());
        var svcs = services.stream().collect(Collectors.toMap(ReportsSpecService::transportType, Function.identity()));
        log.debug("Services: {}", svcs);
        return ReflectionUtils.cast(svcs);
    }
    
    @Bean
    Map<TransportTypeEnum, Mappings<IVisibilityDto>> mappings(List<Mappings<?>> mappings) {
        log.debug("Found {} mappings", mappings.size());
        var maps = mappings.stream().collect(Collectors.toMap(Mappings::transportType, Function.identity()));
        log.debug("Mappings: {}", maps);
        return ReflectionUtils.cast(maps);
    }
    
}
