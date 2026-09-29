package ru.sberbank.ditsib.geo.exceptions;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception throws when GEO API requesting failed.
 */
@Getter
public class GeoApiException extends RuntimeException {

    /**
     * Format of exception message.
     */
    public static final String MSG_FORMAT = "GEO API request failed.\r\nMessages:\r\n%s";

    /**
     * List of received messages.
     */
    private final List<String> messages = new ArrayList<>();

    /**
     * Code from provider.
     */
    private final Integer code;

    /**
     * Create a new exception.
     *
     * @param messages collection of messages from service.
     */
    public GeoApiException(List<String> messages) {
        this(null, messages);
    }

    /**
     * Create a new exception.
     *
     * @param code code received from provider.
     * @param messages collection of messages from service.
     */
    public GeoApiException(Integer code, List<String> messages) {
        super(String.format(MSG_FORMAT, String.join("\r\n", messages)));
        this.code = code;
        this.messages.addAll(messages);
    }
}
