package ru.sberbank.ditsib.transport.reports.service.impl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Lazy;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.reports.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.reports.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.reports.dao.LimitRepository;
import ru.sberbank.ditsib.transport.reports.dao.RequestRepository;
import ru.sberbank.ditsib.transport.reports.service.EmployeeService;

import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EmbeddedPostgres
@Transactional
@DisplayName("Проверка общего аналитического отчета")
@Disabled
class AnalysisServiceImplTest {
    
    @Autowired
    private RequestRepository requestRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private LimitRepository limitRepository;
    
    @Autowired
    @Lazy
    private EmployeeService employeeService;
    /*
    @Test
    @DisplayName("В ответе присутствуют, по крайней мере, данные для графиков типа \"total\", \"SLA\", \"CSI\" и \"budget\"")
    void generalAnalyticalReportData_checkChartTypes() {
        
        final Integer year = 2021;
        
        RequestRepository requestRepository = mock(RequestRepository.class);
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
        LimitRepository limitRepository = mock(LimitRepository.class);
        EmployeeService employeeService = mock(EmployeeService.class);
        
        var cut = new AnalysisServiceImpl(requestRepository, departmentRepository, limitRepository, null, employeeService);
        
        GeneralAnalyticalReportRequestDTO analyticalReportRequest = new GeneralAnalyticalReportRequestDTO();
        analyticalReportRequest.setYear(year);
        List<String> transportTypes = Arrays.asList(TransportTypeEnum.TAXI.name(), TransportTypeEnum.PERSONAL.name());
        analyticalReportRequest.setTransportTypes(transportTypes);
        
        Object principal = null;
        Object credentials = null;
        Collection<? extends GrantedAuthority> authorities = null;
        boolean dataMaster = true;
        var authentication = new UserToken(principal, credentials, authorities, dataMaster);
        
        var response = cut.getGeneralAnalyticalReportData(analyticalReportRequest, authentication);
        var chartsByType = response.getCharts().stream().collect(Collectors.toMap(ChartDTO::getType, Function.identity()));
        
        // В ответе присутствуют, по крайней мере, данные для графиков типа "total", "SLA", "CSI" и "budget"
        assertTrue(Objects.nonNull(chartsByType.get(ChartType.TOTAL.getName())),
                   "В ответе должны присутствовать данные для графика \"" + ChartType.TOTAL.getName() + "\"");
        assertTrue(Objects.nonNull(chartsByType.get(ChartType.SLA.getName())),
                   "В ответе должны присутствовать данные для графика \"" + ChartType.SLA.getName() + "\"");
        assertTrue(Objects.nonNull(chartsByType.get(ChartType.CSI.getName())),
                   "В ответе должны присутствовать данные для графика \"" + ChartType.CSI.getName() + "\"");
        assertTrue(Objects.nonNull(chartsByType.get(ChartType.BUDGET.getName())),
                   "В ответе должны присутствовать данные для графика \"" + ChartType.BUDGET.getName() + "\"");
    }
    
    @Test
    @DisplayName("Данные графика типа \"total\" собираются согласно постановке")
    void generalAnalyticalReportData_checkTotal() {
        
        final int year = 2020;
        final UUID organizationId = UUID.randomUUID();
        Department department = Department.builder().id(UUID.randomUUID()).organizationId(organizationId).build();
        department = departmentRepository.save(department);
        
        Employee employee = Employee.builder().id(UUID.randomUUID()).department(department).build();
        employee = employeeRepository.save(employee);
        
        final UUID anotherOrganizationId = UUID.randomUUID();
        Department departmentFromAnotherOrganization = Department.builder().id(UUID.randomUUID()).organizationId(anotherOrganizationId).build();
        departmentFromAnotherOrganization = departmentRepository.save(departmentFromAnotherOrganization);
        
        Employee employeeFromAnotherOrganization = Employee.builder().id(UUID.randomUUID()).department(departmentFromAnotherOrganization).build();
        employeeFromAnotherOrganization = employeeRepository.save(employeeFromAnotherOrganization);
        
        LimitRepository limitRepository = mock(LimitRepository.class);
        EmployeeService employeeService = mock(EmployeeService.class);
        
        var cut = new AnalysisServiceImpl(requestRepository, departmentRepository, limitRepository, null, employeeService);
        
        List<Request> requests = new ArrayList<>();
        var december = LocalDateTime.of(year - 1, 1, 1, 0, 0, 0);
        var january = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        var february = LocalDateTime.of(year, 2, 1, 0, 0, 0);
        
        int expectedExecutedForTaxiJanuary = 3;
        int expectedExecutedForTaxiFebruary = 0;
        for (int i = 0; i < expectedExecutedForTaxiJanuary; i++) {
            requests.add(Request.builder()
                                .id(UUID.randomUUID())
                                .transportType("TAXI")
                                .passenger(employee)
                                .organizationId(organizationId)
                                .desiredDate(january)
                                .expected(ExpectedData.builder().cost(12017.00).build())
                                .status(TripRequestStatus.TAXI_TRIP_FINISHED.name())
                                .build());
        }
        requests.add(Request.builder().id(UUID.randomUUID()).transportType(TransportTypeEnum.PERSONAL.name()).passenger(employee)
                            .organizationId(organizationId).desiredDate(january)
                            .status(TripRequestStatus.PERSONAL_PAYMENT_DONE.name()).build());
        expectedExecutedForTaxiJanuary++;
        
        requests.add(Request.builder().id(UUID.randomUUID()).transportType(TransportTypeEnum.PUBLIC.name()).passenger(employee)
                            .organizationId(organizationId).desiredDate(january)
                            .status(TripRequestStatus.PUBLIC_PAYMENT_DONE.name()).build());
        requests.add(Request.builder().id(UUID.randomUUID()).transportType(TransportTypeEnum.TAXI.name()).passenger(employeeFromAnotherOrganization)
                            .organizationId(anotherOrganizationId)
                            .desiredDate(january)
                            .status(TripRequestStatus.TAXI_TRIP_FINISHED.name()).build());
        requests.add(Request.builder().id(UUID.randomUUID()).transportType(TransportTypeEnum.TAXI.name()).passenger(employee)
                            .organizationId(organizationId).desiredDate(february)
                            .expected(ExpectedData.builder().cost(12017.00).build())
                            .status(TripRequestStatus.TAXI_TRIP_FINISHED.name()).build());
        expectedExecutedForTaxiFebruary++;
        
        int expectedUnExecutedForTaxiJanuary = 0;
        int expectedUnExecutedForTaxiFebruary = 0;
        requests.add(Request.builder().id(UUID.randomUUID()).transportType(TransportTypeEnum.TAXI.name()).passenger(employee)
                            .organizationId(organizationId).desiredDate(january)
                            .status(TripRequestStatus.TAXI_APPROVED.name()).build());
        expectedUnExecutedForTaxiJanuary++;
        requests.add(Request.builder().id(UUID.randomUUID()).transportType(TransportTypeEnum.TAXI.name()).passenger(employee)
                            .organizationId(organizationId).desiredDate(january)
                            .status(TripRequestStatus.TAXI_AWAITING_APPROVAL.name()).build());
        expectedUnExecutedForTaxiJanuary++;
        requests.add(Request.builder().id(UUID.randomUUID()).transportType(TransportTypeEnum.TAXI.name()).passenger(employee)
                            .organizationId(organizationId).desiredDate(january)
                            .status(TripRequestStatus.TAXI_CANCELLED.name()).build());
        expectedUnExecutedForTaxiJanuary++;
        requests.add(Request.builder().id(UUID.randomUUID()).transportType(TransportTypeEnum.TAXI.name()).passenger(employee)
                            .organizationId(organizationId).desiredDate(january)
                            .status(TripRequestStatus.TAXI_DRIVER_ARRIVED.name()).build());
        expectedUnExecutedForTaxiJanuary++;
        requests.add(Request.builder().id(UUID.randomUUID()).transportType(TransportTypeEnum.TAXI.name()).passenger(employee)
                            .organizationId(organizationId).desiredDate(january)
                            .status(TripRequestStatus.TAXI_DRIVER_FOUND.name()).build());
        expectedUnExecutedForTaxiJanuary++;
        requests.add(Request.builder().id(UUID.randomUUID()).transportType(TransportTypeEnum.TAXI.name()).passenger(employee)
                            .organizationId(organizationId).desiredDate(january)
                            .status(TripRequestStatus.TAXI_DRIVER_ON_THE_WAY.name()).build());
        expectedUnExecutedForTaxiJanuary++;
        requests.add(Request.builder().id(UUID.randomUUID()).transportType(TransportTypeEnum.TAXI.name()).passenger(employee)
                            .organizationId(organizationId).desiredDate(january)
                            .status(TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name()).build());
        expectedUnExecutedForTaxiJanuary++;
        
        requests.add(Request.builder().id(UUID.randomUUID()).transportType(TransportTypeEnum.TAXI.name()).passenger(employeeFromAnotherOrganization)
                            .organizationId(anotherOrganizationId)
                            .desiredDate(january)
                            .status(TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name()).build());
        requests.add(Request.builder().id(UUID.randomUUID()).transportType(TransportTypeEnum.TAXI.name()).passenger(employee)
                            .organizationId(organizationId).desiredDate(december)
                            .status(TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name()).build());
        requests.add(Request.builder().id(UUID.randomUUID()).transportType(TransportTypeEnum.TAXI.name()).passenger(employee)
                            .organizationId(organizationId).desiredDate(february)
                            .status(TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name()).build());
        expectedUnExecutedForTaxiFebruary++;
        
        requestRepository.saveAll(requests);
        
        GeneralAnalyticalReportRequestDTO analyticalReportRequest = new GeneralAnalyticalReportRequestDTO();
        analyticalReportRequest.setOrganizationId(organizationId);
        analyticalReportRequest.setYear(year);
        List<String> transportTypes = Arrays.asList(TransportTypeEnum.TAXI.name(), TransportTypeEnum.PERSONAL.name());
        analyticalReportRequest.setTransportTypes(transportTypes);
        
        Object principal = null;
        Object credentials = null;
        Collection<? extends GrantedAuthority> authorities = null;
        boolean dataMaster = true;
        var authentication = new UserToken(principal, credentials, authorities, dataMaster);
        
        var response = cut.getGeneralAnalyticalReportData(analyticalReportRequest, authentication);
        
        var chartsByType = response.getCharts().stream().collect(Collectors.toMap(ChartDTO::getType, Function.identity()));
        
        var chart = chartsByType.get(ChartType.TOTAL.getName());
        
        var metricsPerPeriod = chart.getDataPerPeriod();
        
        var metricsJanuary =
                metricsPerPeriod.stream().filter(metricsPP -> metricsPP.getDateFrom().equals(LocalDate.of(year, 1, 1))).findFirst().get();
        
        // Данные метрики "Всего заявок" за январь
        var metricTotalJanuary =
                metricsJanuary.getMetrics().stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.TOTAL_TOTAL.getCode())).findFirst()
                              .get();
        
        // Данные метрики "Выполнено" за январь
        var metricExecutedJanuary =
                metricsJanuary.getMetrics().stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.TOTAL_EXECUTED.getCode())).findFirst()
                              .get();
        
        // Данные метрики "Не выполнено" за январь
        var metricUnExecutedJanuary =
                metricsJanuary.getMetrics().stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.TOTAL_UNEXECUTED.getCode()))
                              .findFirst().get();
        
        // Данные метрики "Сумма" за январь
        var metricSumJanuary =
                metricsJanuary.getMetrics().stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.TOTAL_SUM.getCode())).findFirst()
                              .get();
        
        
        var totals = chart.getTotals();
        
        // Данные метрики "Всего заявок" за все периоды (для данных легенды графика/диаграммы)
        var metricTotalTotals = totals.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.TOTAL_TOTAL.getCode())).findFirst().get();
        
        // Данные метрики "Выполнено" за все периоды (для данных легенды графика/диаграммы)
        var metricExecutedTotals =
                totals.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.TOTAL_EXECUTED.getCode())).findFirst().get();
        
        // Данные метрики "Не выполнено" за все периоды (для данных легенды графика/диаграммы)
        var metricUnExecutedTotals =
                totals.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.TOTAL_UNEXECUTED.getCode())).findFirst().get();
        
        // Данные метрики "Сумма" за все периоды (для данных легенды графика/диаграммы)
        var metricSumTotals = totals.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.TOTAL_SUM.getCode())).findFirst().get();
        
        var data = chart.getData();
        
        // Данные метрики "Выполнено" за все периоды (для данных легенды графика/диаграммы)
        var metricExecutedData =
                data.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.TOTAL_EXECUTED.getCode())).findFirst().get();
        
        // Данные метрики "Не выполнено" за все периоды (для данных легенды графика/диаграммы)
        var metricUnExecutedData =
                data.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.TOTAL_UNEXECUTED.getCode())).findFirst().get();
        
        assertFalse(metricsPerPeriod.stream().anyMatch(metricsPP -> metricsPP.getDateFrom().isAfter(metricsPP.getDateTo())),
                    "Дата начала каждого из периода должна быть не больше даты окончания периода");
        
        assertFalse(
                metricsPerPeriod.stream().anyMatch(metricsPP -> metricsPP.getDateFrom().getYear() != year || metricsPP.getDateTo().getYear() != year),
                "Все даты периодов должны быть из указанного в запросе года");
        
        assertNotNull(
                metricExecutedJanuary,
                "В данных по периодам должна присутствовать метрика \"Выполнено\" с кодом \"executed\"");
        
        assertEquals(MetricType.TOTAL_EXECUTED.getTypeValue(), metricExecutedJanuary.getTypeValue(),
                     "Единица измерения для метрики \"Выполнено\" должна " +
                     "быть \"" + MetricType.TOTAL_EXECUTED.getTypeValue() + "\"");
        
        assertEquals(
                expectedExecutedForTaxiJanuary, metricExecutedJanuary.getValue(),
                "Количество выполненных заявок на такси и ОТ должно быть равно количеству заявок с видом транспорта \"TAXI\" или \"PUBLIC\", со " +
                "статусом \"TAXI_TRIP_FINISHED\" или \"PUBLIC_PAYMENT_DONE\", с ожидаемой датой поездки в соответствующем периоде (январе), с " +
                "пассажирами из подразделений указанной организации \"organizationId\"");
        
        assertNotNull(
                metricUnExecutedJanuary,
                "В данных по периодам должна присутствовать метрика \"Не выполнено\" с кодом \"unExecuted\"");
        
        assertEquals(
                expectedUnExecutedForTaxiJanuary, metricUnExecutedJanuary.getValue(),
                "Количество не выполненных заявок на такси должно быть равно количеству заявок со статусом отличным от TAXI_TRIP_FINISHED и \"PUBLIC_PAYMENT_DONE\"за " +
                "соответствующий период (например, январь)");
        
        assertEquals(
                metricExecutedJanuary.getValue() + metricUnExecutedJanuary.getValue(), metricTotalJanuary.getValue(),
                "Должно быть \"Всего заявок\" = \"Выполнено\" + \"Не выполнено\" за соответствующий период (например, январь)");
        
        assertEquals(
                361L, metricSumJanuary.getValue(),
                "Сумма должна быть равна сумме всех заявок за соответствующий период (например, январь), итоговая сумма должна быть " +
                "пересчитана в рубли и округлена до целого (например, 12017+12017+12017 = 36051; 36051 / 100 = 360.51 -> 361");
        
        assertEquals(expectedExecutedForTaxiJanuary + expectedExecutedForTaxiFebruary + expectedUnExecutedForTaxiJanuary +
                     expectedUnExecutedForTaxiFebruary, metricTotalTotals.getValue(),
                     "Количество всех заявок на такси и ОТ должно быть равно количеству всех заявок с видом транспорта \"TAXI\" или " +
                     "\"PUBLIC\" за указанный год");
        assertEquals(expectedExecutedForTaxiJanuary + expectedExecutedForTaxiFebruary, metricExecutedTotals.getValue(),
                     "Количество выполненных заявок на такси и ОТ должно быть равно количеству заявок с видом транспорта \"TAXI\" или \"PUBLIC\", со " +
                     "статусом \"TAXI_TRIP_FINISHED\" или \"PUBLIC_PAYMENT_DONE\" с пассажирами из подразделений указанной организации \"organizationId\"");
        assertEquals(expectedUnExecutedForTaxiJanuary + expectedUnExecutedForTaxiFebruary, metricUnExecutedTotals.getValue(),
                     "Количество не выполненных заявок на такси и ОТ должно быть равно количеству заявок со статусом отличным от TAXI_TRIP_FINISHED" +
                     " и \"PUBLIC_PAYMENT_DONE\"");
        assertEquals(481L, metricSumTotals.getValue(),
                     "Сумма всех заявок на такси и ОТ должна быть равна сумме ожидаемой стоимости все заявок с видом транспорта \"TAXI\" или " +
                     "\"PUBLIC\" за указанный год");
        
        
        assertEquals(
                expectedExecutedForTaxiJanuary + expectedExecutedForTaxiFebruary, metricExecutedData.getValue(),
                "Количество выполненных заявок на такси и ОТ должно быть равно количеству заявок с видом транспорта \"TAXI\" или \"PUBLIC\", со " +
                "статусом \"TAXI_TRIP_FINISHED\" или \"PUBLIC_PAYMENT_DONE\" с пассажирами из подразделений указанной организации \"organizationId\"");
        
        assertEquals(
                expectedUnExecutedForTaxiJanuary + expectedUnExecutedForTaxiFebruary, metricUnExecutedData.getValue(),
                "Количество не выполненных заявок на такси и ОТ должно быть равно количеству заявок со статусом отличным от TAXI_TRIP_FINISHED и " +
                "\"PUBLIC_PAYMENT_DONE\"");
    }
    
    @Test
    @DisplayName("Данные графика типа \"CSI\" собираются согласно постановке")
    void generalAnalyticalReportData_checkCSI() {
        
        final int year = 2020;
        final UUID organizationId = UUID.randomUUID();
        Department department = Department.builder().id(UUID.randomUUID()).organizationId(organizationId).build();
        department = departmentRepository.save(department);
        
        LimitRepository limitRepository = mock(LimitRepository.class);
        EmployeeService employeeService = mock(EmployeeService.class);
        
        var cut = new AnalysisServiceImpl(requestRepository, departmentRepository, limitRepository, null, employeeService);
        
        Employee employee = Employee.builder().id(UUID.randomUUID()).department(department).build();
        employee = employeeRepository.save(employee);
        
        var january = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        var february = LocalDateTime.of(year, 2, 1, 0, 0, 0);
        List<Request> requests = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            RequestRating rating;
            rating = (i == 0 || i > 5) ? null : RequestRating.builder().rating(i).build();
            for (var j = i; j > 0; j--) {
                requests.add(Request.builder()
                                    .id(UUID.randomUUID())
                                    .transportType("TAXI")
                                    .passenger(employee)
                                    .organizationId(organizationId)
                                    .desiredDate(january)
                                    .requestRating(rating)
                                    .expected(ExpectedData.builder().cost(12017.00).build())
                                    .status(TripRequestStatus.TAXI_TRIP_FINISHED.name())
                                    .build());
                requests.add(Request.builder()
                                    .id(UUID.randomUUID())
                                    .transportType("TAXI")
                                    .passenger(employee)
                                    .organizationId(organizationId)
                                    .desiredDate(february)
                                    .requestRating(rating)
                                    .expected(ExpectedData.builder().cost(12017.00).build())
                                    .status(TripRequestStatus.TAXI_TRIP_FINISHED.name())
                                    .build());
            }
        }
        requestRepository.saveAll(requests);
        
        GeneralAnalyticalReportRequestDTO analyticalReportRequest = new GeneralAnalyticalReportRequestDTO();
        analyticalReportRequest.setOrganizationId(organizationId);
        analyticalReportRequest.setYear(year);
        List<String> transportTypes = Arrays.asList(TransportTypeEnum.TAXI.name(), TransportTypeEnum.PERSONAL.name());
        analyticalReportRequest.setTransportTypes(transportTypes);
        
        Object principal = null;
        Object credentials = null;
        Collection<? extends GrantedAuthority> authorities = null;
        boolean dataMaster = true;
        var authentication = new UserToken(principal, credentials, authorities, dataMaster);
        
        var response = cut.getGeneralAnalyticalReportData(analyticalReportRequest, authentication);
        
        var chartsByType = response.getCharts().stream().collect(Collectors.toMap(ChartDTO::getType, Function.identity()));
        var chart = chartsByType.get(ChartType.CSI.getName());
        
        var metricsPerPeriod = chart.getDataPerPeriod();
        
        var metricsJanuary =
                metricsPerPeriod.stream().filter(metricsPP -> metricsPP.getDateFrom().equals(LocalDate.of(year, 1, 1))).findFirst().get();
        
        // Данные метрики "5 звезд" за январь
        var metricStar5January =
                metricsJanuary.getMetrics().stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.CSI_STAR5.getCode())).findFirst()
                              .get();
        var metricStar4January =
                metricsJanuary.getMetrics().stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.CSI_STAR4.getCode())).findFirst()
                              .get();
        var metricStar3January =
                metricsJanuary.getMetrics().stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.CSI_STAR3.getCode())).findFirst()
                              .get();
        var metricStar2January =
                metricsJanuary.getMetrics().stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.CSI_STAR2.getCode())).findFirst()
                              .get();
        var metricStar1January =
                metricsJanuary.getMetrics().stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.CSI_STAR1.getCode())).findFirst()
                              .get();
        var metricCSIJanuary =
                metricsJanuary.getMetrics().stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.CSI_CSI.getCode())).findFirst()
                              .get();
        
        var totals = chart.getTotals();
        
        // Данные метрики "Оцененные заявки" за все периоды (для данных легенды графика/диаграммы)
        var metricTotalEvaluatedTotals =
                totals.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.CSI_TOTALEVALUATED.getCode())).findFirst().get();
        
        // Данные метрики "Положительные" за все периоды (для данных легенды графика/диаграммы)
        var metricStarPositiveTotals =
                totals.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.CSI_STARPOSITIVE.getCode())).findFirst().get();
        
        // Данные метрики "Отрицательные" за все периоды (для данных легенды графика/диаграммы)
        var metricStarNegativeTotals =
                totals.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.CSI_STARNEGATIVE.getCode())).findFirst().get();
        
        // Данные метрики "Удовлетворенные клиенты" за все периоды (для данных легенды графика/диаграммы)
        var metricCSITotals =
                totals.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.CSI_CSI.getCode())).findFirst().get();
        
        var data = chart.getData();
        
        // Данные метрики "Удовлетворенные клиенты" за все периоды (для данных легенды графика/диаграммы)
        var metricFactData =
                data.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.CSI_FACT.getCode())).findFirst().get();
        
        var metricNormData =
                data.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.CSI_NORM.getCode())).findFirst().get();
        
        assertEquals(5L, metricStar5January.getValue(), "Количество заявок за январь с оценкой 5 звезд должно быть равно 5");
        assertEquals(4L, metricStar4January.getValue(), "Количество заявок за январь с оценкой 4 звезд должно быть равно 4");
        assertEquals(3L, metricStar3January.getValue(), "Количество заявок за январь с оценкой 3 звезд должно быть равно 3");
        assertEquals(2L, metricStar2January.getValue(), "Количество заявок за январь с оценкой 2 звезд должно быть равно 2");
        assertEquals(1L, metricStar1January.getValue(), "Количество заявок за январь с оценкой 1 звезд должно быть равно 1");
        assertEquals(60L, metricCSIJanuary.getValue(), "CSI за январь должен быть равен 60%: 9 положительных оценки, всего 15 оценок");
        
        assertEquals(18L, metricStarPositiveTotals.getValue(),
                     "Количество заявок с положительной оценкой должно быть равно количеству заявок с оценкой 4 или 5 за все периоды года");
        assertEquals(12L, metricStarNegativeTotals.getValue(),
                     "Количество заявок с негативной оценкой должно быть равно количеству заявок с оценкой 1, 2 или 3 за все периоды года ");
        assertEquals(30L, metricTotalEvaluatedTotals.getValue());
        assertEquals(60L, metricCSITotals.getValue(), "CSI за все периоды года должен быть равен 60%: 18 положительных оценки, всего 30 оценок");
        
        assertEquals(60L, metricFactData.getValue(), "Факт CSI за год должен быть равен 60%: 18 положительных оценки, всего 30 оценок");
        assertEquals(95L, metricNormData.getValue(), "Норма должна быть 95%");
    }
    
    @Test
    @DisplayName("Данные графика типа \"budget\" собираются согласно постановке")
    void generalAnalyticalReportData_checkBudget() {
        
        final int year = 2020;
        final UUID organizationId = UUID.randomUUID();
        
        Department department = Department.builder().id(UUID.randomUUID()).organizationId(organizationId).build();
        department = departmentRepository.save(department);
        
        Employee employee = Employee.builder().id(UUID.randomUUID()).department(department).build();
        employee = employeeRepository.save(employee);
        
        final UUID anotherOrganizationId = UUID.randomUUID();
        Department departmentFromAnotherOrganization = Department.builder().id(UUID.randomUUID()).organizationId(anotherOrganizationId).build();
        departmentFromAnotherOrganization = departmentRepository.save(departmentFromAnotherOrganization);
        
        Employee employeeFromAnotherOrganization = Employee.builder().id(UUID.randomUUID()).department(departmentFromAnotherOrganization).build();
        employeeFromAnotherOrganization = employeeRepository.save(employeeFromAnotherOrganization);
        
        List<LimitType> limitTypes = new ArrayList<>(Arrays.asList(LimitType.values()));
        List<LimitStatus> limitStatuses = new ArrayList<>(Arrays.asList(LimitStatus.values()));
        List<TransportTypeEnum> transportTypes = new ArrayList<>(Arrays.asList(TransportTypeEnum.values()));
        transportTypes.add(null);
        List<LimitSharingType> limitSharingTypes = new ArrayList<>(Arrays.asList(LimitSharingType.values()));
        List<LimitServiceType> limitServiceTypes = new ArrayList<>(Arrays.asList(LimitServiceType.values()));
        limitServiceTypes.add(null);
        List<Integer> years = new ArrayList<>(Arrays.asList(year - 1, year, year + 1));
        
        
        long expectedTotalBudget = 0L;
        List<Limit> limits = new ArrayList<>();
        for (Integer currentYear : years) {
            for (LimitSharingType limitSharingType : limitSharingTypes) {
                for (TransportTypeEnum transportType : transportTypes) {
                    for (LimitStatus limitStatus : limitStatuses) {
                        for (LimitType limitType : limitTypes) {
                            for (LimitServiceType limitServiceType : limitServiceTypes) {
                                if (transportType == null && limitServiceType != null || transportType != null && limitServiceType == null) {
                                    long sum = 10000L;
                                    limits.add(Limit.builder()
                                                    .id(UUID.randomUUID())
                                                    .organizationId(organizationId)
                                                    .limitType(limitType)
                                                    .limitStatus(limitStatus)
                                                    .limitSharingType(limitSharingType)
                                                    .year(currentYear)
                                                    .transportType(transportType)
                                                    .limitServiceType(limitServiceType)
                                                    .sum(sum)
                                                    .build());
                                    
                                    if ((TransportTypeEnum.TAXI.equals(transportType) || TransportTypeEnum.PERSONAL.equals(transportType)) &&
                                        currentYear.equals(2020)) {
                                        expectedTotalBudget = expectedTotalBudget + sum;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        expectedTotalBudget = expectedTotalBudget / 100;
        
        limitRepository.saveAll(limits);
        
        List<Request> requests = new ArrayList<>();
        
        double expectedSpent = 0.00;
        double cost = 12017.00;
        
        transportTypes = new ArrayList<>(Arrays.asList(TransportTypeEnum.values()));
        
        List<TripRequestStatus> tripRequestStatuses = new ArrayList<>();
        tripRequestStatuses.addAll(TripRequestStatus.TAXI_STATUSES);
        tripRequestStatuses.addAll(TripRequestStatus.PERSONAL_STATUSES);
        tripRequestStatuses.addAll(TripRequestStatus.PUBLIC_STATUSES);
        tripRequestStatuses.addAll(TripRequestStatus.CARSHARING_STATUSES);
        
        Set<TripRequestStatus> cancelStatuses = new HashSet<>();
        cancelStatuses.add(TripRequestStatus.TAXI_CANCELLED);
        cancelStatuses.add(TripRequestStatus.PERSONAL_CANCELLED);
        cancelStatuses.add(TripRequestStatus.PUBLIC_CANCELLED);
        cancelStatuses.add(TripRequestStatus.CARSHARING_CANCELLED);
        
        long countSpent = 0;
        
        for (Integer currentYear : years) {
            var desiredDate = LocalDateTime.of(currentYear, 1, 1, 1, 1, 1);
            for (TransportTypeEnum transportType : transportTypes) {
                for (TripRequestStatus tripRequestStatus : tripRequestStatuses) {
                    requests.add(Request.builder()
                                        .id(UUID.randomUUID())
                                        .transportType(transportType.name())
                                        .passenger(employee)
                                        .organizationId(organizationId)
                                        .desiredDate(desiredDate)
                                        .expected(ExpectedData.builder().cost(cost).build())
                                        .status(tripRequestStatus.name())
                                        .build());
                    if (!(cancelStatuses.contains(tripRequestStatus)) &&
                        currentYear == 2020 && (TransportTypeEnum.TAXI.equals(transportType) || TransportTypeEnum.PERSONAL.equals(transportType))) {
                        expectedSpent += cost; // Подсчет ожидаемой суммы расходов по Такси и ЛТ за 2020-й год - берем все заявки кроме отмененных
                    }
                    
                    requests.add(Request.builder()
                                        .id(UUID.randomUUID())
                                        .transportType(transportType.name())
                                        .passenger(employeeFromAnotherOrganization)
                                        .desiredDate(desiredDate)
                                        .expected(ExpectedData.builder().cost(cost).build())
                                        .status(tripRequestStatus.name())
                                        .build());
                }
            }
        }
        
        requestRepository.saveAll(requests);
        System.out.println("countSpent: " + countSpent);
        System.out.println("requests.size(): " + requests.size());
        
        EmployeeService employeeService = mock(EmployeeService.class);
        
        var cut = new AnalysisServiceImpl(requestRepository, departmentRepository, limitRepository, null, employeeService);
        
        GeneralAnalyticalReportRequestDTO analyticalReportRequest = new GeneralAnalyticalReportRequestDTO();
        analyticalReportRequest.setOrganizationId(organizationId);
        analyticalReportRequest.setYear(year);
        analyticalReportRequest.setTransportTypes(Arrays.asList(TransportTypeEnum.TAXI.name(), TransportTypeEnum.PERSONAL.name()));
        
        Object principal = null;
        Object credentials = null;
        Collection<? extends GrantedAuthority> authorities = null;
        boolean dataMaster = true;
        var authentication = new UserToken(principal, credentials, authorities, dataMaster);
        
        var response = cut.getGeneralAnalyticalReportData(analyticalReportRequest, authentication);
        
        var chartsByType = response.getCharts().stream().collect(Collectors.toMap(ChartDTO::getType, Function.identity()));
        var chart = chartsByType.get(ChartType.BUDGET.getName());
        
        var data = chart.getData();
        
        // Данные метрики "Общий лимит"
        var metricTotalBudget =
                data.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.BUDGET_TOTALBUDGET.getCode())).findFirst().get();
        var metricSpent =
                data.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.BUDGET_SPENT.getCode())).findFirst().get();
        
        assertNull(chart.getDataPerPeriod(), "Для графика \"budget\" не должны передаваться метрики по периодам");
        
        assertNull(chart.getTotals(), "Для графика \"budget\" не должны передаваться данные для легенды графика по периодам");
        
        assertEquals(expectedTotalBudget, metricTotalBudget.getValue());
        
        assertEquals(Math.round(expectedSpent / 100), metricSpent.getValue());
    }
    
    @Test
    @DisplayName("Данные графика типа \"SLA\" собираются согласно постановке")
    void generalAnalyticalReportData_checkSLA() {
        final int year = 2020;
        final UUID organizationId = UUID.randomUUID();
        Department department = Department.builder().id(UUID.randomUUID()).organizationId(organizationId).build();
        department = departmentRepository.save(department);
        
        Employee employee = Employee.builder().id(UUID.randomUUID()).department(department).build();
        employee = employeeRepository.save(employee);
        
        final UUID anotherOrganizationId = UUID.randomUUID();
        Department departmentFromAnotherOrganization = Department.builder().id(UUID.randomUUID()).organizationId(anotherOrganizationId).build();
        departmentFromAnotherOrganization = departmentRepository.save(departmentFromAnotherOrganization);
        
        Employee employeeFromAnotherOrganization = Employee.builder().id(UUID.randomUUID()).department(departmentFromAnotherOrganization).build();
        employeeFromAnotherOrganization = employeeRepository.save(employeeFromAnotherOrganization);
        
        LimitRepository limitRepository = mock(LimitRepository.class);
        
        List<Request> requests = new ArrayList<>();
        
        List<Integer> years = new ArrayList<>(Arrays.asList(year - 1, year, year + 1));
        for (Integer currentYear : years) {
            for (Integer month = 1; month <= 12; month++) {
                LocalDateTime desiredDate = LocalDateTime.of(currentYear, month, 1, 0, 0, 0);
                for (Integer i = month; i > 0; i--) {
                    requests.add(Request.builder()
                                        .id(UUID.randomUUID())
                                        .transportType(TransportTypeEnum.TAXI.name())
                                        .passenger(employee)
                                        .organizationId(organizationId)
                                        .desiredDate(desiredDate)
                                        .expected(ExpectedData.builder().cost(12017.00).build())
                                        .status(TripRequestStatus.TAXI_TRIP_FINISHED.name())
                                        .build());
                    requests.add(Request.builder()
                                        .id(UUID.randomUUID())
                                        .transportType(TransportTypeEnum.TAXI.name())
                                        .passenger(employee)
                                        .organizationId(organizationId)
                                        .desiredDate(desiredDate)
                                        .expected(ExpectedData.builder().cost(12017.00).build())
                                        .status(TripRequestStatus.TAXI_TRIP_FINISHED.name())
                                        .build());
                    requests.add(Request.builder()
                                        .id(UUID.randomUUID())
                                        .transportType(TransportTypeEnum.PERSONAL.name())
                                        .passenger(employee)
                                        .organizationId(organizationId)
                                        .desiredDate(desiredDate)
                                        .expected(ExpectedData.builder().cost(12017.00).build())
                                        .status(TripRequestStatus.PERSONAL_PAYMENT_DONE.name())
                                        .build());
                    
                    requests.add(Request.builder()
                                        .id(UUID.randomUUID())
                                        .transportType(TransportTypeEnum.TAXI.name())
                                        .passenger(employee)
                                        .organizationId(organizationId)
                                        .desiredDate(desiredDate)
                                        .deadlineState(DeadlineState.RED)
                                        .expected(ExpectedData.builder().cost(12017.00).build())
                                        .status(TripRequestStatus.TAXI_TRIP_FINISHED.name())
                                        .build());
                    requests.add(Request.builder()
                                        .id(UUID.randomUUID())
                                        .transportType(TransportTypeEnum.TAXI.name())
                                        .passenger(employee)
                                        .organizationId(organizationId)
                                        .desiredDate(desiredDate)
                                        .expected(ExpectedData.builder().cost(12017.00).build())
                                        .status(TripRequestStatus.TAXI_CANCELLED.name())
                                        .statusCode(TripRequestStatus.TaxiStatusCode.TAXI_CANCELLED_BY_DRIVER.getCode())
                                        .build());
                    requests.add(Request.builder()
                                        .id(UUID.randomUUID())
                                        .transportType(TransportTypeEnum.PUBLIC.name())
                                        .slaExpired(true)
                                        .passenger(employee)
                                        .organizationId(organizationId)
                                        .desiredDate(desiredDate)
                                        .expected(ExpectedData.builder().cost(12017.00).build())
                                        .status(TripRequestStatus.PUBLIC_PAYMENT_DONE.name())
                                        .build());
                    requests.add(Request.builder()
                                        .id(UUID.randomUUID())
                                        .transportType(TransportTypeEnum.PERSONAL.name())
                                        .slaExpired(true)
                                        .passenger(employee)
                                        .organizationId(organizationId)
                                        .desiredDate(desiredDate)
                                        .expected(ExpectedData.builder().cost(12017.00).build())
                                        .status(TripRequestStatus.PERSONAL_PAYMENT_DONE.name())
                                        .build());
                    
                    requests.add(Request.builder()
                                        .id(UUID.randomUUID())
                                        .transportType(TransportTypeEnum.TAXI.name())
                                        .passenger(employee)
                                        .organizationId(organizationId)
                                        .desiredDate(desiredDate)
                                        .deadlineState(DeadlineState.RED)
                                        .expected(ExpectedData.builder().cost(12017.00).build())
                                        .status(TripRequestStatus.TAXI_CANCELLED.name())
                                        .statusCode(TripRequestStatus.TaxiStatusCode.TAXI_DECLINED.getCode())
                                        .build());
                    requests.add(Request.builder()
                                        .id(UUID.randomUUID())
                                        .transportType(TransportTypeEnum.TAXI.name())
                                        .passenger(employee)
                                        .organizationId(anotherOrganizationId)
                                        .desiredDate(desiredDate)
                                        .expected(ExpectedData.builder().cost(12017.00).build())
                                        .status(TripRequestStatus.TAXI_TRIP_FINISHED.name())
                                        .build());
                }
            }
        }
        
        requestRepository.saveAll(requests);
        
        var cut = new AnalysisServiceImpl(requestRepository, departmentRepository, limitRepository, null, employeeService);
        
        GeneralAnalyticalReportRequestDTO analyticalReportRequest = new GeneralAnalyticalReportRequestDTO();
        analyticalReportRequest.setOrganizationId(organizationId);
        analyticalReportRequest.setYear(year);
        analyticalReportRequest.setTransportTypes(Arrays.asList(TransportTypeEnum.TAXI.name(), TransportTypeEnum.PERSONAL.name(),
                                                                TransportTypeEnum.PUBLIC.name()));
        
        Object principal = null;
        Object credentials = null;
        Collection<? extends GrantedAuthority> authorities = null;
        boolean dataMaster = true;
        var authentication = new UserToken(principal, credentials, authorities, dataMaster);
        
        var response = cut.getGeneralAnalyticalReportData(analyticalReportRequest, authentication);
        
        var chartsByType = response.getCharts().stream().collect(Collectors.toMap(ChartDTO::getType, Function.identity()));
        var chart = chartsByType.get(ChartType.SLA.getName());
        
        var metricsPerPeriod = chart.getDataPerPeriod();
        var metricsFebruary =
                metricsPerPeriod.stream().filter(metricsPP -> metricsPP.getDateFrom().equals(LocalDate.of(year, 2, 1))).findFirst().get();
        var metricSlaWithoutViolationFebruary =
                metricsFebruary.getMetrics().stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.SLA_WITHOUTVIOLATION.getCode()))
                               .findFirst()
                               .get();
        var metricSlaWithViolationFebruary =
                metricsFebruary.getMetrics().stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.SLA_WITHVIOLATION.getCode()))
                               .findFirst()
                               .get();
        var metricSlaFactFebruary =
                metricsFebruary.getMetrics().stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.SLA_FACT.getCode())).findFirst()
                               .get();
        
        var totals = chart.getTotals();
        var metricTotalsWithoutViolation =
                totals.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.SLA_WITHOUTVIOLATION.getCode())).findFirst().get();
        var metricTotalsWithViolation =
                totals.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.SLA_WITHVIOLATION.getCode())).findFirst().get();
        var metricTotalsFact =
                totals.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.SLA_FACT.getCode())).findFirst().get();
        
        var data = chart.getData();
        
        var metricSlaFact =
                data.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.SLA_FACT.getCode())).findFirst().get();
        var metricSlaNorm =
                data.stream().filter(metricDTO -> metricDTO.getCode().equals(MetricType.SLA_NORM.getCode())).findFirst().get();
        
        assertNotNull(metricSlaFact, "В ответе в атрибуте data должна быть информация по метрике 'fact'");
        assertNotNull(metricSlaNorm, "В ответе в атрибуте data должна быть информация по метрике 'norm'");
        
        // Проверка расчетов по периоду
        assertEquals(14L, metricSlaWithoutViolationFebruary.getValue() + metricSlaWithViolationFebruary.getValue(),
                     "При расчете должны учитываться заявки по указанному в запросе organizationId, видам транспорта transportTypes и desiredDate " +
                     "которых относится к соответствующему периоду (например, январь) и имеющих положительный конечный статус (TAXI_TRIP_FINISHED / " +
                     "PERSONAL_PAYMENT_DONE / CARSHARING_TRIP_FINISHED / TAXI_TRIP_FINISHED) или статус TAXI_CANCELLED с кодом отмены " +
                     "TaxiStatusCode.TAXI_CANCELLED_BY_DRIVER - 'Отменено водителем'");
        assertEquals(8L, metricSlaWithViolationFebruary.getValue(),
                     "Количество заявок выполненных с нарушением считается по значению deadlineState = DeadlineState.RED или значению статуса = TAXI_CANCELLED с кодом отмены TaxiStatusCode.TAXI_CANCELLED_BY_DRIVER - 'Отменено водителем'");
        assertEquals(43L, metricSlaFactFebruary.getValue());
        
        // Проверка расчетов для легенды
        assertEquals(234L, metricTotalsWithoutViolation.getValue());
        assertEquals(312L, metricTotalsWithViolation.getValue());
        assertEquals(43L, metricTotalsFact.getValue());
        
        // Проверка расчетов для круговой итоговой диаграммы
        assertEquals(43L, metricSlaFact.getValue());
        assertEquals(95L, metricSlaNorm.getValue());
    }
    
    @Test
    @DisplayName("Проверка доступа к данным организации в зависимсоти от признака data_master")
    void generalAnalyticalReportData_checkRole() {
        final int year = 2020;
        final UUID organizationId = UUID.randomUUID();
        var cut = new AnalysisServiceImpl(requestRepository, departmentRepository, limitRepository, null, employeeService);
        
        GeneralAnalyticalReportRequestDTO analyticalReportRequest = new GeneralAnalyticalReportRequestDTO();
        analyticalReportRequest.setOrganizationId(organizationId);
        analyticalReportRequest.setYear(year);
        analyticalReportRequest.setTransportTypes(Arrays.asList(TransportTypeEnum.TAXI.name(), TransportTypeEnum.PERSONAL.name()));
        
        var notDataMasterAuth = new UserToken(null, null, null, false);
        ru.sber.transport.roles.check.exceptions.ForbiddenException thrown =
                Assertions.assertThrows(ru.sber.transport.roles.check.exceptions.ForbiddenException.class, () -> {
                    var response = cut.getGeneralAnalyticalReportData(analyticalReportRequest, notDataMasterAuth);
                }, "Если у ролей пользователя нет установленного признака data_master и запрос не по организации пользователя, то должно быть " +
                   "вызвано исключение ForbiddenException");
        
        var dataMasterAuth = new UserToken(null, null, null, true);
        Assertions.assertDoesNotThrow(() -> {
            var response = cut.getGeneralAnalyticalReportData(analyticalReportRequest, dataMasterAuth);
        }, "Если у роли пользователя установлен признака data_master и запрос не по организации пользователя, то не должно быть " +
           "вызвано исключение ForbiddenException");
    }*/
}