package ru.sber.transport;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.lang.NonNull;

@RequiredArgsConstructor
public class SimpleObjectProvider<T> implements ObjectProvider<T> {

    private final T object;

    @Override
    public @NonNull T getObject(@NonNull Object... args) throws BeansException {
        return object;
    }

    @Override
    public T getIfAvailable() throws BeansException {
        return object;
    }

    @Override
    public T getIfUnique() throws BeansException {
        return object;
    }

    @Override
    public @NonNull T getObject() throws BeansException {
        return object;
    }
}
