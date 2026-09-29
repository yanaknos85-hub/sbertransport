CREATE TABLE if NOT EXISTS vehicle.accessible_position (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);
CREATE INDEX if NOT EXISTS idx_accessible_position_title ON vehicle.accessible_position(title);

comment on table vehicle.accessible_position is 'Справочник должностей';
comment on column vehicle.accessible_position.id is 'Идентификатор записи';
comment on column vehicle.accessible_position.title is 'Наименование записи';


