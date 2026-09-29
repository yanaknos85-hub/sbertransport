create schema IF NOT EXISTS exchange_request;
comment on schema exchange_request is 'Заявки для биржи';

-- Создание таблицы для генерации human-readable ID с поддержкой нескольких префиксов
-- Пример формата: ОР-202504-00000001, АУК-202504-00000001

CREATE TABLE IF NOT EXISTS exchange_request.humanreadable_id_counter (
    year_month VARCHAR(6) NOT NULL,        -- Год и месяц в формате ГГГГММ (например, 202504)
    prefix     VARCHAR(10) NOT NULL,    -- Префикс типа заявки: ОР, АУК, ФРХ и др.
    next_val   BIGINT NOT NULL DEFAULT 1, -- Следующее значение счётчика (начинается с 1)
    PRIMARY KEY (year_month, prefix)    -- Уникальность по комбинации месяца и префикса
);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE exchange_request.humanreadable_id_counter IS 'Счётчик для генерации human-readable ID заявок. Поддерживает независимые последовательности для разных префиксов (ОР, АУК и др.) в пределах месяца.';

COMMENT ON COLUMN exchange_request.humanreadable_id_counter.year_month IS 'Месяц в формате ГГГГММ. Определяет период действия счётчика. При смене месяца счётчик начинается с 1.';
COMMENT ON COLUMN exchange_request.humanreadable_id_counter.prefix IS 'Префикс типа заявки (например, ОР — ордер, АУК — аукцион, ФРХ — фрахт). Используется как часть human-readable ID.';
COMMENT ON COLUMN exchange_request.humanreadable_id_counter.next_val IS 'Текущее значение счётчика. Увеличивается на 1 при каждом использовании. Начальное значение — 1.';


-- =============================================
-- Таблица: requests
-- Назначение: хранение заявок на перевозку груза
-- Вид: основная сущность биржи грузоперевозок
-- =============================================
CREATE TABLE IF NOT EXISTS exchange_request.request (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Человекочитаемый номер: ОП-ГГГГММ-ННННННН
    humanreadable_id VARCHAR(50) NOT NULL UNIQUE,

    -- Внутренний ID от грузовладельца
    internal_id VARCHAR(100),

    -- Ссылка на пользователя-создателя (владельца заявки)
    owner_id UUID NOT NULL,

    -- Статус заявки
    -- Колонка status в таблице exchange_request.request
status VARCHAR(50) NOT NULL DEFAULT 'DRAFT'
    CHECK (status IN (
        'DRAFT',
        'PUBLISHED',
        'CARRIER_SELECTED',
        'AWAITING_CARRIER_CONFIRMATION',
        'IN_EXECUTION',
        'CARRIER_DEPARTED_TO_LOAD',
        'CARRIER_ARRIVED_AT_LOAD',
        'LOADING_AT_PROGRESS',
        'CARGO_TRANSFERRED',
        'CARRIER_DEPARTED_TO_DELIVERY',
        'CARRIER_ARRIVED_AT_DELIVERY',
        'UNLOADING_IN_PROGRESS',
        'AWAITING_RECEIVER_CONFIRMATION',
        'CARGO_DELIVERED',
        'AWAITING_CARRIER_SIGNATURE',
        'AWAITING_SHIPPER_CONFIRMATION',
        'PAYMENT_CONFIRMATION',
        'PAID',
        'RATING',
        'ARCHIVED',
        'CANCELLED',
        'ERROR'
    )),

    -- Использование ЭТрН
    use_etrn BOOLEAN NOT NULL DEFAULT TRUE,

    -- Вид заявки для перевозчиков
    view_type VARCHAR(50)
        CHECK (view_type IN ('FIXED')),  -- MVP: ТОЛЬКО ФИКС. ДАЛЕЕ: AUCTION, TENDER И Т.Д.

    -- Форма оплаты
    payment_form VARCHAR(50)
        CHECK (payment_form IN ('CASH', 'NON_CASH')),

    -- Условия оплаты
    payment_terms VARCHAR(100)
        CHECK (payment_terms IN ('PREPAYMENT', 'ON_DELIVERY', 'DEFERRED_PAYMENT')),

    -- Срок оплаты в днях (до 30)
    payment_days INTEGER,

    -- Дата создания в системе заказчика
    request_created DATE,

    -- Контакты отправителя
    sender_fio VARCHAR(255),
    sender_phone VARCHAR(20),

    -- Контакты получателя
    recipient_fio VARCHAR(255),
    recipient_phone VARCHAR(20),

    -- Системные временные метки
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,

    draft_info JSONB,

    cost_request DOUBLE PRECISION,
    vat_include BOOLEAN
);

