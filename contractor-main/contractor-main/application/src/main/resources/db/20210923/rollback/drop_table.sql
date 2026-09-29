ALTER TABLE contractors.attribute
    RENAME TO driver_tag;
ALTER TABLE contractors.trip_service
    RENAME TO trip_classes;
ALTER TABLE contractors.driver
    add COLUMN autopark_id uuid;
ALTER TABLE contractors.driver
    RENAME COLUMN service_license_number TO service_provider_license_number;
ALTER TABLE contractors.car_parameters
    RENAME COLUMN trip_service TO trip_class_id;
create TABLE contractors.driver_tag_connector
(
    TAG_ID    UUID NOT NULL,
    DRIVER_ID UUID NOT NULL,
    PRIMARY KEY (TAG_ID, DRIVER_ID),
    foreign key (TAG_ID) references CONTRACTORS.DRIVER_TAG (id),
    foreign key (DRIVER_ID) references CONTRACTORS.DRIVER (id)
);
create TABLE contractors.driver_tag_to_handbook
(
    trip_class_id  uuid,
    tag_id         uuid,
    primary key (trip_class_id, tag_id)
);
create TABLE contractors.driver_license_classes
(
    DRIVER_ID UUID       NOT NULL REFERENCES contractors.driver (id),
    CLASS     VARCHAR(3) NOT NULL,
    PRIMARY KEY (DRIVER_ID, CLASS)
);
