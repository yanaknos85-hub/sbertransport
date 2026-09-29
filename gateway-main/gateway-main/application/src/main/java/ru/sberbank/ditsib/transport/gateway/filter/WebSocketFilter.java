package ru.sberbank.ditsib.transport.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

@Component
@Slf4j
public class WebSocketFilter implements GlobalFilter, Ordered {

    private static final String HTTPS_SCHEME = "https";

    private static final String HTTP_SCHEME = "http";

    @Value("${https.enabled}")
    private boolean httpsEnabled;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI requestUrl = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);
        String scheme = requestUrl.getScheme();
        log.info(String.format("Request to %s will be handle by %s", exchange.getRequest().getURI(), requestUrl));
        if (!"ws".equals(scheme) && !"wss".equals(scheme)) {
            return chain.filter(
                    exchange.mutate().request(
                            exchange.getRequest().mutate()
                                    .header("referer", applySchema(exchange.getRequest().getURI().toString()))
                                    .build())
                            .build());
        } else if ("/ws".equals(requestUrl.getPath())) {
            String wsScheme = convertWsToHttp(scheme);
            URI wsRequestUrl = UriComponentsBuilder.fromUri(requestUrl).scheme(wsScheme).build().toUri();
            exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, wsRequestUrl);
        }
        
        //Solution returns multiple origin information
        return chain.filter(exchange).then(Mono.defer(() -> {
            exchange.getResponse().getHeaders().entrySet().stream()
                    .filter(kv -> (kv.getValue() != null && kv.getValue().size() > 1))
                    .filter(kv -> (kv.getKey().equals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
                                   || kv.getKey().equals(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS)))
                    .forEach(kv ->
                            kv.setValue(List.of(kv.getValue().get(0))));
            
            return chain.filter(exchange);
        }));
    }
    
    private String applySchema(String uri) {
        if (httpsEnabled && !uri.startsWith(HTTPS_SCHEME) && uri.startsWith(HTTP_SCHEME)) {
            return uri.replaceFirst(HTTP_SCHEME, HTTPS_SCHEME);
        }
        return uri;
    }
    
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 2;
    }
    
    private static String convertWsToHttp(String scheme) {
        return scheme.replace("wss", HTTPS_SCHEME).replace("ws", HTTP_SCHEME);
    }
}