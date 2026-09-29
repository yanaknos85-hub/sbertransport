alter table exchange_request.request add if not exists response_status VARCHAR(50);
COMMENT ON COLUMN exchange_request.request.response_status IS 'Данные по статусам отклика Перевозчика';

create table if not exists exchange_request.request_history
(
    id                    uuid         not null
        primary key,
    change_date           timestamp    not null,
    comment               varchar(255),
    status        varchar(255) not null,
    request_id  uuid         not null
        constraint fk_request_history_request
            references exchange_request.request,
    initiator_id          uuid not null
        constraint fk_personal_history_users
            references exchange_request.users,
    initiator_role VARCHAR(128) NOT NULL
        CONSTRAINT chk_initiator_role CHECK (initiator_role IN ('CARRIER', 'SHIPPER'))
);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE exchange_request.request_history IS 'История изменений статусов заявки: фиксирует все переходы статусов и инициатора действия';

COMMENT ON COLUMN exchange_request.request_history.id IS 'Уникальный идентификатор записи истории';
COMMENT ON COLUMN exchange_request.request_history.change_date IS 'Дата и время изменения статуса';
COMMENT ON COLUMN exchange_request.request_history.comment IS 'Комментарий к изменению статуса (опционально)';
COMMENT ON COLUMN exchange_request.request_history.status IS 'Новое значение статуса заявки после изменения';
COMMENT ON COLUMN exchange_request.request_history.request_id IS 'Ссылка на заявку, для которой зафиксировано изменение';
COMMENT ON COLUMN exchange_request.request_history.initiator_id IS 'Идентификатор инициатора изменения (token_id пользователя)';
COMMENT ON COLUMN exchange_request.request_history.initiator_role IS 'Роль инициатора изменения: CARRIER (перевозчик) или SHIPPER (грузовладелец)';