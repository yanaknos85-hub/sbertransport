ALTER TABLE exchange_request.organization
ADD COLUMN IF NOT EXISTS contact_phone varchar(20);

-- Обновляем комментарий к колонке (опционально, для документации в БД)
COMMENT ON COLUMN exchange_request.organization.contact_phone IS 'Контактный телефон';