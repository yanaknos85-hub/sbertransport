CREATE TABLE vehicle.model
(
    id    uuid PRIMARY KEY,
    title VARCHAR(255)
        CONSTRAINT model_title_uc UNIQUE NOT NULL,
    brand_id uuid REFERENCES vehicle.brand NOT NULL
);
comment on table vehicle.model is 'Модель транспортного средства';
comment on column vehicle.model.id is 'ИД модели ТС';
comment on column vehicle.model.title is 'Наименование модели ТС';
comment on column vehicle.model.brand_id is 'Марка модели ТС';