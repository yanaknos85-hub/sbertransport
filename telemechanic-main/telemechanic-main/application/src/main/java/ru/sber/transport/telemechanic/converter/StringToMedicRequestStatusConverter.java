package ru.sber.transport.telemechanic.converter;

import org.jetbrains.annotations.NotNull;
import org.jooq.Converter;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

public class StringToMedicRequestStatusConverter implements Converter<String, TelemedicineStatus> {
    
    @Override
    public TelemedicineStatus from(String source) {
        return TelemedicineStatus.valueOf(source);
    }
    
    @Override
    public String to(TelemedicineStatus source) {
        return source.name();
    }
    
    @Override
    public @NotNull Class<String> fromType() {
        return String.class;
    }
    
    @Override
    public @NotNull Class<TelemedicineStatus> toType() {
        return TelemedicineStatus.class;
    }
}
