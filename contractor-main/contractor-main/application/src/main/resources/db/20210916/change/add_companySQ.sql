CREATE TABLE IF NOT EXISTS contractors.company_sq (
    id        uuid primary key,
    prefix    varchar(2)  not null,
    orgDigitId  numeric     not null,
    sq        numeric     not null,
    dt_insert TIMESTAMPTZ not null DEFAULT NOW(),
    dt_modify TIMESTAMPTZ not null DEFAULT NOW()
);
COMMENT ON TABLE contractors.company_sq is 'Таблица для формирования последовательностей для компаний';
COMMENT ON COLUMN contractors.company_sq.id is 'Уникальный идентификатор (первичный ключ)';
COMMENT ON COLUMN contractors.company_sq.prefix is 'Кодовое обозначение типа сущности';
COMMENT ON COLUMN contractors.company_sq.orgDigitId is 'ID организации (числовой)';
COMMENT ON COLUMN contractors.company_sq.sq is 'Порядковый номер (в рамках клиента)';
COMMENT ON COLUMN contractors.company_sq.dt_insert is 'Дата время вставки записи';
COMMENT ON COLUMN contractors.company_sq.dt_modify is 'Дата время модификации записи';

CREATE UNIQUE INDEX ux_company_sq_1 ON contractors.company_sq (prefix, orgDigitId, sq);
CREATE UNIQUE INDEX ux_company_sq_2 ON contractors.company_sq (prefix, orgDigitId);