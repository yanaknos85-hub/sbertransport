CREATE TABLE vehicle.wheel_size
(
    id    uuid PRIMARY KEY,
    title VARCHAR(20)
        CONSTRAINT wheel_size_title_uc UNIQUE NOT NULL
);
comment on table vehicle.wheel_size is 'Размер колеса ТС';
comment on column vehicle.wheel_size.id is 'Идентификатор Размера колеса ТС';
comment on column vehicle.wheel_size.title is 'Наименование Размера колеса ТС';