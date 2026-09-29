package ru.sber.transport.telemechanic.converter;

import org.jetbrains.annotations.NotNull;
import org.jooq.Converter;

import java.sql.Date;
import java.time.LocalDate;

public class DateToLocalDateConverter implements Converter<Date, LocalDate> {
    
    @Override
    public LocalDate from(Date date) {
        return date != null ? date.toLocalDate() : null;
    }
    
    @Override
    public Date to(LocalDate localDate) {
        return localDate != null ? Date.valueOf(localDate) : null;
    }
    
    @Override
    public @NotNull Class<Date> fromType() {
        return Date.class;
    }
    
    @Override
    public @NotNull Class<LocalDate> toType() {
        return LocalDate.class;
    }
}
