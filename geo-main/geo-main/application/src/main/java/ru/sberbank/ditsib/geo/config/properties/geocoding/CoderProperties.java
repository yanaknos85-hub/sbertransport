package ru.sberbank.ditsib.geo.config.properties.geocoding;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpMethod;
import ru.sberbank.ditsib.geo.config.properties.ApiKeyProperties;
import ru.sberbank.ditsib.geo.config.properties.ApiProperties;
import ru.sberbank.ditsib.geo.config.properties.FormatProperties;

/**
 * Properties of coder.
 */
@Getter
@Setter
@NoArgsConstructor
public class CoderProperties implements ApiProperties {
    
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
     * Маппинг сортировок.
     */
    private SortMapping sort = new SortMapping();
    
    /**
     * Маппинг типов запроса.
     */
    private RequestTypeMapping requestType = new RequestTypeMapping();
    
}
