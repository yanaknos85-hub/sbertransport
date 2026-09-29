ALTER TABLE tariff.tariff
    ADD COLUMN IF NOT EXISTS
        humanReadableId varchar(100);
COMMENT ON COLUMN tariff.tariff.humanreadableid is 'Уникальный идентификатор (человекочитаемый ID)';

alter table tariff.tariff
    add constraint ux_department_1 unique (humanReadableId);