-- Удаление полей oversized_length, oversized_width, oversized_height из таблицы special_conditions

-- 1. Удаляем связанные CHECK-ограничения (если ещё не удалены)
ALTER TABLE exchange_request.special_conditions
DROP CONSTRAINT IF EXISTS chk_oversized_requires_flag;

-- 2. Удаляем сами колонки
ALTER TABLE exchange_request.special_conditions
DROP COLUMN IF EXISTS oversized_length,
DROP COLUMN IF EXISTS oversized_width,
DROP COLUMN IF EXISTS oversized_height;

-- 3. Обновляем комментарий к таблице (опционально, для документации)
COMMENT ON TABLE exchange_request.special_conditions IS 'Особые условия перевозки. Удалены поля: oversized_length, oversized_width, oversized_height.';