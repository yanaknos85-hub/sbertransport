package ru.sber.transport.address.providers.model;

import ru.sber.transport.address.database.TsVector;

/**
 * Interface for indexed records.
 */
public interface IndexedRecord extends HasId {

    /**
     * Get value of country field.
     *
     * @return value.
     */
    String getCountry();

    /**
     * Get value of region field.
     *
     * @return value.
     */
    String getRegion();

    /**
     * Get value of city field.
     *
     * @return value.
     */
    String getCity();

    /**
     * Get value of street field.
     *
     * @return value.
     */
    String getStreet();

    /**
     * Get value of house field.
     *
     * @return value.
     */
    String getHouse();

    /**
     * Get value of building field.
     *
     * @return value.
     */
    String getBuilding();

    /**
     * Get value of structure field.
     *
     * @return value.
     */
    String getStructure();

    /**
     * Set value of document for full text search.
     *
     * @param document value.
     */
    void setDocument(TsVector document);

    /**
     * Calculate vector for full text search.
     */
    default void calculateVector() {
        var vector = new TsVector(null);
        vector.addValue(getCountry());
        vector.addValue(getRegion());
        vector.addValue(getCity());
        vector.addValue(getStreet());
        vector.addValue(getHouse());
        vector.addValue(getBuilding());
        vector.addValue(getStructure());
        setDocument(vector);
    }

}
