-- Добавление поля is_sender_forwarder в таблицу exchange_request.request

ALTER TABLE exchange_request.request
ADD COLUMN IF NOT EXISTS is_sender_forwarder BOOLEAN NOT NULL DEFAULT FALSE;

-- Добавление комментария к колонке
COMMENT ON COLUMN exchange_request.request.is_sender_forwarder IS 'Признак экспедитора у грузоотправителя. Указывает, является ли отправитель (sender) экспедитором. По умолчанию — false.';

-- Добавление колонки occupied_places_count в таблицу cargo_details с DEFAULT = 1
ALTER TABLE exchange_request.cargo_details
ADD COLUMN IF NOT EXISTS occupied_places_count INTEGER DEFAULT 1;

-- Добавление CHECK-ограничения: значение должно быть больше 0
ALTER TABLE exchange_request.cargo_details
ADD CONSTRAINT chk_occupied_places_count_positive
CHECK (occupied_places_count IS NULL OR occupied_places_count > 0);

-- Комментарий к колонке occupied_places_count
COMMENT ON COLUMN exchange_request.cargo_details.occupied_places_count IS 'Количество грузовых мест. Должно быть больше 0. По умолчанию — 1.';

-- Добавление колонки type_of_container в таблицу cargo_details с ограничением NOT NULL и DEFAULT = '00'

-- Добавление новой колонки type_of_container как JSONB для хранения массива строк
ALTER TABLE exchange_request.cargo_details
ADD COLUMN IF NOT EXISTS type_of_container JSONB NOT NULL DEFAULT '["00"]';

-- Комментарий к новой колонке type_of_container
COMMENT ON COLUMN exchange_request.cargo_details.type_of_container IS 'Вид тары — массив строк (например, ["box", "pallet"]). Значение выбирается из справочника. По умолчанию — ["00"].';