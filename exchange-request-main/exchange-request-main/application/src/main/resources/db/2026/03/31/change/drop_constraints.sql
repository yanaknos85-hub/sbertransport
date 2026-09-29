-- Удаление ограничения chk_dangerous_class_valid из таблицы special_conditions

ALTER TABLE exchange_request.special_conditions
DROP CONSTRAINT IF EXISTS chk_dangerous_class_valid;

-- Скрипт: удаление обязательности (сделать nullable) полей length, width, height в таблице cargo_details

ALTER TABLE exchange_request.cargo_details
    ALTER COLUMN length DROP NOT NULL,
    ALTER COLUMN width DROP NOT NULL,
    ALTER COLUMN height DROP NOT NULL;