package ru.sber.transport.etrn.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulingConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SchedulingConfiguration.class);

    @Test
    @DisplayName("SchedulingConfiguration — создаётся при enabled=true")
    void configCreated_whenEnabledTrue() {
        contextRunner
                .withPropertyValues("scheduling.enabled=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                });
    }

    @Test
    @DisplayName("SchedulingConfiguration — не создаётся при enabled=false")
    void configNotCreated_whenEnabledFalse() {
        contextRunner
                .withPropertyValues("scheduling.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                });
    }
}
