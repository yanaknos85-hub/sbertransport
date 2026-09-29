-- Скрипт 4: UPDATE записи с FINAL_ROUTE_AFFIRM → TRIP_AFFIRM
-- Меняет type только для тех записей, где is_target_for_update = true
-- (т.е. не существует записи с тем же parent_id, классом REQUEST_PERSONAL
--  и type = 'TRIP_AFFIRM' — чтобы не создать дубль)

UPDATE notifications_settings.notification ns
SET type = 'TRIP_AFFIRM'
WHERE
    ns.class = 'REQUEST_PERSONAL'
    AND ns.type = 'FINAL_ROUTE_AFFIRM'
    AND ns.description = 'notice_308'
    AND NOT EXISTS (
        SELECT 1
        FROM notifications_settings.notification joined_ns
        WHERE joined_ns.parent_id = ns.parent_id
          AND joined_ns.class = 'REQUEST_PERSONAL'
          AND joined_ns.type = 'TRIP_AFFIRM'
    );