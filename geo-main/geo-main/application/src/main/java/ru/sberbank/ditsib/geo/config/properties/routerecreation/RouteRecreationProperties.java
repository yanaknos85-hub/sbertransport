package ru.sberbank.ditsib.geo.config.properties.routerecreation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpMethod;
import ru.sberbank.ditsib.geo.config.properties.ApiKeyProperties;
import ru.sberbank.ditsib.geo.config.properties.ApiProperties;
import ru.sberbank.ditsib.geo.config.properties.FormatProperties;
import ru.sberbank.ditsib.geo.config.properties.geocoding.RequestTypeMapping;
import ru.sberbank.ditsib.geo.config.properties.geocoding.SortMapping;

/**
 * Properties of route recreation.
 */
@Setter
@Getter
@NoArgsConstructor
public class RouteRecreationProperties implements ApiProperties {

    /**
     * API key.
     */
    private ApiKeyProperties apiKey = new ApiKeyProperties();

    /**
     * URL.
     */
    private String url;

    /**
     * Method to request data.
     */
    private HttpMethod method;

    /**
     * Format of request/response.
     */
    private FormatProperties format = new FormatProperties();

    /**
     * Path to error messages. It must be a dot-separated path. For example: One message or array of messages:
     * <code>path.to.message.or.array</code> One message from array:
     * <code>path.to.message[15].of.array</code>
     */
    private String errorMessagesPath;

    /**
     * items URL.
     */
    private String urlItems;

    /**
     * Маппинг типов запроса.
     */
    private RequestTypeMapping requestType = new RequestTypeMapping();

    /**
     * Маппинг сортировок.
     */
    private SortMapping sort = new SortMapping();

    /**
     * Default bad point tolerance level: high, medium, low.
     */
    private String defaultBadPointTolerance = "high";
}
