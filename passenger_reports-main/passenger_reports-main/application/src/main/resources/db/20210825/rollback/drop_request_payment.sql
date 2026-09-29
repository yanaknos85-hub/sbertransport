ALTER TABLE reports.request
    DROP COLUMN IF EXISTS payment_type_code_main,
    DROP COLUMN IF EXISTS payment_price_main,
    DROP COLUMN IF EXISTS payment_type_code_optional,
    DROP COLUMN IF EXISTS payment_price_optional;
    DROP COLUMN IF EXISTS employee_driver_id;