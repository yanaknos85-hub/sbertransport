-- 1. Индекс по state + active — для findByStateAndActiveTrue(state)
CREATE INDEX IF NOT EXISTS idx_session_state_active ON authorization_sbid.sessions (state, active);

-- 2. Индекс по refresh_token + active — для findByRefreshTokenAndActiveTrue(refreshToken)
CREATE INDEX IF NOT EXISTS idx_session_refresh_token_active ON authorization_sbid.sessions (refresh_token, active);

-- 3. Индекс по access_token + sub + active — для findAllByActiveTrueAndAccessTokenAndSub(accessToken, sub)
CREATE INDEX IF NOT EXISTS idx_session_access_token_sub_active ON authorization_sbid.sessions (access_token, sub, active);

-- 4. Индекс по creation_time + active — полезен для TTL, очистки старых сессий
CREATE INDEX IF NOT EXISTS idx_session_creation_time_active ON authorization_sbid.sessions (creation_time, active);

-- 5. Индекс по sub — если часто ищете все сессии пользователя
CREATE INDEX IF NOT EXISTS idx_session_sub ON authorization_sbid.sessions (sub);

-- Создание таблицы organizations для хранения данных об организациях

CREATE TABLE IF NOT EXISTS authorization_sbid.organizations (
    -- Уникальный идентификатор организации (UUID)
    id UUID PRIMARY KEY,

    -- ИНН организации (уникальный)
    inn VARCHAR(12) NOT NULL UNIQUE,

    -- ОГРН
    ogrn VARCHAR(15) UNIQUE,

    -- КПП
    kpp VARCHAR(9),

    -- ОКТМО
    oktmo VARCHAR(11),

    -- Сокращённая форма собственности (ООО, ПАО и т.д.)
    legal_form_short VARCHAR(10),

    -- Полное наименование организации
    full_name TEXT NOT NULL,

    -- Юридический адрес
    juridical_address TEXT,

    -- Фактический адрес
    actual_address TEXT,

    -- Территориальный банк
    territorial_bank VARCHAR(255),

    -- Email организации
    email VARCHAR(255),

    -- Исполнительное учреждение
    individual_executive_agency integer,

    -- Срок действия оферты
    offer_expiration_date TIMESTAMP WITH TIME ZONE,

    -- Организация в юр. лице
    org_law_form VARCHAR(255),

    -- Системные поля
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Индексы для ускорения поиска
CREATE INDEX IF NOT EXISTS idx_organizations_inn ON authorization_sbid.organizations(inn);
CREATE INDEX IF NOT EXISTS idx_organizations_ogrn ON authorization_sbid.organizations(ogrn);
CREATE INDEX IF NOT EXISTS idx_organizations_kpp ON authorization_sbid.organizations(kpp);
CREATE INDEX IF NOT EXISTS idx_organizations_legal_form_short ON authorization_sbid.organizations(legal_form_short);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE authorization_sbid.organizations IS 'Справочник организаций, полученных из SberBusinessID. Используется для централизованного хранения данных об организациях.';

COMMENT ON COLUMN authorization_sbid.organizations.id IS 'Уникальный внутренний идентификатор организации (UUID)';
COMMENT ON COLUMN authorization_sbid.organizations.inn IS 'ИНН организации — уникальный идентификатор';
COMMENT ON COLUMN authorization_sbid.organizations.ogrn IS 'ОГРН организации';
COMMENT ON COLUMN authorization_sbid.organizations.kpp IS 'КПП организации';
COMMENT ON COLUMN authorization_sbid.organizations.oktmo IS 'ОКТМО — код муниципального образования';
COMMENT ON COLUMN authorization_sbid.organizations.legal_form_short IS 'Сокращённая форма собственности: ООО, ПАО, ЗАО и т.д.';
COMMENT ON COLUMN authorization_sbid.organizations.full_name IS 'Полное наименование организации';
COMMENT ON COLUMN authorization_sbid.organizations.juridical_address IS 'Юридический адрес организации';
COMMENT ON COLUMN authorization_sbid.organizations.actual_address IS 'Фактический адрес организации';
COMMENT ON COLUMN authorization_sbid.organizations.territorial_bank IS 'Территориальный банк';
COMMENT ON COLUMN authorization_sbid.organizations.email IS 'Email организации';
COMMENT ON COLUMN authorization_sbid.organizations.individual_executive_agency IS 'Индивидуальное исполнительное учреждение (например, судебный пристав)';
COMMENT ON COLUMN authorization_sbid.organizations.offer_expiration_date IS 'Срок действия оферты в формате';
COMMENT ON COLUMN authorization_sbid.organizations.created_at IS 'Дата и время создания записи';
COMMENT ON COLUMN authorization_sbid.organizations.updated_at IS 'Дата и время последнего обновления';
-- Создание таблицы users с внешним ключом на organizations

CREATE TABLE IF NOT EXISTS authorization_sbid.users (
    -- Уникальный идентификатор пользователя (UUID)
    id UUID PRIMARY KEY,

    -- Уникальный идентификатор пользователя в SBID (subject)
    sub VARCHAR(255) NOT NULL UNIQUE,

    -- Полное имя пользователя
    full_name VARCHAR(255),

    -- ИНН пользователя (может быть ИНН физлица или подразделения)
    inn VARCHAR(12),

    -- Номер телефона
    phone_number VARCHAR(20),

    -- Ссылка на организацию (внешний ключ по ИНН)
    organization_inn VARCHAR(12) REFERENCES authorization_sbid.organizations(inn) ON DELETE SET NULL,

    -- Системные поля
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Индексы для ускорения поиска
CREATE INDEX IF NOT EXISTS idx_users_sub ON authorization_sbid.users(sub);
CREATE INDEX IF NOT EXISTS idx_users_inn ON authorization_sbid.users(inn);
CREATE INDEX IF NOT EXISTS idx_users_organization_inn ON authorization_sbid.users(organization_inn);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE authorization_sbid.users IS 'Пользователи, авторизованные через SberBusinessID. Каждый пользователь может быть связан с одной организацией.';

COMMENT ON COLUMN authorization_sbid.users.id IS 'Уникальный внутренний идентификатор пользователя (UUID)';
COMMENT ON COLUMN authorization_sbid.users.sub IS 'Уникальный идентификатор пользователя в системе SBID (subject)';
COMMENT ON COLUMN authorization_sbid.users.full_name IS 'Полное имя пользователя';
COMMENT ON COLUMN authorization_sbid.users.inn IS 'ИНН пользователя (физического лица или подразделения)';
COMMENT ON COLUMN authorization_sbid.users.phone_number IS 'Контактный номер телефона пользователя';
COMMENT ON COLUMN authorization_sbid.users.organization_inn IS 'Ссылка на организацию по ИНН. Внешний ключ к таблице organizations.inn';
COMMENT ON COLUMN authorization_sbid.users.created_at IS 'Дата и время создания записи';
COMMENT ON COLUMN authorization_sbid.users.updated_at IS 'Дата и время последнего обновления';
