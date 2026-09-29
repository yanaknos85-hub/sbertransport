package ru.sber.transport;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;

@RequiredArgsConstructor
public class SimpleObjectProvider<T> implements ObjectProvider<T> {

    private final T value;

    @NotNull
    @Override
    public T getObject() throws BeansException {
        return value;
    }
}
