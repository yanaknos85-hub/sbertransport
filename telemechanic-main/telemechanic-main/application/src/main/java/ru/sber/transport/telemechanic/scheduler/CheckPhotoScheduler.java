package ru.sber.transport.telemechanic.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sber.transport.telemechanic.service.CheckPhotoService;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "check-photo.auto-deletion", name = "enabled", havingValue = "true")
public class CheckPhotoScheduler {
    
    private final CheckPhotoService checkPhotoService;
    
    @Scheduled(cron = "${check-photo.auto-deletion.cron}")
    @SchedulerLock(name = "deleteRequestsOldPhotos",
                   lockAtLeastFor = "${check-photo.auto-deletion.lock-at-least-for}",
                   lockAtMostFor = "${check-photo.auto-deletion.lock-at-most-for}")
    public void deleteOldPhotos() {
        checkPhotoService.deleteOutdatedPhotos();
    }
    
}