-- ================================
-- Комментарии к таблице и столбцам
-- ================================

COMMENT ON TABLE exchange_request.request IS 'Заявки на перевозку груза в рамках биржи';

COMMENT ON COLUMN exchange_request.request.id IS 'Уникальный идентификатор заявки (UUID)';
COMMENT ON COLUMN exchange_request.request.humanreadable_id IS 'Внутренний номер заказа: формат ОП-ГГГГММ-ННННННН, например ОП-202601-0000001';
COMMENT ON COLUMN exchange_request.request.internal_id IS 'Номер заказа в системе грузовладельца. Допустимы любые символы, кроме <>?@#${}[]*()!~`'';:,&%';
COMMENT ON COLUMN exchange_request.request.owner_id IS 'Ссылка на пользователя-создателя заявки (внешний ключ на users.id)';
COMMENT ON COLUMN exchange_request.request.status IS 'Текущий статус заявки: cargo_draft (черновик), cargo_published (опубликована), cargo_archived (завершена)';
COMMENT ON COLUMN exchange_request.request.use_etrn IS 'Флаг использования Электронного Товарно-Расходного Накладного (ЭТрН): TRUE/FALSE';
COMMENT ON COLUMN exchange_request.request.view_type IS 'Вид заявки для перевозчиков: fixed (фикс), далее могут быть auction, tender и др.';
COMMENT ON COLUMN exchange_request.request.payment_form IS 'Форма оплаты: cash (наличные), non_cash (безнал)';
COMMENT ON COLUMN exchange_request.request.payment_terms IS 'Условия оплаты: prepayment (предоплата), on_delivery (по факту), deferred_payment (отсрочка)';
COMMENT ON COLUMN exchange_request.request.payment_days IS 'Срок оплаты в днях (1–30). Заполняется только при условиях "отсрочка")';
COMMENT ON COLUMN exchange_request.request.request_created IS 'Дата создания заявки в системе заказчика (без времени)';
COMMENT ON COLUMN exchange_request.request.sender_fio IS 'ФИО контактного лица отправителя';
COMMENT ON COLUMN exchange_request.request.sender_phone IS 'Телефон отправителя';
COMMENT ON COLUMN exchange_request.request.recipient_fio IS 'ФИО контактного лица получателя';
COMMENT ON COLUMN exchange_request.request.recipient_phone IS 'Телефон получателя';
COMMENT ON COLUMN exchange_request.request.created_at IS 'Дата и время создания записи в базе данных';
COMMENT ON COLUMN exchange_request.request.updated_at IS 'Дата и время последнего обновления записи';
COMMENT ON COLUMN exchange_request.request.expires_at IS 'Дата автоматического удаления черновика: created_at + 30 дней';
COMMENT ON COLUMN exchange_request.request.published_at IS 'Дата и время публикации заявки';
COMMENT ON COLUMN exchange_request.request.completed_at IS 'Дата и время завершения заявки';
COMMENT ON COLUMN exchange_request.request.draft_info IS 'Информация о черновике в статусе DRAFT';
-- ===============
-- Индексы
-- ===============

