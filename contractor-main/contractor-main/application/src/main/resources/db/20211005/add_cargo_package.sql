CREATE TABLE contractors.cargo_package
(
    id         uuid PRIMARY KEY,
    label      varchar(128)         NOT NULL,
    cost       double precision     NOT NULL,
    unit       varchar(128)         not null,
    active     boolean default true not null,
    contractor uuid
        CONSTRAINT cargo_package_contractor_fk REFERENCES contractors.contractor (id)
);

comment on table contractors.cargo_package is 'Справочник упаковочных материалов';
comment on column contractors.cargo_package.id is 'Идентификатор';
comment on column contractors.cargo_package.label is 'Наименование упаковки';
comment on column contractors.cargo_package.cost is 'Стоимость, руб';
comment on column contractors.cargo_package.unit is 'Единица измерения';
comment on column contractors.cargo_package.active is 'Признак активной записи';
comment on column contractors.cargo_package.contractor is 'Идентификатор контрагента';