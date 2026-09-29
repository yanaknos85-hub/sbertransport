package ru.sber.transport.request.external.providers.employee;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.transport.business.providers.DepartmentsProvider;

@RequiredArgsConstructor
public class SimpleObjectProvider implements ObjectProvider<DepartmentsProvider> {

    private final DepartmentsProvider value;

    @NotNull
    @Override
    public DepartmentsProvider getObject() {
        return value;
    }

}
