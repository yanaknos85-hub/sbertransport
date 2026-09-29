-- =============================================
-- Таблица: request_carrier_reply
-- Назначение: хранение откликов для заявок
-- Вид: основная сущность биржи грузоперевозок
-- =============================================
CREATE TABLE IF NOT EXISTS exchange_request.request_carrier_reply (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID NOT NULL,
    reply JSONB,
    organization_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ================================
-- Комментарии к таблице и столбцам
-- ================================

COMMENT ON TABLE exchange_request.request_carrier_reply IS 'Отклики грузоперевозчиков для заявок';

COMMENT ON COLUMN exchange_request.request_carrier_reply.id IS 'Уникальный идентификатор отклика (UUID)';
COMMENT ON COLUMN exchange_request.request_carrier_reply.request_id IS 'Заявка, к которой относится отклик';
COMMENT ON COLUMN exchange_request.request_carrier_reply.reply IS 'Детали отклика';
COMMENT ON COLUMN exchange_request.request_carrier_reply.organization_id IS 'Идентификатор организации перевозчика';
COMMENT ON COLUMN exchange_request.request_carrier_reply.created_at IS 'Дата и время создания записи в базе данных';
-- ===============
-- Индексы
-- ===============

-- Индекс по организации
CREATE INDEX if not exists idx_exchange_request_reply_organization_id ON exchange_request.request_carrier_reply(organization_id);

-- Индекс по заявке
CREATE INDEX if not exists idx_exchange_request_reply_request_id ON exchange_request.request_carrier_reply(request_id);

-- Добавляем новые поля в таблицу exchange_request.request

ALTER TABLE exchange_request.request
ADD COLUMN IF NOT EXISTS carrier_organization_id UUID,
ADD COLUMN IF NOT EXISTS carrier_info JSONB;

-- Опционально: добавляем комментарии к новым полям
COMMENT ON COLUMN exchange_request.request.carrier_organization_id IS 'Организация-перевозчик, назначенная по заявке';
COMMENT ON COLUMN exchange_request.request.carrier_info IS 'Информация о перевозчике (авто, водитель и т.д.) в формате JSONB';

-- Опционально: добавляем индекс по полю carrier_organization_id для ускорения поиска
CREATE INDEX IF NOT EXISTS idx_exchange_request_carrier_org_id ON exchange_request.request(carrier_organization_id);

-- Удаляем старое ограничение CHECK для колонки status
ALTER TABLE exchange_request.request
DROP CONSTRAINT IF EXISTS request_status_check;
