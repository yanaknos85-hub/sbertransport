package ru.sber.transport.trip.web.service.impl.file_resolvers.utils;

import lombok.NonNull;
import ru.sber.transport.trip.business.model.HasName;

import java.util.Optional;

public class StringHandlerUtil
{
    private static final String NOT_DATA = "Н/Д";

    public static String mapNotNull(String source) {
        if (source == null || source.isBlank()) {
            return NOT_DATA;
        }
        return source;
    }

    public static void append(@NonNull StringBuilder builder, String source, String delimiter) {
        if (source != null) {
            if (!builder.isEmpty()) {
                builder.append(delimiter);
            }
            builder.append(source);
        }
    }

    public static String mapName(HasName hasName) {
        if (hasName == null) {
            return null;
        }
        var nameBuilder = new StringBuilder();
        append(nameBuilder, hasName.getFirstName(), " ");
        append(nameBuilder, hasName.getPatronymic(), " ");
        append(nameBuilder, Optional.ofNullable(hasName.getLastName()).map(s -> s.charAt(0)).map(c -> c + ".").orElse(""), " ");
        return nameBuilder.toString();
    }


}