-- Индекс по owner_id для быстрого поиска заявок пользователя
CREATE INDEX if not exists idx_exchange_request_owner_id ON exchange_request.request(owner_id);

-- Индекс по статусу
CREATE INDEX if not exists idx_exchange_request_status ON exchange_request.request(status);

-- Индекс по дате публикации
CREATE INDEX if not exists idx_requests_published_at ON exchange_request.request(published_at);

-- Индекс по человекочитаемому ID
CREATE INDEX if not exists idx_requests_humanreadable_id ON exchange_request.request(humanreadable_id);


CREATE TABLE IF NOT EXISTS exchange_request.organization (
    id UUID PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    inn VARCHAR(12) NOT NULL,
    kpp VARCHAR(9),
    legal_address VARCHAR(500),
    bank_account VARCHAR(20),
    bank_name VARCHAR(200),
    bic VARCHAR(9)
);

CREATE UNIQUE INDEX if not exists idx_organization_inn ON exchange_request.organization(inn) WHERE kpp IS NULL;

COMMENT ON TABLE exchange_request.organization IS 'Организации, указанные в заявках на обмен';
COMMENT ON COLUMN exchange_request.organization.id IS 'Уникальный идентификатор организации (автоматически генерируемый UUID)';
COMMENT ON COLUMN exchange_request.organization.name IS 'Полное наименование организации (до 500 символов)';
COMMENT ON COLUMN exchange_request.organization.inn IS 'Идентификационный номер налогоплательщика (ИНН), 10 или 12 цифр';
COMMENT ON COLUMN exchange_request.organization.kpp IS 'Код причины постановки на учёт (КПП), может отсутствовать';
COMMENT ON COLUMN exchange_request.organization.legal_address IS 'Юридический адрес организации (до 500 символов), может отсутствовать';
COMMENT ON COLUMN exchange_request.organization.bank_account IS 'Расчетный счёт организации в банке (до 20 символов), может отсутствовать';
COMMENT ON COLUMN exchange_request.organization.bank_name IS 'Наименование банка (до 200 символов), может отсутствовать';
COMMENT ON COLUMN exchange_request.organization.bic IS 'Банковский идентификационный код (БИК), 9 цифр, может отсутствовать';

-- Файл: V2__create_waypoint_table.sql
-- Создание таблицы waypoint в схеме exchange_request

CREATE TABLE IF NOT EXISTS exchange_request.waypoint (
    id UUID PRIMARY KEY,
    ordering_index INTEGER NOT NULL,
    radius INTEGER NOT NULL,
    request_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    address_info JSONB NOT NULL,
    date_from TIMESTAMP WITH TIME ZONE NOT NULL,
    date_to TIMESTAMP WITH TIME ZONE NOT NULL,
    contact_info JSONB,
    -- Внешний ключ на таблицу request
    CONSTRAINT fk_waypoint_request_id
        FOREIGN KEY (request_id)
        REFERENCES exchange_request.request(id)
        ON DELETE CASCADE
);


CREATE INDEX IF NOT EXISTS idx_waypoint_request_id ON exchange_request.waypoint(request_id);

COMMENT ON TABLE exchange_request.waypoint IS 'Точки маршрута: погрузки и выгрузки для заявок на перевозку';

COMMENT ON COLUMN exchange_request.waypoint.id IS 'Уникальный идентификатор точки маршрута. Первичный ключ.';
COMMENT ON COLUMN exchange_request.waypoint.ordering_index IS 'Порядковый номер точки в маршруте, начиная с 0.';
COMMENT ON COLUMN exchange_request.waypoint.radius IS 'Радиус поиска в метрах вокруг точки.';
COMMENT ON COLUMN exchange_request.waypoint.request_id IS 'Ссылка на заявку. Внешний ключ на exchange_request.request.id.';
COMMENT ON COLUMN exchange_request.waypoint.type IS 'Тип операции: LOAD — погрузка, UNLOAD — выгрузка.';
COMMENT ON COLUMN exchange_request.waypoint.address_info IS 'JSONB с информацией об адресе: город, улица, координаты и т.д.';
COMMENT ON COLUMN exchange_request.waypoint.date_from IS 'Начало временного окна для выполнения операции.';
COMMENT ON COLUMN exchange_request.waypoint.date_to IS 'Конец временного окна для выполнения операции.';
COMMENT ON COLUMN exchange_request.waypoint.contact_info IS 'JSONB с контактной информацией: ФИО, телефон, email и организация. Может быть null, если контакт не указан.';

