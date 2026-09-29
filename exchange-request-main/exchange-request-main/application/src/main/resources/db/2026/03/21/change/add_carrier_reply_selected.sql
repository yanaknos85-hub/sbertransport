ALTER TABLE exchange_request.request_carrier_reply
ADD COLUMN IF NOT EXISTS selected boolean default false;

-- Обновляем комментарий к колонке (опционально, для документации в БД)
COMMENT ON COLUMN exchange_request.request_carrier_reply.selected IS 'Признак выбора отклика (true - выбран, false - нет)';