package ru.sber.transport.roles.common.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
class UrlImpl implements Url {

    private UUID id;

    private HttpMethod method;

    private String restrictedUrl;

    private String pattern;

    private Set<String> roles = new HashSet<>();

    private boolean isNew;
    
}