-- Создание таблицы cargo_details в схеме exchange_request
CREATE TABLE IF NOT EXISTS exchange_request.cargo_details (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL UNIQUE,
    weight_kg DECIMAL(10,2) NOT NULL,
    volume_m3 DECIMAL(10,3) NOT NULL,
    declared_value DECIMAL(15,2) NOT NULL,
    length DECIMAL(5,2) NOT NULL,
    width DECIMAL(5,2) NOT NULL,
    height DECIMAL(5,2) NOT NULL,
    cargo_type VARCHAR(100) NOT NULL,
    cargo_package VARCHAR(100) NOT NULL,

    -- Внешний ключ на заявку
    CONSTRAINT fk_cargo_details_request_id
        FOREIGN KEY (request_id)
        REFERENCES exchange_request.request(id)
        ON DELETE CASCADE
);

-- Индекс по request_id (уже покрыт UNIQUE, но может быть полезен для JOIN)
CREATE INDEX IF NOT EXISTS idx_cargo_details_request_id ON exchange_request.cargo_details(request_id);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE exchange_request.cargo_details IS 'Данные о грузе: вес, объем, габариты, стоимость, тип и упаковка';

COMMENT ON COLUMN exchange_request.cargo_details.id IS 'Уникальный идентификатор записи о грузе. Первичный ключ.';
COMMENT ON COLUMN exchange_request.cargo_details.request_id IS 'Ссылка на заявку. Уникальный внешний ключ (1:1).';
COMMENT ON COLUMN exchange_request.cargo_details.weight_kg IS 'Вес груза в килограммах. Ограничения: от 1 до 20 000 кг.';
COMMENT ON COLUMN exchange_request.cargo_details.volume_m3 IS 'Объём груза в кубометрах. Ограничения: от 1 до 90 м³.';
COMMENT ON COLUMN exchange_request.cargo_details.declared_value IS 'Объявленная стоимость груза в рублях. Максимум — 1 млрд руб.';
COMMENT ON COLUMN exchange_request.cargo_details.length IS 'Длина груза в метрах. Ограничения: от 2 до 13 м.';
COMMENT ON COLUMN exchange_request.cargo_details.width IS 'Ширина груза в метрах.';
COMMENT ON COLUMN exchange_request.cargo_details.height IS 'Высота груза в метрах.';
COMMENT ON COLUMN exchange_request.cargo_details.cargo_type IS 'Тип груза. Значение из справочника (cargo_type.id).';
COMMENT ON COLUMN exchange_request.cargo_details.cargo_package IS 'Тип упаковки. Значение из справочника (packages_type.id).';
-- Создание таблицы vehicle_requirements в схеме exchange_request

CREATE TABLE IF NOT EXISTS exchange_request.vehicle_requirements (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL UNIQUE,
    load_type VARCHAR(50) NOT NULL,
    unload_type VARCHAR(50) NOT NULL,
    capacity_m3 DECIMAL(10,3) NOT NULL CHECK (capacity_m3 > 0 AND capacity_m3 <= 90),
    load_capacity DECIMAL(10,3) NOT NULL CHECK (load_capacity > 0 AND load_capacity <= 20),
    no_additional_load BOOLEAN NOT NULL,
    vehicle_body_type VARCHAR(100) NOT NULL,
    vehicle_extra_features VARCHAR(100) NOT NULL,
    comment TEXT,

    -- Внешний ключ на заявку
    CONSTRAINT fk_vehicle_requirements_request_id
        FOREIGN KEY (request_id)
        REFERENCES exchange_request.request(id)
        ON DELETE CASCADE
);

