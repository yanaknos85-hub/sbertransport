package ru.sber.transport.driver_track.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sber.transport.driver_track.service.ExpectedRouteService;
import ru.sber.transport.driver_track.service.RouteService;

/**
 * Шедулер для генерации планового маршрута
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpectedRouteScheduler {

    private final ExpectedRouteService expectedRouteService;
    private final RouteService routeService;

    @Value("${route.scheduler.isEnabled:true}")
    private boolean isEnabled;

    /**
     * Метод вызывается каждую минуту для построения маршрутов
     */
    @Scheduled(cron = "${route.scheduler.rate:0 */1 * * * *}")
    public void createExpectedRouteTrip() {
        if(isEnabled) {
            log.trace("Route scheduler started");
            expectedRouteService.createExpectedRoute();
            routeService.createFactRoute();
        }
    }
}
