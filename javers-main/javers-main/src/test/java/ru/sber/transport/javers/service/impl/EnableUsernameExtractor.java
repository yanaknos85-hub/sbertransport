package ru.sber.transport.javers.service.impl;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(UsernameAuthenticationExtractor.class)
public @interface EnableUsernameExtractor {

}