-- Индекс по request_id (уже покрыт UNIQUE, но полезен для JOIN и производительности)
CREATE INDEX IF NOT EXISTS idx_vehicle_requirements_request_id ON exchange_request.vehicle_requirements(request_id);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE exchange_request.vehicle_requirements IS 'Требования к транспорту: тип загрузки/выгрузки, объём, грузоподъёмность, тип кузова, доп. опции и комментарии';

COMMENT ON COLUMN exchange_request.vehicle_requirements.id IS 'Уникальный идентификатор записи с требованиями к транспорту. Первичный ключ.';
COMMENT ON COLUMN exchange_request.vehicle_requirements.request_id IS 'Ссылка на заявку. Уникальный внешний ключ (1:1).';
COMMENT ON COLUMN exchange_request.vehicle_requirements.load_type IS 'Тип загрузки: top (верхняя), side (боковая), rear (задняя).';
COMMENT ON COLUMN exchange_request.vehicle_requirements.unload_type IS 'Тип выгрузки: top (верхняя), side (боковая), rear (задняя).';
COMMENT ON COLUMN exchange_request.vehicle_requirements.capacity_m3 IS 'Требуемый объём кузова в кубометрах. Ограничения: от 1 до 90 м³.';
COMMENT ON COLUMN exchange_request.vehicle_requirements.load_capacity IS 'Требуемая грузоподъёмность в тоннах. Ограничения: от 0.001 до 20 тонн.';
COMMENT ON COLUMN exchange_request.vehicle_requirements.no_additional_load IS 'Признак запрета на догрузку: TRUE — без догрузки, FALSE — можно догружать.';
COMMENT ON COLUMN exchange_request.vehicle_requirements.vehicle_body_type IS 'Тип кузова (например, тент, рефрижератор). Значение из справочника vehicle_body_types.id.';
COMMENT ON COLUMN exchange_request.vehicle_requirements.vehicle_extra_features IS 'Дополнительные опции (например, манипулятор, подогрев). Значение из справочника vehicle_extra_features.id.';
COMMENT ON COLUMN exchange_request.vehicle_requirements.comment IS 'Произвольный комментарий к требованиям по транспорту. Поле необязательное.';

-- Создание таблицы special_conditions в схеме exchange_request

CREATE TABLE IF NOT EXISTS exchange_request.special_conditions (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL UNIQUE,
    is_dangerous BOOLEAN,
    dangerous_class VARCHAR(10),
    has_temperature BOOLEAN,
    temp_min SMALLINT,
    temp_max SMALLINT,
    is_oversized BOOLEAN,
    oversized_length DECIMAL(5,2),
    oversized_width DECIMAL(5,2),
    oversized_height DECIMAL(5,2),
    other_conditions TEXT,

    -- Внешний ключ на заявку
    CONSTRAINT fk_special_conditions_request_id
        FOREIGN KEY (request_id)
        REFERENCES exchange_request.request(id)
        ON DELETE CASCADE,

    -- Проверка: класс опасности — цифра от 1 до 9
    CONSTRAINT chk_dangerous_class_valid
        CHECK (dangerous_class IS NULL OR dangerous_class ~ '^[1-9]$'),

    -- Проверка диапазона температур
    CONSTRAINT chk_temp_range
        CHECK (
            (temp_min IS NULL AND temp_max IS NULL) OR
            (temp_min >= -200 AND temp_max <= 50 AND (temp_min <= temp_max))
        ),

    -- Температурные поля заполняются только если has_temperature = TRUE
    CONSTRAINT chk_temp_requires_flag
        CHECK (
            (has_temperature IS TRUE AND temp_min IS NOT NULL AND temp_max IS NOT NULL) OR
            (has_temperature IS NOT TRUE AND temp_min IS NULL AND temp_max IS NULL)
        ),

    -- Габаритные размеры заполняются только если is_oversized = TRUE
    CONSTRAINT chk_oversized_requires_flag
        CHECK (
            (is_oversized IS TRUE AND (oversized_length IS NOT NULL OR oversized_width IS NOT NULL OR oversized_height IS NOT NULL)) OR
            (is_oversized IS NOT TRUE AND oversized_length IS NULL AND oversized_width IS NULL AND oversized_height IS NULL)
        )
);

