package ru.sberbank.ditsib.transport.srm.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.srm.dao.SrmSharedRideRepository;

import java.time.ZonedDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class FinishSharedRideScheduler {

    private final SrmSharedRideRepository sharedRideRepository;
    
    @Scheduled(cron = "${sharedride.deactivate.cron:0 0 * * * *}")
    public void finishRidesScheduler() {
        int deactivated = 0;
        final var now = ZonedDateTime.now();
        var currentPage = 0;
        var pages = 0;
        do {
            final var sharedRidesPage = sharedRideRepository.findByActive(true, PageRequest.of(currentPage, 500));
            pages = sharedRidesPage.getTotalPages();
            final var sharedRides = sharedRidesPage.getContent();
            log.info("SRM: finishRidesScheduler: found active shared rides {}", sharedRides.size());
            for (final var sharedRide : sharedRides) {
                if (sharedRide.getWaypoints().isEmpty() || now.isAfter(sharedRide.getWaypoints().get(0).getStartTime())) {
                    sharedRide.setActive(false);
                    log.debug("SRM: finishRidesScheduler: shared ride deactivated {}", sharedRide.getId());
                    deactivated++;
                }
            }
            sharedRideRepository.saveAll(sharedRides);
        } while (++currentPage < pages);
        log.info("SRM: finishRidesScheduler: total deactivated {}", deactivated);
    }
}
