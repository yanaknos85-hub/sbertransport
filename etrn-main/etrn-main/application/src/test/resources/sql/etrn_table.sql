-- Тестовые ЭТрН
insert into etrn_cargo.etrn (id, humanreadableid, status, sender_name, receiver_name, carrier_name, timezone, sla, active, title_chain, verifications, lock_info, created_at, updated_at, version,
                            application_number, route_number, cargo_description, cargo_places, cargo_weight_kg, route, mrpa_expires_at, cargo_length, cargo_width, cargo_height,
                            ses_full_name, ses_role, ses_event_datetime, ses_event_id)
values
    ('cf8f42df-998a-48b4-b349-1a33f0c1072b', 'ETRN-0001-00000001', 'IDENTIFIED',
     'ООО «ТестЛогистик»', 'ООО «ТранспортСервис»', 'ИП Иванов И.И.', 'Europe/Moscow',
     '2025-02-01 10:00:00', true,
     '[{"title":"T1","signedAt":"2025-01-15T10:00:00","signedBy":"f10bcc5b-51db-4e1c-a747-2a229604f974"}]',
     null,
     '{"userId":"f10bcc5b-51db-4e1c-a747-2a229604f974","lockUntil":"2025-01-15T12:00:00"}',
     '2025-01-15 10:00:00', '2025-01-15 12:00:00', 1,
     'APP-2025-001', 'RT-2025-001', 'Груз: оборудование', 5, 1500.50, 'Москва — Санкт-Петербург',
     '2025-12-31', 120.0, 3.0, 4.0,
     'Петров И.И.', 'Carrier', '2025-01-15T11:00:00', 'EVENT-001'),

    ('df9f53ee-009b-59c5-c450-2b44g1d2083c', 'ETRN-0002-00000002', 'WAIT_CONDITIONS',
     'ООО «ТранспортСервис»', 'ООО «ТестЛогистик»', 'ИП Сидоров С.С.', 'Europe/Moscow',
     '2025-03-01 14:00:00', true,
     '[{"title":"T1","signedAt":"2025-02-01T10:00:00","signedBy":"a26a3382-674d-4497-9411-815303250ee1"},
       {"title":"T2","signedAt":"2025-02-01T11:00:00","signedBy":"a26a3382-674d-4497-9411-815303250ee1"}]',
     null,
     null,
     '2025-02-01 10:00:00', '2025-02-01 15:00:00', 2,
     'APP-2025-002', 'RT-2025-002', 'Груз: материалы', 10, 3200.00, 'Санкт-Петербург — Москва',
     '2025-12-31', 200.0, 4.0, 5.0,
     null, null, null, null);

-- Записи аудита
insert into etrn_cargo.etrn_audit (id, etrn_id, action, details, created_by, created_at)
values
    ('a1111111-1111-1111-1111-111111111111', 'cf8f42df-998a-48b4-b349-1a33f0c1072b', 'ЭТрН создана',
     'humanReadableId=ETRN-0001-00000001', 'f10bcc5b-51db-4e1c-a747-2a229604f974', '2025-01-15 10:00:00'),

    ('a2222222-2222-2222-2222-222222222222', 'df9f53ee-009b-59c5-c450-2b44g1d2083c', 'ЭТрН создана',
     'humanReadableId=ETRN-0002-00000002', 'a26a3382-674d-4497-9411-815303250ee1', '2025-02-01 10:00:00');
