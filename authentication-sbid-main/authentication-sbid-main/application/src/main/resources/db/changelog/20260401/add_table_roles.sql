CREATE TABLE IF NOT EXISTS authorization_sbid.roles (
    -- Уникальный идентификатор роли
    id UUID PRIMARY KEY,
    -- Идентификатор пользователя (ссылка на authorization_sbid.users)
    user_id UUID NOT NULL,
    -- Роль пользователя: например, 'ADMIN', 'USER', 'MODERATOR' и т.д.
    role VARCHAR(11) NOT NULL,

    -- Внешний ключ на таблицу пользователей
    CONSTRAINT fk_role_user_id
    FOREIGN KEY (user_id)
    REFERENCES authorization_sbid.users(id)
    ON DELETE CASCADE
);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE authorization_sbid.roles IS 'Таблица хранит роли пользователей в системе авторизации.';
COMMENT ON COLUMN authorization_sbid.roles.id IS 'Уникальный идентификатор записи роли (UUID).';
COMMENT ON COLUMN authorization_sbid.roles.user_id IS 'Идентификатор пользователя, которому назначена роль. Ссылается на authorization_sbid.users(id).';
COMMENT ON COLUMN authorization_sbid.roles.role IS 'Наименование роли пользователя. Максимальная длина — 11 символов (например, ADMIN, USER и т.п.).';