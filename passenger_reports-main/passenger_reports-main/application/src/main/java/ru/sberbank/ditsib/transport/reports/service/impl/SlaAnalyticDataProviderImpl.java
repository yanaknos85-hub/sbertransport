package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.reports.dao.StatsRepository;
import ru.sberbank.ditsib.transport.reports.dto.analytic.*;
import ru.sberbank.ditsib.transport.reports.model.Stats;
import ru.sberbank.ditsib.transport.reports.service.AnalyticDataProvider;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
@Transactional
@Slf4j
@Component
@RequiredArgsConstructor
public class SlaAnalyticDataProviderImpl implements AnalyticDataProvider {
    
    private final StatsRepository statsRepository;
    
    @Override
    public ChartDTO getChartData(GeneralAnalyticalReportRequestDTO request, List<Stats> statsList) {
        
        log.info("Start: SlaAnalyticDataProviderImpl.getChartData(organizationId={}, year={}, transportTypes={})", request.getOrganizationId(),
                 request.getYear(), request.getTransportTypes());
        
        var chart = new ChartDTO();
        chart.setType(ChartType.SLA.getName());
        
        List<MetricsPerPeriodDTO> dataPerPeriod = new ArrayList<>();
        for (var month = 1; month <= 12; month++) {
            dataPerPeriod.add(getMetricsPerPeriod(request, month, statsList));
        }
        List<MetricDTO> totals = getTotals(dataPerPeriod);
        
        List<MetricDTO> data = new ArrayList<>();
        var metricSlaFact = createMetric(
                MetricType.SLA_FACT,
                totals.stream()
                      .filter(metricDTO -> metricDTO.getCode().equals(MetricType.SLA_FACT.getCode()))
                      .mapToLong(MetricDTO::getValue).findFirst().orElse(0L));
        metricSlaFact.setName("Факт"); // Для круговой диаграммы наименование показателя отличается, но значение и код (смысл) показателя прежние
        data.add(metricSlaFact);
        data.add(createMetric(MetricType.SLA_NORM, 95L));
        
        chart.setDataPerPeriod(dataPerPeriod);
        chart.setTotals(totals);
        chart.setData(data);
        
        log.info("End: SlaAnalyticDataProviderImpl.getChartData(organizationId={}, year={}, transportTypes={})", request.getOrganizationId(),
                 request.getYear(), request.getTransportTypes());
        
        return chart;
    }
    
    private List<MetricDTO> getTotals(List<MetricsPerPeriodDTO> dataPerPeriod) {
        
        var metricWithoutViolation = createMetric(MetricType.SLA_WITHOUTVIOLATION, dataPerPeriod);
        var metricWithViolation = createMetric(MetricType.SLA_WITHVIOLATION, dataPerPeriod);
        var metricFact = createMetric(MetricType.SLA_FACT, calcFact(metricWithoutViolation.getValue(), metricWithViolation.getValue()));
        
        List<MetricDTO> metrics = new ArrayList<>();
        metrics.add(metricWithoutViolation);
        metrics.add(metricWithViolation);
        metrics.add(metricFact);
        
        return metrics;
    }
    
    private long calcFact(Long withoutViolation, Long withViolation) {
        long fact = 100L;
        long total = ((withoutViolation == null ? 0L : withoutViolation) + (withViolation == null ? 0L : withViolation));
        if (total != 0) {
            fact = ((withoutViolation == null ? 0L : withoutViolation) * 1000L / total + 5L) / 10L;
        }
        return fact;
    }
    
    private Long getValue(List<MetricsPerPeriodDTO> dataPerPeriod, MetricType metricType) {
        return dataPerPeriod.stream().map(MetricsPerPeriodDTO::getMetrics).flatMap(Collection::stream)
                            .filter(metric -> metric.getCode().equals(metricType.getCode())).mapToLong(MetricDTO::getValue).sum();
    }
    
    MetricsPerPeriodDTO getMetricsPerPeriod(GeneralAnalyticalReportRequestDTO request, int month, List<Stats> statsList) {
        var year = request.getYear();
        List<Stats> statsValues = new ArrayList<>();
        for (Stats stats : statsList) {
            if (month == stats.getMonth()) {
                statsValues.add(stats);
            }
        }
        
        long valueWithoutViolation = statsValues.stream().mapToLong(e -> (e.getSlaWithoutViolation())).sum();
        long valueWithViolation = statsValues.stream().mapToLong(e -> (e.getSlaWithViolation())).sum();
        long valueTotal = valueWithoutViolation + valueWithViolation;
        long valueFact = valueTotal == 0 ? 100L : ((valueWithoutViolation * 1000L / valueTotal + 5L) / 10L);
        
        var withoutViolation = new MetricDTO();
        withoutViolation.setCode(MetricType.SLA_WITHOUTVIOLATION.getCode());
        withoutViolation.setName(MetricType.SLA_WITHOUTVIOLATION.getName());
        withoutViolation.setValue(valueWithoutViolation);
        withoutViolation.setTypeValue(MetricType.SLA_WITHOUTVIOLATION.getTypeValue());
        
        var withViolation = new MetricDTO();
        withViolation.setCode(MetricType.SLA_WITHVIOLATION.getCode());
        withViolation.setName(MetricType.SLA_WITHVIOLATION.getName());
        withViolation.setValue(valueWithViolation);
        withViolation.setTypeValue(MetricType.SLA_WITHVIOLATION.getTypeValue());
        
        var fact = new MetricDTO();
        fact.setCode(MetricType.SLA_FACT.getCode());
        fact.setName(MetricType.SLA_FACT.getName());
        //fact.setValue(factMetricCalculatingByTransportType(request, dateFrom, dateTo, statusMap));
        fact.setValue(valueFact);
        fact.setTypeValue(MetricType.SLA_FACT.getTypeValue());
        
        var mpp = new MetricsPerPeriodDTO();
        mpp.setDateFrom(LocalDate.of(year, month, 1));
        mpp.setDateTo(mpp.getDateFrom().withDayOfMonth(mpp.getDateFrom().lengthOfMonth()));
        
        List<MetricDTO> metrics = new ArrayList<>();
        metrics.add(withoutViolation);
        metrics.add(withViolation);
        metrics.add(fact);
        mpp.setMetrics(metrics);
        
        return mpp;
    }
    
    private MetricDTO createMetric(MetricType metricType, List<MetricsPerPeriodDTO> dataPerPeriod) {
        MetricDTO metric = new MetricDTO();
        metric.setCode(metricType.getCode());
        metric.setName(metricType.getName());
        metric.setValue(getValue(dataPerPeriod, metricType));
        metric.setTypeValue(metricType.getTypeValue());
        
        return metric;
    }
    
    private MetricDTO createMetric(MetricType metricType, long value) {
        MetricDTO metric = new MetricDTO();
        metric.setCode(metricType.getCode());
        metric.setName(metricType.getName());
        metric.setValue(value);
        metric.setTypeValue(metricType.getTypeValue());
        
        return metric;
    }
}
