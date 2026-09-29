package ru.sber.transport.dispatcher.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.database.dao.ContractorRepository;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.service.ContractorCounter;

import java.time.Duration;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@DisplayName("Проверка счетчика")
class ContractorCounterImplTest {

    private final ContractorRepository contractorRepository = mock(ContractorRepository.class);

    private final ContractorCounter contractorCounter = new ContractorCounterImpl(contractorRepository);

    @DisplayName("Счетчик. Инкремент")
    @Test
    void test_count_increment() {
        var contractor = Instancio.of(Contractor.class)
                .ignore(Select.field(Contractor::getId))
                .ignore(Select.field(Contractor::getMainDispatcher))
                .ignore(Select.field(Contractor::getEmployeeCount))
                .set(Select.field(Contractor::isActive), true)
                .create();
        when(contractorRepository.getReferenceById(contractor.getId())).thenReturn(contractor);
        when(contractorRepository.saveAndFlush(any(Contractor.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = contractorCounter.changeCount(contractor, 1);

        assertThat(result.getEmployeeCount())
                .isEqualTo(contractor.getEmployeeCount())
                .isEqualTo(1);
    }

    @DisplayName("Счетчик. Декремент")
    @Test
    void test_count_decrement() {
        var contractor = Instancio.of(Contractor.class)
                .ignore(Select.field(Contractor::getId))
                .ignore(Select.field(Contractor::getMainDispatcher))
                .set(Select.field(Contractor::getEmployeeCount), 2)
                .set(Select.field(Contractor::isActive), true)
                .create();

        when(contractorRepository.getReferenceById(contractor.getId())).thenReturn(contractor);
        when(contractorRepository.saveAndFlush(any(Contractor.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = contractorCounter.changeCount(contractor, -1);

        assertThat(result.getEmployeeCount())
                .isEqualTo(contractor.getEmployeeCount())
                .isEqualTo(1);
    }

    @DisplayName("Счетчик. Конкурент")
    @Test
    void test_concurrent() {
        var contractor = Instancio.of(Contractor.class)
                .ignore(Select.field(Contractor::getId))
                .ignore(Select.field(Contractor::getMainDispatcher))
                .ignore(Select.field(Contractor::getMainDispatcher))
                .ignore(Select.field(Contractor::getEmployeeCount))
                .set(Select.field(Contractor::isActive), true)
                .create();
        when(contractorRepository.getReferenceById(contractor.getId())).thenReturn(contractor);
        when(contractorRepository.saveAndFlush(any(Contractor.class))).thenAnswer(inv -> inv.getArgument(0));

        var executors = Executors.newFixedThreadPool(100);
        var futures = new LinkedList<Future<?>>();

        for (var i = 0; i < 100; i++) {
            int finalI = i;
            futures.add(executors.submit(() -> contractorCounter.changeCount(contractor, finalI)));
            futures.add(executors.submit(() -> contractorCounter.changeCount(contractor, -1 * finalI)));
        }
        futures.add(executors.submit(() -> contractorCounter.changeCount(contractor, 100)));

        await()
                .timeout(Duration.ofSeconds(30))
                .pollDelay(Duration.ofSeconds(1))
                .until(() -> futures.stream().allMatch(Future::isDone));

        assertThat(contractor.getEmployeeCount()).isEqualTo(100);
    }

}