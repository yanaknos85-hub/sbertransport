package ru.sberbank.ditsib.geo.config.properties;

import java.util.HashMap;

/**
 * Маппинг полей.
 */
public class MappingFields extends HashMap<String, String> {

    /**
     * Название поля страны.
     *
     * @return название поля.
     */
    public String getCountry() {
        return get("country");
    }

    /**
     * Название поля типа данных.
     *
     * @return название поля.
     */
    public String getType() {
        return get("type");
    }

    /**
     * Название поля области.
     *
     * @return название поля.
     */
    public String getDistrict() {
        return get("district");
    }

    /**
     * Название поля авто.
     *
     * @return название поля.
     */
    public String getCar() {
        return get("car");
    }

    /**
     * Название поля идентификатора.
     *
     * @return название поля.
     */
    public String getId() {
        return get("id");
    }

    /**
     * Название поля названия.
     *
     * @return название поля.
     */
    public String getName() {
        return get("name");
    }

    /**
     * Название поля региона.
     *
     * @return название поля.
     */
    public String getRegion() {
        return get("region");
    }

    /**
     * Название поля города.
     *
     * @return название поля.
     */
    public String getCity() {
        return get("city");
    }

    /**
     * Название поля села.
     *
     * @return название поля.
     */
    public String getSettlement() {
        return get("settlement");
    }

    /**
     * Название поля жилого района.
     *
     * @return название поля.
     */
    public String getLivingArea() {
        return get("living_area");
    }

    /**
     * Название поля какго-либо площадного объекта.
     *
     * @return название поля.
     */
    public String getPlace() {
        return get("place");
    }

    /**
     * Название поля улицы.
     *
     * @return название поля.
     */
    public String getStreet() {
        return get("street");
    }

    /**
     * Название поля дома.
     *
     * @return название поля.
     */
    public String getHouse() {
        return get("house");
    }

    /**
     * Название поля широты.
     *
     * @return название поля.
     */
    public String getLatitude() {
        return get("latitude");
    }

    /**
     * Название поля долготы.
     *
     * @return название поля.
     */
    public String getLongitude() {
        return get("longitude");
    }

    /**
     * Название поля местоположения.
     *
     * @return название поля.
     */
    public String getLocation() {
        return get("location");
    }

    /**
     * Название поля запроса
     *
     * @return название поля.
     */
    public String getQuery() {
        return get("query");
    }

    /**
     * Название поля сегментов маршрута.
     *
     * @return название поля.
     */
    public String getSegments() {
        return get("segments");
    }

    /**
     * Название поля координат.
     *
     * @return название поля.
     */
    public String getCoordinates() {
        return get("coordinates");
    }

    /**
     * Название поля путевых точек.
     *
     * @return название поля.
     */
    public String getWaypoints() {
        return get("waypoints");
    }

    /**
     * Название поля расстояния.
     *
     * @return название поля.
     */
    public String getDistance() {
        return get("distance");
    }

    /**
     * Название поля времени.
     *
     * @return название поля.
     */
    public String getTime() {
        return get("time");
    }

    /**
     * Название поля сортировки.
     *
     * @return название поля.
     */
    public String getSort() {
        return get("sort");
    }

    /**
     * Название поля радиуса поиска.
     *
     * @return название поля.
     */
    public String getRadius() {
        return get("radius");
    }

    /**
     * Название поля координат центра экрана
     *
     * @return название поля.
     */
    public String getCenter() {
        return get("center");
    }

    /**
     * Field name of attribute groups.
     *
     * @return field name.
     */
    public String getAttributeGroups() {
        return get("attribute_groups");
    }

    /**
     * Field name of ex name.
     *
     * @return field name.
     */
    public String getNameEx() {
        return get("name_ex");
    }

    /**
     * Field name of paid parking.
     *
     * @return field name.
     */
    public String getIsPaid() {
        return get("is_paid");
    }

    /**
     * Field name of point.
     *
     * @return field name.
     */
    public String getPoint() {
        return get("point");
    }

    /**
     * Field name of full name of parking.
     *
     * @return field name.
     */
    public String getFullName() {
        return get("full_name");
    }

    /**
     * Field name of parking geometry.
     *
     * @return field name.
     */
    public String getGeometry() {
        return get("geometry");
    }

    /**
     * Field name of object ID.
     *
     * @return field name.
     */
    public String getObjectId() {
        return get("id");
    }

    /**
     * Field name of name of purpose.
     *
     * @return field name.
     */
    public String getPurposeName() {
        return get("purpose_name");
    }

    /**
     * Field name of subtype.
     *
     * @return field name.
     */
    public String getSubtype() {
        return get("subtype");
    }

    /**
     * Field name of bad point tolerance.
     *
     * @return field name.
     */
    public String getBadPointTolerance() {
        return get("bad_point_tolerance");
    }


    /**
     * Название поля.
     *
     * @param fieldName название поля.
     * @return название поля или null если не предусмотрено.
     */
    public String get(String fieldName) {
        return getOrDefault(fieldName, null);
    }
}
