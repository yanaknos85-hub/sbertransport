ALTER TABLE vehicle.transport
    DROP COLUMN year;

ALTER TABLE vehicle.transport
    ADD year INT NOT NULL;

comment on column vehicle.transport.year is 'Год выпуска';
