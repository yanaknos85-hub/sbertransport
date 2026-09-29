CREATE TABLE vehicle.attorney
(
    id               UUID                        PRIMARY KEY,
    telemechanic_id  UUID                        NOT NULL REFERENCES vehicle.employee (id),
    attorney_id      UUID                        NOT NULL,
    issue_date       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    expiry_date      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    creation_system  VARCHAR(150)
);


CREATE UNIQUE INDEX attorney_telemechanic_id_udx
ON vehicle.attorney (telemechanic_id, attorney_id);

CREATE INDEX attorney_telemechanic_id_idx
    ON vehicle.attorney USING hash(telemechanic_id);

comment on column vehicle.attorney.id is 'ID МЧД';
comment on column vehicle.attorney.telemechanic_id is 'ID телемеханика';
comment on column vehicle.attorney.attorney_id is 'Номер доверенности';
comment on column vehicle.attorney.issue_date is 'Дата выдачи';
comment on column vehicle.attorney.expiry_date is 'Дата окончания срока действия';
comment on column vehicle.attorney.creation_system is 'Система создания';