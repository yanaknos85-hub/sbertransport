create table contractors.driver_tag_to_handbook (
    trip_class_id  uuid,
    tag_id         uuid,
    primary key (trip_class_id, tag_id)
);

COMMENT ON TABLE contractors.driver_tag_to_handbook is 'Таблица связей: признак водителя - класс поездки';