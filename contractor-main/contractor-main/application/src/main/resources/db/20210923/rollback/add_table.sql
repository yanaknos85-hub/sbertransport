ALTER TABLE contractors.driver_tag
    drop COLUMN status;
ALTER TABLE contractors.trip_classes
    drop COLUMN status;
drop TABLE contractors.driver_license;
drop TABLE contractors.driver_attribute;
drop TABLE contractors.trip_service_attribute;