-- Добавление поля methodDeterminingMass в таблицу cargo_details

ALTER TABLE exchange_request.cargo_details
ADD COLUMN IF NOT EXISTS method_determining_mass JSONB;

-- Обновляем комментарий к колонке (опционально, для документации в БД)
COMMENT ON COLUMN exchange_request.cargo_details.method_determining_mass IS 'Метод определения массы груза. Значение выбирается из справочника.';