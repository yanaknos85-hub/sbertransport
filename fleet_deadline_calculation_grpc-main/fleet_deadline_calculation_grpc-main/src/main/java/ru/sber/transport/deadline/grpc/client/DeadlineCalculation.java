package ru.sber.transport.deadline.grpc.client;

import org.springframework.context.annotation.Import;
import ru.sber.transport.deadline.grpc.client.impl.DeadlineCalculationClientConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ DeadlineCalculationClientConfiguration.class})
public @interface DeadlineCalculation {}
