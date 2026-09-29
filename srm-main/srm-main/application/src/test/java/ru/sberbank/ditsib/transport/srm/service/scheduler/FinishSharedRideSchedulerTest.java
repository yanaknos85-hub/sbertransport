package ru.sberbank.ditsib.transport.srm.service.scheduler;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.srm.dao.SrmSharedRideRepository;
import ru.sberbank.ditsib.transport.srm.model.SrmSharedRide;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@Isolated
@Feature("app_passenger_srm")
@DisplayName("Проверка работы планировщика завершения поездки")
class FinishSharedRideSchedulerTest {

    private final SrmSharedRideRepository repository = mock(SrmSharedRideRepository.class);

    private final FinishSharedRideScheduler scheduler = new FinishSharedRideScheduler(repository);

    @Test
    @DisplayName("Проверка механизма")
    void test_schedule() {
        final var data = Instancio.ofList(SrmSharedRide.class)
                .set(Select.field(SrmSharedRide::getWaypoints), List.of())
                .create();

        when(repository.findByActive(true, PageRequest.of(0, 500))).thenReturn(new PageImpl<>(data));

        scheduler.finishRidesScheduler();

        final var actualCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(actualCaptor.capture());

        final var actualList = actualCaptor.getValue();
        assertThat(actualList).hasSameSizeAs(data);
        assertThat(actualList.stream().map(it -> ((SrmSharedRide) it).isActive()).noneMatch(it -> (boolean) it)).isTrue();
    }

}