package ru.sberbank.ditsib.geo.config.properties.geocoding;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpMethod;
import ru.sberbank.ditsib.geo.config.properties.ApiKeyProperties;
import ru.sberbank.ditsib.geo.config.properties.ApiProperties;
import ru.sberbank.ditsib.geo.config.properties.FormatProperties;
import ru.sberbank.ditsib.geo.config.properties.PropertiesUtils;

/**
 * Properties of geocoding.
 */
@Setter
@Getter
@NoArgsConstructor
public class GeoCodingProperties implements ApiProperties {
    
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
     * Coder properties.
     */
    private CoderProperties coder = new CoderProperties();
    
    /**
     * Reverse coder properties.
     */
    private CoderProperties reverse = new CoderProperties();
    
    /**
     * Parking properties
     */
    private CoderProperties parking = new CoderProperties();

    /**
     * Parking by number properties
     */
    private CoderProperties parkingByNumber = new CoderProperties();
    
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
    public ApiProperties getCoderProperties() {
        return PropertiesUtils.mergeProperties(this, coder);
    }
    
    /**
     * Get reverse coder properties (from coordinates to address). Merge coder properties with root properties and
     * return it. Coder properties has greater priority then root properties. API key from root will be replaced with
     * API key from coder if it exists. If url from coder started with <code>/</code>, it will be appended to the root
     * url, otherwise replace it. Method will be replaced. If path to root element at a coder mapping started with
     * <code>.</code>, it will be appended to the root mapping data, otherwise replace it. Fields mapping will be
     * appended. If path to error messages at a coder started with
     * <code>.</code>, it will be appended to the root path.
     *
     * @return merged coder properties.
     */
    public ApiProperties getReverseCoderProperties() {
        return PropertiesUtils.mergeProperties(this, reverse);
    }
    
    /**
     * Get parking properties (from coordinates to address). Merge coder properties with root properties and
     * return it. Coder properties has greater priority then root properties. API key from root will be replaced with
     * API key from coder if it exists. If url from coder started with <code>/</code>, it will be appended to the root
     * url, otherwise replace it. Method will be replaced. If path to root element at a coder mapping started with
     * <code>.</code>, it will be appended to the root mapping data, otherwise replace it. Fields mapping will be
     * appended. If path to error messages at a coder started with
     * <code>.</code>, it will be appended to the root path.
     *
     * @return merged parking properties.
     */
    public ApiProperties getParkingProperties(){
        return PropertiesUtils.mergeProperties(this, parking);
    }

    /**
     * Get parking properties by number (from coordinates to address). Merge coder properties with root properties and
     * return it. Coder properties has greater priority then root properties. API key from root will be replaced with
     * API key from coder if it exists. If url from coder started with <code>/</code>, it will be appended to the root
     * url, otherwise replace it. Method will be replaced. If path to root element at a coder mapping started with
     * <code>.</code>, it will be appended to the root mapping data, otherwise replace it. Fields mapping will be
     * appended. If path to error messages at a coder started with
     * <code>.</code>, it will be appended to the root path.
     *
     * @return merged parking properties.
     */
    public ApiProperties getParkingByNumberProperties(){
        return PropertiesUtils.mergeProperties(this, parkingByNumber);
    }
}
