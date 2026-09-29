package ru.sberbank.ditsib.geo.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import ru.sberbank.ditsib.geo.config.properties.geocoding.GeoCodingProperties;
import ru.sberbank.ditsib.geo.config.properties.geocoding.RequestTypeMapping;
import ru.sberbank.ditsib.geo.config.properties.geocoding.SortMapping;
import ru.sberbank.ditsib.geo.config.properties.region.RegionResolverProperties;
import ru.sberbank.ditsib.geo.config.properties.routerecreation.RouteRecreationProperties;
import ru.sberbank.ditsib.geo.config.properties.routing.RoutingProperties;

/**
 * Properties of geo service.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "geo")
public class GeoProperties implements ApiProperties {
    
    /**
     * API key.
     */
    private ApiKeyProperties apiKey = new ApiKeyProperties();
    
    /**
     * URL to request.
     */
    private String url;

    /**
     * items URL.
     */
    private String urlItems;

    /**
     * Method to request.
     */
    private HttpMethod method = HttpMethod.GET;
    
    /**
     * Format of request/response.
     */
    private FormatProperties format = new FormatProperties();
    
    /**
     * Properties of region resolver.
     */
    private RegionResolverProperties region = new RegionResolverProperties();
    
    /**
     * Properties of geocoding.
     */
    private GeoCodingProperties geoCoding = new GeoCodingProperties();
    
    @Getter
    private RoutingProperties routing = new RoutingProperties();

    /**
     * Properties of route recreation.
     */
    private RouteRecreationProperties routeRecreation = new RouteRecreationProperties();
    
    /**
     * Маппинг сортировок.
     */
    private SortMapping sort = new SortMapping();
    
    /**
     * Маппинг типов запроса.
     */
    private RequestTypeMapping requestType = new RequestTypeMapping();
    
    /**
     * Dot-separated path to error messages.
     */
    private String errorMessagesPath;
    
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
        return PropertiesUtils.mergeProperties(this, geoCoding.getCoderProperties());
    }
    
    /**
     * Get region properties (resolving a region). Merge properties with root properties and return it. Region
     * properties has greater priority then root properties. API key from root will be replaced with API key from region
     * if it exists. If url from coder started with <code>/</code>, it will be appended to the root url, otherwise
     * replace it. Method will be replaced. If path to root element at a coder mapping started with
     * <code>.</code>, it will be appended to the root mapping data, otherwise replace it. Fields mapping will be
     * appended. If path to error messages at a region started with <code>.</code>, it will be appended to the root
     * path.
     *
     * @return merged region properties.
     */
    public ApiProperties getRegionProperties() {
        return PropertiesUtils.mergeProperties(this, region);
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
        return PropertiesUtils.mergeProperties(this, geoCoding.getReverseCoderProperties());
    }
    
    /**
     * Get route properties. Merge route properties with root properties and return it. Route properties has greater
     * priority then root properties. API key from root will be replaced with API key from route if it exists. If url
     * from route started with <code>/</code>, it will be appended to the root url, otherwise replace it. Method will be
     * replaced. If path to root element at a route mapping started with <code>.</code>, it will be appended to the root
     * mapping data, otherwise replace it. Fields mapping will be appended. If path to error messages at a coder started
     * with <code>.</code>, it will be appended to the root path.
     *
     * @return route properties.
     */
    public ApiProperties getRouteProperties() {
        return PropertiesUtils.mergeProperties(this, routing.getRouteProperties());
    }

    /**
     * Get route recreation properties. Merge route properties with root properties and return it. Route properties has greater
     * priority then root properties. API key from root will be replaced with API key from route if it exists. If url
     * from route started with <code>/</code>, it will be appended to the root url, otherwise replace it. Method will be
     * replaced. If path to root element at a route mapping started with <code>.</code>, it will be appended to the root
     * mapping data, otherwise replace it. Fields mapping will be appended. If path to error messages at a coder started
     * with <code>.</code>, it will be appended to the root path.
     *
     * @return route properties.
     */
    public ApiProperties getRouteRecreationProperties() {
        return PropertiesUtils.mergeProperties(this, routeRecreation);
    }
    
    /**
     * Get parking properties. Merge route properties with root properties and return it. Route properties has greater
     * priority then root properties. API key from root will be replaced with API key from route if it exists. If url
     * from route started with <code>/</code>, it will be appended to the root url, otherwise replace it. Method will be
     * replaced. If path to root element at a route mapping started with <code>.</code>, it will be appended to the root
     * mapping data, otherwise replace it. Fields mapping will be appended. If path to error messages at a coder started
     * with <code>.</code>, it will be appended to the root path.
     *
     * @return parking properties.
     */
    public ApiProperties getParkingProperties(){
        return PropertiesUtils.mergeProperties(this, geoCoding.getParkingProperties());
    }

    /**
     * Get parking properties by number. Merge route properties with root properties and return it. Route properties has greater
     * priority then root properties. API key from root will be replaced with API key from route if it exists. If url
     * from route started with <code>/</code>, it will be appended to the root url, otherwise replace it. Method will be
     * replaced. If path to root element at a route mapping started with <code>.</code>, it will be appended to the root
     * mapping data, otherwise replace it. Fields mapping will be appended. If path to error messages at a coder started
     * with <code>.</code>, it will be appended to the root path.
     *
     * @return parking properties.
     */
    public ApiProperties getParkingByNumberProperties(){
        return PropertiesUtils.mergeProperties(this, geoCoding.getParkingByNumberProperties());
    }
}
