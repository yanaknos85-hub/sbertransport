package ru.sber.transport.common.api.service.model;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Credentials {

    private final Map<String, Map<String, Contractor>> credentialMap = new ConcurrentHashMap<>();

    public void add(URI url, String login, Contractor contractor) {
        credentialMap.computeIfAbsent(url.getHost(), host -> new ConcurrentHashMap<>());
        credentialMap.computeIfPresent(url.getHost(),
                (host, credentialsByHost) -> { credentialsByHost.put(login, contractor); return credentialsByHost; });
    }

    public Contractor get(URI url, String login) {
        if (url.getHost() == null || login == null) {
            return null;
        }

        return Optional.ofNullable(credentialMap.get(url.getHost()))
                .map(credentialsByHost ->credentialsByHost.get(login))
                .orElse(null);
    }

}
