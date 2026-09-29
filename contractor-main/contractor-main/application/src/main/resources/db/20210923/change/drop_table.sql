DROP TABLE contractors.driver_tag_connector;
DROP TABLE contractors.driver_tag_to_handbook;
DROP TABLE contractors.driver_license_classes;
ALTER TABLE contractors.driver_tag
  RENAME TO attribute;
ALTER TABLE contractors.trip_classes
  RENAME TO trip_service;
ALTER TABLE contractors.driver
  DROP COLUMN autopark_id;
ALTER TABLE contractors.driver
  RENAME COLUMN service_provider_license_number TO service_license_number;
ALTER TABLE contractors.car_parameters
  RENAME COLUMN trip_class_id TO trip_service;
