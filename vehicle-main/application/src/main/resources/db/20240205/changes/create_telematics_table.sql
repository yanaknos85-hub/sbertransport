CREATE TABLE vehicle.telematics
(
    id    uuid PRIMARY KEY,
    imei  VARCHAR(20) CONSTRAINT telematics_imei_uc UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL
);
comment on table vehicle.telematics is 'Телематика';
comment on column vehicle.telematics.id is 'ID телематики';
comment on column vehicle.telematics.imei is 'IME-номер Телематики';
comment on column vehicle.telematics.title is 'Наименование Телематики';