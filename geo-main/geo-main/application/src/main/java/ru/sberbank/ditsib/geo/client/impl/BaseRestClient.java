package ru.sberbank.ditsib.geo.client.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.sber.transport.utils.collections.MapUtils;
import ru.sberbank.ditsib.geo.config.properties.ApiProperties;
import ru.sberbank.ditsib.geo.config.properties.MappingProperties;
import ru.sberbank.ditsib.geo.exceptions.GeoApiException;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

/**
 * Base rest client implementation.
 */
@Slf4j
@RequiredArgsConstructor
abstract class BaseRestClient {

    private static final String ITEMS_KEY = "items";

    private static final String RESULT_KEY = "result";

    private final ObjectMapper objectMapper;

    private final MapUtils mapUtils;

    private static final Map<String, Integer> SCHEMA_PORT_MAPPING = Map.of(
        "http", 80,
        "https", 443
    );

    /**
     * Perform request.
     *
     * @param parameters parameters.
     * @return response.
     */
    @SneakyThrows
    protected Map<String, Object> request(ApiProperties properties, Map<String, Object> parameters) {
        var restTemplate = getRestTemplate();
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.USER_AGENT, "AS Transport/Sber");
        HttpEntity<?> httpEntity = new HttpEntity<>(new HashMap<>(), headers);

        var url = properties.getUrl();
        var apiKey = properties.getApiKey();
        var method = properties.getMethod();
        var requestMapping = properties.getFormat().getRequest();

        url += String.format("%s%s=%s", url.contains("?") ? "&" : "?", apiKey.getField(), apiKey.getValue());

        url = processUrl(url, parameters);

