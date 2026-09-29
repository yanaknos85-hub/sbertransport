package ru.sber.transport.deadline.grpc.client.impl;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DeadlineCalculationClientImpl.class)
public class DeadlineCalculationClientConfiguration {
}
