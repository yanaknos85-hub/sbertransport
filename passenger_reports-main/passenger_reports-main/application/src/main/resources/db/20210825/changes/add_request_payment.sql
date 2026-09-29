ALTER TABLE reports.request
    ADD COLUMN IF NOT EXISTS payment_type_code_main INTEGER,
    ADD COLUMN IF NOT EXISTS payment_price_main BIGINT,
    ADD COLUMN IF NOT EXISTS payment_type_code_optional INTEGER,
    ADD COLUMN IF NOT EXISTS payment_price_optional BIGINT,
    ADD COLUMN IF NOT EXISTS employee_driver_id uuid;

COMMENT ON COLUMN reports.request.payment_type_code_main is 'Код вида основной оплаты';
COMMENT ON COLUMN reports.request.payment_price_main is 'Сумма основной оплаты, коп';
COMMENT ON COLUMN reports.request.payment_type_code_optional is 'Код вида дополнительной оплаты';
COMMENT ON COLUMN reports.request.payment_price_optional is 'Сумма дополнительной оплаты, коп';
COMMENT ON COLUMN reports.request.employee_driver_id is 'Водитель личного транспорта';