        if (POST.equals(method)) {
            parameters = processPostParameters(requestMapping, parameters);
            httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(parameters),
                    httpEntity.getHeaders());
        } else if (GET.equals(method)) {
            var uri = URI.create(url);
            var port = getPort(uri);
            url = String.format("%s://%s:%s%s", uri.getScheme(), uri.getHost(), port, uri.getPath());
            if (parameters.get("q") != null && parameters.get("type") != null && parameters.get("type").equals("branch")) {
                var location = parameters.get("q");
                parameters.putAll(toMap(uri.getQuery()));
                parameters.put("q", String.valueOf(parameters.get("q")) + location);
            } else {
                parameters.putAll(toMap(uri.getQuery()));
            }
            var uriParameters = processGetParameters(requestMapping, parameters);
            var urlBuilder = fillBuilder(url, uriParameters);
            url = String.format("%s?%s", url, urlBuilder);
        } else {
            throw new IllegalStateException("Unexpected value: " + method);
        }

        ResponseEntity<Map<String, Object>> response = null;
        try {
            log.debug("Requested url: {} ; with data {}", url, httpEntity);
            response = requestAttempt(restTemplate, url, method, httpEntity);

            if (!response.hasBody() || response.getBody() == null) {
                return Map.of();
            }

            var result = response.getBody();
            if (parameters.get("q") != null && !result.containsKey(RESULT_KEY) && !parameters.get("type").equals("branch")) {
                result = sendCrutchRequest(restTemplate, properties, parameters, method, httpEntity);
            }
            return result;
        } catch (Exception e) {
            log.error("Processing url: %s ; with data %s failed".formatted(url, httpEntity), e);
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                return Map.of();
            }
            throw new GeoApiException(List.of(e.getMessage()));
        }
    }

    private ResponseEntity<Map<String, Object>> requestAttempt(RestTemplate restTemplate, String url, HttpMethod method, HttpEntity<?> httpEntity) {
        try {
            return restTemplate.exchange(url, method, httpEntity, new ParameterizedTypeReference<>() {
            });
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() != 502) {
                throw e;
            }

            return restTemplate.exchange(url, method, httpEntity, new ParameterizedTypeReference<>() {
            });
        }
    }

    private int getPort(URI uri) {
        var scheme = Optional.ofNullable(uri.getScheme()).orElse("http");
        return Optional.of(uri.getPort())
            .filter(p -> p != -1)
            .orElseGet(() -> SCHEMA_PORT_MAPPING.get(scheme));
    }

    private Map<String, Object> sendCrutchRequest(RestTemplate restTemplate, ApiProperties properties, Map<String, Object> parameters,
                                                  HttpMethod method, HttpEntity<?> httpEntity) throws JsonProcessingException {
        var itemsUrl = properties.getUrlItems() + "&q=" + parameters.get("q") + "&key=" + properties.getApiKey().getValue();
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(itemsUrl, method, httpEntity, new ParameterizedTypeReference<>() {
        });
        var result = response.getBody();
        if (result == null || !result.containsKey(RESULT_KEY)) {
            return Map.of();
        } else {
            var convertedResult = new ArrayList<Map<String, Object>>();
            var extractedResults = mapUtils.extractNode(result, properties.getFormat().getResponse().getRoot(), List.class);
            for (var item : extractedResults) {
                convertedResult.add(ReflectionUtils.castObjectToMap(item, String.class, Object.class));
            }
            ResponseEntity<Map<String, Object>> sortedResponse;
            for (var resultItem : convertedResult) {
                if (resultItem.get("type") != null && !resultItem.get("type").equals("building") && resultItem.get("point") != null) {
                    Map<String, Object> point = (Map<String, Object>) resultItem.get("point");
                    var sortPointItemsUrl = itemsUrl + "&sort_point=" + point.get("lon") + "," + point.get("lat");
                    sortedResponse = restTemplate.exchange(sortPointItemsUrl, method, httpEntity, new ParameterizedTypeReference<>() {
                    });
                    var sortedResult = sortedResponse.getBody();
                    if (sortedResult != null && sortedResult.containsKey(RESULT_KEY)) {
                        Map<String, Object> resultMap = (Map<String, Object>) result.get(RESULT_KEY);
                        Map<String, Object> sortedResultMap = (Map<String, Object>) sortedResult.get(RESULT_KEY);
                        ArrayList<Object> items = (ArrayList<Object>) resultMap.get(ITEMS_KEY);
                        ArrayList<Object> sortedItems = (ArrayList<Object>) sortedResultMap.get(ITEMS_KEY);
                        sortedItems.forEach(sortedItem -> {
                            if (!items.contains(sortedItem)) {
                                items.add(sortedItem);
                            }
                        });
                        resultMap.replace(ITEMS_KEY, items);
                        result.replace(RESULT_KEY, resultMap);
                    }
                }
            }
        }
        return result;
    }

    private StringBuilder fillBuilder(String url, Map<String, Object> uriParameters) {
        var urlBuilder = new StringBuilder();
        for (var entry : uriParameters.entrySet()) {
            if (!urlBuilder.toString().isEmpty()) {
                urlBuilder.append("&");
            }
            if (!url.contains("{" + entry.getKey() + "}")) {
                urlBuilder.append(String.format("%s=%s", entry.getKey(), entry.getValue()));
            }
        }
        return urlBuilder;
    }

    /**
     * Гео провайдер tomtom некорректно парсит слеш / из параметра address в GET запросе. Метод заэкранирует слеш в
     * конечном url для запроса
     *
     * @param parameters параметры запроса, содержащие адрес по ключу location
     */
    private void escapeSlashSymbolForTomtomGeoProvider(MappingProperties mapping, Map<String, Object> parameters) {
        String oldAddress = (String) parameters.get(mapping.getFields().getLocation());
        String escapedAddress = oldAddress.replace("/", "%2F");
        parameters.put(mapping.getFields().getLocation(), escapedAddress);
    }

    /**
     * Process parameters for GET request.
     *
     * @param parameters parameters.
     * @return processed parameters.
     */
    private Map<String, Object> processGetParameters(MappingProperties mapping, Map<String, Object> parameters) {
        if (parameters.get(mapping.getFields().getLocation()) instanceof String) {
            escapeSlashSymbolForTomtomGeoProvider(mapping, parameters);
        }
        return filterParameters(parameters, mapping);
    }

    /**
     * Parameters filtering. Parameters with null-value will be filtered out.
     *
     * @param parameters source parameters.
     * @param mapping    fields mapping.
     * @return filtered parameters.
     */
    private Map<String, Object> filterParameters(Map<String, Object> parameters, MappingProperties mapping) {
        var processedMap = new HashMap<String, Object>();
        for (var entry : parameters.entrySet()) {
            var key = entry.getKey();
            if (key != null && key.contains(".")) {
                processedMap.putAll(mapUtils.wrap(key, String.class, parameters.get(key), Object.class));
            } else {
                addToMap(processedMap, key, entry.getValue());
            }
        }
        return processedMap.keySet().stream()
            .filter(Objects::nonNull)
            .filter(key -> parameters.get(key) != null && !key.isBlank())
            .collect(Collectors
                .toMap(key -> mapping.getFields().getOrDefault(key, key),
                    processedMap::get));
    }

    private void addToMap(Map<String, Object> target, String key, Object value) {
        if (value != null && Collection.class.isAssignableFrom(value.getClass()) &&
            !((Collection) value).stream().allMatch(String.class::isInstance)) {
            var processedItems = new ArrayList<Map<Object, Object>>();
            for (var item : ReflectionUtils.castObjectToList(value, Object.class)) {
                var itemMap = ReflectionUtils.castObjectToMap(item);
                if (itemMap != null && target.containsKey(key)) {
                    itemMap.putAll(ReflectionUtils.castObjectToMap(target.get(key)));
                }
                processedItems.add(itemMap);
            }
            value = processedItems;
        }
        target.put(key, value);
    }

    /**
     * Process parameters for POST request.
     *
     * @param parameters parameters.
     * @return processed parameters.
     */
    private Map<String, Object> processPostParameters(MappingProperties mapping, Map<String, Object> parameters) {
        var processedParameters = filterParameters(parameters, mapping);

        return mapUtils.wrap(mapping.getRoot(), String.class, processedParameters, Object.class);
    }

    /**
     * Process URL variables.
     *
     * @param url        source URL.
     * @param parameters parameters to inject.
     * @return URL with injected parameters.
     */
    private String processUrl(String url, Map<String, Object> parameters) { // NOSONAR
        if (url.contains("{") && url.contains("}")) {
            var urlParts = url.split("\\{");
            for (var urlPart : urlParts) {
                if (urlPart.contains("}")) {
                    urlPart = urlPart.split("}")[0];
                    var matcher = Pattern.compile("\\[\\D*]").matcher(urlPart);
                    var result = new StringBuilder();
                    if (matcher.find()) {
                        var values = matcher.group();
                        var separatorMatcher = Pattern.compile("\\(\\D*\\)").matcher(urlPart);
                        if (separatorMatcher.find()) {
                            var separator = separatorMatcher.group();
                            var parameter = urlPart.replace(separator, "").replace(values, "");
                            separator = separator.replaceAll("[()]", "");
                            values = values.replaceAll("[\\[\\]]", "");
                            var valuesSeparatorMatcher = Pattern.compile("(\\W)").matcher(values);
                            if (valuesSeparatorMatcher.find()) {
                                var valuesSeparator = valuesSeparatorMatcher.group();
                                var valuesPaths = values.split(valuesSeparator);
                                var parameterItems = (List<?>) parameters.get(parameter);
                                for (var item : parameterItems) {
                                    if (!result.toString().isEmpty()) {
                                        result.append(separator);
                                    }
                                    var valueBuilder = new StringBuilder();
                                    for (var valuePath : valuesPaths) {
                                        if (!valueBuilder.toString().isEmpty()) {
                                            valueBuilder.append(valuesSeparator);
                                        }
                                        parameters.remove(valuePath);
                                        valueBuilder.append(((Map<?, ?>) item).get(valuePath));
                                    }
                                    result.append(valueBuilder);
                                }
                            }
                        }
                        url = url.replace("{" + urlPart + "}", result.toString());
                        parameters.remove(urlPart.split("\\[")[0]);
                    } else {
                        var parameter = parameters.get(urlPart);
                        url = url.replace("{" + urlPart + "}", String.valueOf(parameter));
                    }

                }
            }
        }
        return url;
    }

    private Map<String, String> toMap(String query) {
        var params = query.split("&");
        var map = new HashMap<String, String>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            if (value != null && !Pattern.compile("null[^a-zA-Z0-9]|[^a-zA-Z0-9]null").matcher(value).find()) {
                map.put(name, value);
            }
        }
        return map;
    }

    @Lookup
    RestTemplate getRestTemplate() {
        return null;
    }
}
