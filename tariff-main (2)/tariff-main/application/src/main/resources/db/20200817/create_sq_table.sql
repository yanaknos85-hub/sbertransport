CREATE TABLE IF NOT EXISTS tariff.company_sq
(
    id         uuid primary key,
    prefix     varchar(2)  not null,
    orgDigitId numeric     not null,
    sq         numeric     not null,
    dt_insert  TIMESTAMPTZ not null DEFAULT NOW(),
    dt_modify  TIMESTAMPTZ not null DEFAULT NOW()
);
COMMENT ON TABLE tariff.company_sq is 'Таблица для формирования последовательностей для компаний';
COMMENT ON COLUMN tariff.company_sq.id is 'Уникальный идентификатор (первичный ключ)';
COMMENT ON COLUMN tariff.company_sq.prefix is 'Кодовое обозначение типа сущности';
COMMENT ON COLUMN tariff.company_sq.orgDigitId is 'ID организации (числовой)';
COMMENT ON COLUMN tariff.company_sq.sq is 'Порядковый номер (в рамках клиента)';
COMMENT ON COLUMN tariff.company_sq.dt_insert is 'Дата время вставки записи';
COMMENT ON COLUMN tariff.company_sq.dt_modify is 'Дата время модификации записи';

CREATE UNIQUE INDEX ux_company_sq_1 ON tariff.company_sq (prefix, orgDigitId, sq);
CREATE UNIQUE INDEX ux_company_sq_2 ON tariff.company_sq (prefix, orgDigitId);