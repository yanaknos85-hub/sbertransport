create table contractors.trip_classes (
    id              uuid,
    trip_class      varchar(255),
    contractor_id   uuid,
    primary key (id)
);

alter table contractors.trip_classes add constraint contractor_trip_class foreign key (contractor_id) references contractors.contractor;

COMMENT ON TABLE contractors.trip_classes IS 'Класс поездки';
COMMENT ON COLUMN contractors.trip_classes.id IS 'Идентификатор класса поездки';
COMMENT ON COLUMN contractors.trip_classes.trip_class IS 'Класс поездки';
COMMENT ON COLUMN contractors.trip_classes.contractor_id IS 'Контрагент';