-- Индекс по request_id для ускорения JOIN и поиска
CREATE INDEX IF NOT EXISTS idx_special_conditions_request_id ON exchange_request.special_conditions(request_id);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE exchange_request.special_conditions IS 'Особые условия перевозки: опасный груз, температурный режим, негабарит, иные условия';

COMMENT ON COLUMN exchange_request.special_conditions.id IS 'Уникальный идентификатор записи с особыми условиями. Первичный ключ.';
COMMENT ON COLUMN exchange_request.special_conditions.request_id IS 'Ссылка на заявку. Уникальный внешний ключ (1:1).';
COMMENT ON COLUMN exchange_request.special_conditions.is_dangerous IS 'Признак, что груз является опасным (TRUE — опасный).';
COMMENT ON COLUMN exchange_request.special_conditions.dangerous_class IS 'Класс опасности груза (1–9), заполняется только если is_dangerous = TRUE.';
COMMENT ON COLUMN exchange_request.special_conditions.has_temperature IS 'Признак необходимости поддержания температурного режима (TRUE — требуется).';
COMMENT ON COLUMN exchange_request.special_conditions.temp_min IS 'Минимальная температура перевозки в °C (от -200 до +50), актуально при has_temperature = TRUE.';
COMMENT ON COLUMN exchange_request.special_conditions.temp_max IS 'Максимальная температура перевозки в °C (от -200 до +50), актуально при has_temperature = TRUE.';
COMMENT ON COLUMN exchange_request.special_conditions.is_oversized IS 'Признак, что груз является негабаритным (TRUE — негабарит).';
COMMENT ON COLUMN exchange_request.special_conditions.oversized_length IS 'Длина негабаритного груза в метрах, заполняется только если is_oversized = TRUE.';
COMMENT ON COLUMN exchange_request.special_conditions.oversized_width IS 'Ширина негабаритного груза в метрах, заполняется только если is_oversized = TRUE.';
COMMENT ON COLUMN exchange_request.special_conditions.oversized_height IS 'Высота негабаритного груза в метрах, заполняется только если is_oversized = TRUE.';
COMMENT ON COLUMN exchange_request.special_conditions.other_conditions IS 'Произвольное текстовое описание дополнительных условий перевозки.';

-- Создание таблицы attachments в схеме exchange_request

CREATE TABLE IF NOT EXISTS exchange_request.attachments (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL,
    file_type VARCHAR(20) NOT NULL,
    original_name VARCHAR(500) NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    file_size BIGINT NOT NULL CHECK (file_size > 0),
    mime_type VARCHAR(100) NOT NULL,
    upload_at TIMESTAMP WITH TIME ZONE NOT NULL,

    -- Внешний ключ на заявку
    CONSTRAINT fk_attachments_request_id
        FOREIGN KEY (request_id)
        REFERENCES exchange_request.request(id)
        ON DELETE CASCADE,

    -- Ограничение на допустимые типы файлов
    CONSTRAINT chk_attachments_file_type
        CHECK (file_type IN ('CARGO_PHOTO', 'DOCUMENT'))
);

-- Индекс по request_id для ускорения запросов по заявке
CREATE INDEX IF NOT EXISTS idx_attachments_request_id ON exchange_request.attachments(request_id);

-- Индекс по request_id + file_type — для фильтрации по типу вложений
CREATE INDEX IF NOT EXISTS idx_attachments_request_file_type ON exchange_request.attachments(request_id, file_type);

