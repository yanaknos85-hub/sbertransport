package ru.sber.transport.common.api.service.service.client;

import feign.Logger;
import feign.Request;
import feign.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static feign.Logger.Level.HEADERS;

@Slf4j
public class FeignLogger extends Logger {
    
    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        if (logLevel.ordinal() >= HEADERS.ordinal()) {
            super.logRequest(configKey, logLevel, request);
        } else {
            var bodyLength = 0;
            if (request.body() != null) {
                bodyLength = request.body().length;
            }
            log(configKey, "---> %s %s HTTP/1.1 (%s-byte body) ", request.httpMethod().name(), request.url(), bodyLength);
        }
    }
    
    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) throws IOException {
        if (logLevel.ordinal() >= HEADERS.ordinal()) {
            super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);
        } else {
            var status = response.status();
            var request = response.request();
            log(configKey, "<--- %s %s HTTP/1.1 %s (%sms) ", request.httpMethod().name(), request.url(), status, elapsedTime);
        }
        return response;
    }
    
    @Override
    protected void log(String configKey, String format, Object... args) {
        log.info(format(configKey, format, args));
    }
    
    protected String format(String configKey, String format, Object... args) {
        format = methodTag(configKey) + format;
        return String.format(format, args);
    }
}
