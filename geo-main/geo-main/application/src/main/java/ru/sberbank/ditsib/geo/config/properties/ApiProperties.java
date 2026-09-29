package ru.sberbank.ditsib.geo.config.properties;

import org.springframework.http.HttpMethod;
import ru.sberbank.ditsib.geo.config.properties.geocoding.RequestTypeMapping;
import ru.sberbank.ditsib.geo.config.properties.geocoding.SortMapping;

import java.util.Locale;

/**
 * Interface of API properties.
 */
public interface ApiProperties {
    
    /**
     * Get path to error messages.
     *
     * @return path to error messages.
     */
    String getErrorMessagesPath();
    
    /**
     * Set path to error messages.
     *
     * @param path path to error messages.
     */
    void setErrorMessagesPath(String path);
    
    /**
     * Get method.
     *
     * @return method.
     */
    HttpMethod getMethod();
    
    /**
     * Set method.
     *
     * @param method method.
     */
    void setMethod(HttpMethod method);
    
    /**
     * Get format.
     *
     * @return format.
     */
    FormatProperties getFormat();
    
    /**
     * Set format.
     *
     * @param formatProperties format.
     */
    void setFormat(FormatProperties formatProperties);
    
    /**
     * Get API key.
     *
     * @return API key.
     */
    ApiKeyProperties getApiKey();
    
    /**
     * Set API key.
     *
     * @param apiKey API key.
     */
    void setApiKey(ApiKeyProperties apiKey);
    
    /**
     * Get URL.
     *
     * @return URL.
     */
    String getUrl();

    /**
     * Get items URL.
     *
     * @return items URL.
     */
    String getUrlItems();
    
    /**
     * Set URL.
     *
     * @param url URL.
     */
    void setUrl(String url);

    /**
     * Set items URL.
     *
     * @param urlItems items URL.
     */
    void setUrlItems(String urlItems);
    
    /**
     * Get request type.
     */
    RequestTypeMapping getRequestType();
    
    /**
     * Set request type.
     */
    void setRequestType(RequestTypeMapping mapping);
    
    /**
     * Get sorting.
     */
    SortMapping getSort();
    
    /**
     * Set sorting.
     */
    void setSort(SortMapping mapping);
    
    /**
     * Map request type to string.
     */
    default String mapRequestType(String value) {
        return getRequestType().get(value.toLowerCase(Locale.ROOT));
    }
    
    /**
     * Map source to string.
     */
    default String mapSort(String value) {
        return getSort().get(value.toLowerCase(Locale.ROOT));
    }
    
}