-- Индекс по upload_at для сортировки по времени загрузки
CREATE INDEX IF NOT EXISTS idx_attachments_upload_at ON exchange_request.attachments(upload_at DESC);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE exchange_request.attachments IS 'Прикреплённые файлы к заявке: фото груза и документы';

COMMENT ON COLUMN exchange_request.attachments.id IS 'Уникальный идентификатор файла. Первичный ключ.';
COMMENT ON COLUMN exchange_request.attachments.request_id IS 'Ссылка на заявку. Внешний ключ на exchange_request.request(id).';
COMMENT ON COLUMN exchange_request.attachments.file_type IS 'Тип файла: ''cargo_photo'' — фото груза, ''document'' — документ.';
COMMENT ON COLUMN exchange_request.attachments.original_name IS 'Оригинальное имя файла при загрузке. Максимум 500 символов.';
COMMENT ON COLUMN exchange_request.attachments.storage_path IS 'Путь к файлу в системе хранения (например, S3, MinIO). Максимум 1000 символов.';
COMMENT ON COLUMN exchange_request.attachments.file_size IS 'Размер файла в байтах. Должен быть больше 0.';
COMMENT ON COLUMN exchange_request.attachments.mime_type IS 'MIME-тип файла (например, image/jpeg, application/pdf). Используется для валидации и отображения.';
COMMENT ON COLUMN exchange_request.attachments.upload_at IS 'Дата и время загрузки файла. С временем часового пояса.';

-- =============================================
-- Таблица: users
-- Назначение: хранение пользователей системы
-- Каждый пользователь связан с организацией
-- =============================================
CREATE TABLE IF NOT EXISTS exchange_request.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    token_id VARCHAR(255) NOT NULL UNIQUE,

    -- Электронная почта: обязательная, уникальная, с валидацией формата
    email VARCHAR(255) NOT NULL UNIQUE
        CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    -- Телефон: опциональный, но если указан — должен быть уникальным и соответствовать формату
    phone VARCHAR(20) UNIQUE
        CHECK (phone IS NULL OR phone ~ '^+?[0-9\s-]+$'),
    -- Ссылка на организацию (обязательно)
    organization_id UUID NOT NULL,

    -- Внешний ключ на organization.id
    CONSTRAINT fk_user_organization
        FOREIGN KEY (organization_id) REFERENCES exchange_request.organization(id)

    -- Уникальность пары email-phone не требуется, но логически может быть добавлена позже
    -- При необходимости: UNIQUE(email, phone)
);

-- ================================
-- Комментарии к таблице и столбцам
-- ================================

COMMENT ON TABLE exchange_request.users IS 'Пользователи системы биржи грузоперевозок';

COMMENT ON COLUMN exchange_request.users.id IS 'Уникальный идентификатор пользователя (UUID)';
COMMENT ON COLUMN exchange_request.users.token_id IS 'Уникальный идентификатор токена пользователя';
COMMENT ON COLUMN exchange_request.users.email IS 'Электронная почта пользователя. Обязательна, уникальна. Формат: user@example.com';
COMMENT ON COLUMN exchange_request.users.phone IS 'Телефонный номер. Опционально, но если указан — должен начинаться с + и содержать только цифры, пробелы и дефисы';
COMMENT ON COLUMN exchange_request.users.organization_id IS 'Ссылка на организацию, к которой принадлежит пользователь';

-- ================================
-- Индексы
-- ================================

CREATE INDEX if not exists idx_user_organization_id ON exchange_request.users(organization_id);

COMMENT ON INDEX exchange_request.idx_user_organization_id IS 'Индекс для ускорения поиска пользователей по организации';

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_token_id ON exchange_request.users(token_id);

COMMENT ON INDEX exchange_request.idx_users_token_id IS 'Уникальный индекс для быстрого поиска пользователя по token_id (jti)';

