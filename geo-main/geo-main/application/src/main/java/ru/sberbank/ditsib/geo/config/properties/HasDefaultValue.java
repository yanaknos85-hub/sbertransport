package ru.sberbank.ditsib.geo.config.properties;

/**
 * Interface of value-class with default value.
 *
 * @param <T> type of value.
 */
public interface HasDefaultValue<T> {
    
    /**
     * Get default value.
     *
     * @return value.
     */
    T getDefault();
    
}
