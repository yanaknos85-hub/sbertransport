INSERT INTO authentication.role (code, name, description, data_master, default_for) VALUES('ROLE_ETRN_SIGNER', 'Подписант ЭТрН', 'Подписант ЭТрН', false, '[]') ON CONFLICT DO NOTHING;

INSERT INTO sudir.role (code, name, description, data_master, default_for) VALUES('ROLE_ETRN_SIGNER', 'Подписант ЭТрН', 'Подписант ЭТрН', false, '[]') ON CONFLICT DO NOTHING;

INSERT INTO roles."role"
(code, "name", description, data_master, default_for, exclusive)
VALUES('ROLE_ETRN_SIGNER', 'Подписант ЭТрН', 'Подписант ЭТрН', false, '[]'::jsonb, '["INTERNAL"]'::json) ON CONFLICT DO NOTHING;


-- ============================================================
-- Роли доступа ROLE_ETRN_SIGNER для сервиса ЭТрН (etrn-cargo)
-- ============================================================

-- POST  — создание новой ЭТрН
CALL migrations.fill_roles('etrn_cargo', 'POST /', 'ROLE_ETRN_SIGNER', true);

-- POST /list — журнал ЭТрН (список с пагинацией)
CALL migrations.fill_roles('etrn_cargo', 'POST /list/', 'ROLE_ETRN_SIGNER', true);

-- GET /{id} — детальная информация об ЭТрН
CALL migrations.fill_roles('etrn_cargo', 'GET /{id}/', 'ROLE_ETRN_SIGNER', true);

-- POST /{id}/force-transition — принудительный переход статуса
CALL migrations.fill_roles('etrn_cargo', 'POST /{id}/force-transition/', 'ROLE_ETRN_SIGNER', true);

-- PUT /{id}/lock — установка блокировки
CALL migrations.fill_roles('etrn_cargo', 'PUT /{id}/lock/', 'ROLE_ETRN_SIGNER', true);

-- DELETE /{id}/lock — снятие блокировки
CALL migrations.fill_roles('etrn_cargo', 'DELETE /{id}/lock/', 'ROLE_ETRN_SIGNER', true);

-- GET /attorneyCheck — проверка доверенностей
CALL migrations.fill_roles('etrn_cargo', 'GET /attorneyCheck/', 'ROLE_ETRN_SIGNER', true);