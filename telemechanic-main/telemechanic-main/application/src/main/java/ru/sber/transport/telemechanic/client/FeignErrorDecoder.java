package ru.sber.transport.telemechanic.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import ru.sber.transport.telemechanic.exception.AuthorizationException;


public class FeignErrorDecoder implements ErrorDecoder {
    private static final ErrorDecoder DEFAULT_DECODER = new Default();

    @Override
    public Exception decode(String s, Response response) {
        if (HttpStatus.UNAUTHORIZED.value() == response.status()) {
            throw new AuthorizationException("External service Authorization failed");
        }
        return DEFAULT_DECODER.decode(s, response);
    }
}
