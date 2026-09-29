package ru.sber.transport.telemechanic.converter;

import org.jetbrains.annotations.NotNull;
import org.jooq.Converter;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;

public class StringToEwbStatusConverter implements Converter<String, EwbStatus> {
    
    @Override
    public EwbStatus from(String s) {
        return EwbStatus.valueOf(s);
    }
    
    @Override
    public String to(EwbStatus status) {
        return status.name();
    }
    
    @Override
    public @NotNull Class<String> fromType() {
        return String.class;
    }
    
    @Override
    public @NotNull Class<EwbStatus> toType() {
        return EwbStatus.class;
    }
}
