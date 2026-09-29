package ru.sberbank.ditsib.geo.config.properties;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpMethod;
import ru.sberbank.ditsib.geo.config.properties.geocoding.RequestTypeMapping;
import ru.sberbank.ditsib.geo.config.properties.geocoding.SortMapping;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Utils for working with properties.
 */
@UtilityClass
public class PropertiesUtils {
    
    /**
     * Merge the root properties with given.
     *
     * @param sourceProperties source properties.
     * @param propertiesToMerge properties to merge with root.
     *
     * @return merged properties.
     */
    @SneakyThrows({ NoSuchMethodException.class, SecurityException.class, InstantiationException.class,
                    IllegalAccessException.class, IllegalArgumentException.class, InvocationTargetException.class })
    public ApiProperties mergeProperties(ApiProperties sourceProperties, ApiProperties propertiesToMerge) {
        var coder = propertiesToMerge.getClass().getDeclaredConstructor().newInstance();
    
        coder.setUrl(mergeUrl(sourceProperties, propertiesToMerge));
        coder.setUrlItems(mergeUrlItems(sourceProperties, propertiesToMerge));
        coder.setApiKey(mergeApiKey(sourceProperties, propertiesToMerge));
        coder.setFormat(mergeFormat(sourceProperties, propertiesToMerge));
        coder.setMethod(mergeMethod(sourceProperties, propertiesToMerge));
        coder.setSort(mergeSort(sourceProperties, propertiesToMerge));
        coder.setRequestType(mergeRequestType(sourceProperties, propertiesToMerge));
        coder.setErrorMessagesPath(mergeErrorMessagesPath(sourceProperties, propertiesToMerge));
    
        return coder;
    }
    
    /**
     * Merge sorting properties.
     *
     * @param propertiesToMerge source base properties.
     * @param source source properties.
     *
     * @return merged properties.
     */
    private static SortMapping mergeSort(ApiProperties source, ApiProperties propertiesToMerge) {
        var mappingProperties = new SortMapping();
    
        if (source != null) {
            mappingProperties = source.getSort();
        }
    
        if (source != null && source.getSort() != null) {
            var defaultFields = new HashMap<>(source.getSort());
            var propertiesFields = new HashMap<>(propertiesToMerge.getSort());
        
            defaultFields.putAll(propertiesFields.keySet().stream()
                                                 .filter(key -> propertiesFields.get(key) != null)
                                                 .collect(Collectors.toMap(key -> key,
                                                                           propertiesFields::get)));
            defaultFields.keySet().stream()
                         .filter(key -> defaultFields.get(key) == null)
                         .forEach(key -> defaultFields.put(key, key));
        
            var mappingFields = new SortMapping();
            mappingFields.putAll(defaultFields);
        
            mappingProperties = mappingFields;
        }
    
        return mappingProperties;
    }
    
    /**
     * Merge request type properties.
     *
     * @param propertiesToMerge source base properties.
     * @param source source properties.
     *
     * @return merged properties.
     */
    private static RequestTypeMapping mergeRequestType(ApiProperties source, ApiProperties propertiesToMerge) {
        var mappingProperties = new RequestTypeMapping();
    
        if (source != null) {
            mappingProperties = source.getRequestType();
        }
    
        if (source != null && source.getSort() != null) {
            var defaultFields = new HashMap<>(source.getRequestType());
            var propertiesFields = new HashMap<>(propertiesToMerge.getRequestType());
        
            defaultFields.putAll(propertiesFields.keySet().stream()
                                                 .filter(key -> propertiesFields.get(key) != null)
                                                 .collect(Collectors.toMap(key -> key,
                                                                           propertiesFields::get)));
            defaultFields.keySet().stream()
                         .filter(key -> defaultFields.get(key) == null)
                         .forEach(key -> defaultFields.put(key, key));
        
            var mappingFields = new RequestTypeMapping();
            mappingFields.putAll(defaultFields);
        
            mappingProperties = mappingFields;
        }
    
        return mappingProperties;
    }
    
