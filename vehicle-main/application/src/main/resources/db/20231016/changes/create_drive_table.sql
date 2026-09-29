CREATE TABLE vehicle.drive
(
    id    uuid PRIMARY KEY,
    title VARCHAR(255)
        CONSTRAINT drive_title_uc UNIQUE NOT NULL
);
comment on table vehicle.drive is 'Привод транспортного средства';
comment on column vehicle.drive.id is 'Идентификатор привода ТС';
comment on column vehicle.drive.title is 'Наименование привода ТС';