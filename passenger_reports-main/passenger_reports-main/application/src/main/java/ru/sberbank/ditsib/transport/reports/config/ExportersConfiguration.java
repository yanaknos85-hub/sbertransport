package ru.sberbank.ditsib.transport.reports.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.service.XlsxExporter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
class ExportersConfiguration {
    
    @Bean
    Map<TransportTypeEnum, XlsxExporter> exporters(List<XlsxExporter> exporterList) {
        return exporterList.stream().collect(Collectors.toMap(XlsxExporter::transportType, Function.identity()));
    }
    
}