    /**
     * Merge path to error message.
     *
     * @param sourceProperties source base properties.
     * @param source source properties.
     */
    private String mergeErrorMessagesPath(ApiProperties sourceProperties, ApiProperties source) {
        if (source.getErrorMessagesPath() != null) {
            if (source.getErrorMessagesPath().startsWith(".")) {
                return sourceProperties.getErrorMessagesPath() + source.getErrorMessagesPath();
            } else {
                return source.getErrorMessagesPath();
            }
        }
        return sourceProperties.getErrorMessagesPath();
    }
    
    /**
     * Merge mapping.
     *
     * @param sourceProperties source base properties.
     * @param source source properties.
     *
     * @return merged properties.
     */
    private MappingProperties mergeMapping(MappingProperties sourceProperties, MappingProperties source) {
        var mappingProperties = new MappingProperties();
    
        if (sourceProperties != null) {
            mappingProperties.setRoot(sourceProperties.getRoot());
            mappingProperties.setFields(sourceProperties.getFields());
        }
        if (source != null && source.getRoot() != null) {
            if (source.getRoot().startsWith(".")) {
                mappingProperties.setRoot(mappingProperties.getRoot() + source.getRoot());
            } else {
                mappingProperties.setRoot(source.getRoot());
            }
        }
    
        if (source != null && source.getFields() != null) {
            var defaultFields = new HashMap<>(mappingProperties.getFields());
            var propertiesFields = new HashMap<>(source.getFields());
    
            defaultFields.putAll(propertiesFields.keySet().stream()
                                                 .filter(key -> propertiesFields.get(key) != null)
                                                 .collect(Collectors.toMap(key -> key,
                                                                           propertiesFields::get)));
            defaultFields.keySet().stream()
                         .filter(key -> defaultFields.get(key) == null)
                         .forEach(key -> defaultFields.put(key, key));
    
            var mappingFields = new MappingFields();
            mappingFields.putAll(defaultFields);
    
            mappingProperties.setFields(mappingFields);
        }
    
        return mappingProperties;
    }
    
    /**
     * Merge method.
     *
     * @param sourceProperties source base properties.
     * @param source source properties.
     */
    private HttpMethod mergeMethod(ApiProperties sourceProperties, ApiProperties source) {
        if (source.getMethod() != null) {
            return source.getMethod();
        }
        return sourceProperties.getMethod();
    }
    
    /**
     * Merge format.
     *
     * @param sourceProperties source base properties.
     * @param source source properties.
     */
    private FormatProperties mergeFormat(ApiProperties sourceProperties, ApiProperties source) {
        var properties = new FormatProperties();
        
        properties.setRequest(mergeMapping(sourceProperties.getFormat().getRequest(),
                                           source.getFormat().getRequest()));
        
        properties.setResponse(mergeMapping(sourceProperties.getFormat().getResponse(),
                                            source.getFormat().getResponse()));
        
        return properties;
    }
    
    /**
     * Merge API key.
     *
     * @param sourceProperties source base properties.
     * @param source source properties.
     */
    private ApiKeyProperties mergeApiKey(ApiProperties sourceProperties, ApiProperties source) {
        if (source.getApiKey().getValue() != null) {
            return source.getApiKey();
        }
        return sourceProperties.getApiKey();
    }
    
    /**
     * Merge url.
     *
     * @param sourceProperties source base properties.
     * @param source source properties.
     */
    private String mergeUrl(ApiProperties sourceProperties, ApiProperties source) {
        if (source.getUrl() != null) {
            if (source.getUrl().startsWith("/") || source.getUrl().startsWith("?")) {
                return sourceProperties.getUrl() + source.getUrl();
            } else {
                return source.getUrl();
            }
        }
        return sourceProperties.getUrl();
    }

    /**
     * Merge items url.
     *
     * @param sourceProperties source base properties.
     * @param source source properties.
     */
    private String mergeUrlItems(ApiProperties sourceProperties, ApiProperties source) {
        if (source.getUrlItems() != null) {
            if (source.getUrlItems().startsWith("/") || source.getUrlItems().startsWith("?")) {
                return sourceProperties.getUrl() + source.getUrlItems();
            } else {
                return source.getUrlItems();
            }
        }
        return sourceProperties.getUrl();
    }
}
