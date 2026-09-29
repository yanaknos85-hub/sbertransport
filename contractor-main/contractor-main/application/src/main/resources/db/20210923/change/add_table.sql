ALTER TABLE contractors.attribute
  ADD COLUMN status VARCHAR(256);
ALTER TABLE contractors.trip_service
  ADD COLUMN status VARCHAR(256);
CREATE TABLE contractors.driver_license(
    id              UUID PRIMARY KEY,
    driver          UUID,
    license_class   VARCHAR(256)
);
CREATE TABLE contractors.driver_attribute(
    driver          UUID,
    attribute       UUID
);
CREATE TABLE contractors.trip_service_attribute(
    trip_service    UUID,
    attribute       UUID
);

COMMENT ON COLUMN contractors.attribute.status IS 'Признак активности';
COMMENT ON COLUMN contractors.trip_service.status IS 'Признак активности';

COMMENT ON COLUMN contractors.driver_license.driver IS 'Водитель';
COMMENT ON COLUMN contractors.driver_license.license_class IS 'Право управления видом ТС';

COMMENT ON COLUMN contractors.driver_attribute.driver IS 'Водитель';
COMMENT ON COLUMN contractors.driver_attribute.attribute IS 'Признак водителя';

COMMENT ON COLUMN contractors.trip_service_attribute.trip_service IS 'Класс поездки';
COMMENT ON COLUMN contractors.trip_service_attribute.attribute IS 'Признак водителя';