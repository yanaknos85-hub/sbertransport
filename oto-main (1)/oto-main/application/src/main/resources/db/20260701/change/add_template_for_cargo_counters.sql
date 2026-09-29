ALTER TABLE oto_cargo.template_for_cargo
    ADD COLUMN IF NOT EXISTS count_requests INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS count_requests_in_route INTEGER DEFAULT 0;

COMMENT ON COLUMN oto_cargo.template_for_cargo.count_requests IS 'Количество заявок созданных целиком по расписанию (из request.is_template = true и humanReadableId = template.humanReadableId)';
COMMENT ON COLUMN oto_cargo.template_for_cargo.count_requests_in_route IS 'Количество заявок в рамках одного маршрута (шаблона), создаваемого по расписанию';

