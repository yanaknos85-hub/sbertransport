package ru.sberbank.ditsib.geo.config.properties.routing;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpMethod;
import ru.sberbank.ditsib.geo.config.properties.ApiKeyProperties;
import ru.sberbank.ditsib.geo.config.properties.ApiProperties;
import ru.sberbank.ditsib.geo.config.properties.FormatProperties;
import ru.sberbank.ditsib.geo.config.properties.PropertiesUtils;
import ru.sberbank.ditsib.geo.config.properties.geocoding.RequestTypeMapping;
import ru.sberbank.ditsib.geo.config.properties.geocoding.SortMapping;

/**
 * Properties of routing.
 */
@Setter
@Getter
@NoArgsConstructor
public class RoutingProperties implements ApiProperties {
    
    /**
     * API key.
     */
    private ApiKeyProperties apiKey = new ApiKeyProperties();
    
    /**
     * URL.
     */
    private String url;

    /**
     * items URL.
     */
    private String urlItems;


    /**
     * Path to error messages. It must be a dot-separated path. For example: One message or array of messages:
     * <code>path.to.message.or.array</code> One message from array:
     * <code>path.to.message[15].of.array</code>
     */
    private String errorMessagesPath;
    
    /**
     * Method to request data.
     */
    private HttpMethod method;
    
    /**
     * Format of request/response.
     */
    private FormatProperties format = new FormatProperties();
    
    /**
     * Properties of units.
     */
    private UnitProperties unit = new UnitProperties();
    
    /**
     * Properties of units.
     */
    private TypeProperties type = new TypeProperties();
    
    /**
     * Coder properties.
     */
    private RouteProperties route = new RouteProperties();
    
    /**
     * Маппинг сортировок.
     */
    private SortMapping sort = new SortMapping();
    
    /**
     * Маппинг типов запроса.
     */
    private RequestTypeMapping requestType = new RequestTypeMapping();
    
    /**
     * Get coder properties (from address to coordinates). Merge coder properties with root properties and return it.
     * Coder properties has greater priority then root properties. API key from root will be replaced with API key from
     * coder if it exists. If url from coder started with <code>/</code>, it will be appended to the root url, otherwise
     * replace it. Method will be replaced. If path to root element at a coder mapping started with <code>.</code>, it
     * will be appended to the root mapping data, otherwise replace it. Fields mapping will be appended. If path to
     * error messages at a coder started with
     * <code>.</code>, it will be appended to the root path.
     *
     * @return merged coder properties.
     */
    public ApiProperties getRouteProperties() {
        return PropertiesUtils.mergeProperties(this, route);
    }

}
