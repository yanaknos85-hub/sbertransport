CREATE TABLE vehicle.fuel_type
(
    id    uuid PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    engine_type_id uuid REFERENCES vehicle.engine_type NOT NULL,
    CONSTRAINT fuel_type_title_engine_type_id_idx UNIQUE (title, engine_type_id)
);
comment on table vehicle.fuel_type is 'Вид топлива для транспортного средства';
comment on column vehicle.fuel_type.id is 'ИД Вида топлива ТС';
comment on column vehicle.fuel_type.title is 'Наименование Вида топлива ТС';
comment on column vehicle.fuel_type.engine_type_id is 'Тип двигателя ТС';