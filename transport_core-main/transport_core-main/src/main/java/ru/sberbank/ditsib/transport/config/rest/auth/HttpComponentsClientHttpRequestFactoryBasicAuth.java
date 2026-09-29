package ru.sberbank.ditsib.transport.config.rest.auth;

import lombok.AllArgsConstructor;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.NonNull;

import java.net.URI;

/**
 * Реализация Basic Auth
 */
@AllArgsConstructor
public class HttpComponentsClientHttpRequestFactoryBasicAuth extends HttpComponentsClientHttpRequestFactory {
    
    private final HttpHost host;

    @Override
    protected HttpContext createHttpContext(@NonNull HttpMethod httpMethod, @NonNull URI uri) {
        return createHttpContext();
    }
    
    private HttpContext createHttpContext() {
        var authCache = new BasicAuthCache();
        
        var basicAuth = new BasicScheme();
        authCache.put(host, basicAuth);
        
        var localcontext = new BasicHttpContext();
        localcontext.setAttribute(HttpClientContext.AUTH_CACHE, authCache);
        return localcontext;
    }
}
