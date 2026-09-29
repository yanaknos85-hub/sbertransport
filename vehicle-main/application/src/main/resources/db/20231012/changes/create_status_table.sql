CREATE TABLE vehicle.status
(
    id    uuid PRIMARY KEY,
    title VARCHAR(255)
        CONSTRAINT status_title_uc UNIQUE NOT NULL
);

comment on column vehicle.status.id is 'Идентификатор статуса ТС';
comment on column vehicle.status.title is 'Наименование статуса ТС';