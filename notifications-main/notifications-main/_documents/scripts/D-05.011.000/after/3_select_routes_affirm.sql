-- Скрипт 3: SELECT с проверкой условий для notice_308
-- Выбирает записи с class = REQUEST_PERSONAL и type = FINAL_ROUTE_AFFIRM
-- Флаг is_target_for_update = true, если:
--   - ns.description = 'notice_308'
--   - не существует записи с тем же parent_id, классом REQUEST_PERSONAL
--     и type = 'TRIP_AFFIRM' (т.е. дубль по (parent_id, class, type) не возникнет)

SELECT
    ns.description,
    ns.parent_id,
    CASE
        WHEN ns.description = 'notice_308'
             AND NOT EXISTS (
                 SELECT 1
                 FROM notifications_settings.notification joined_ns
                 WHERE joined_ns.parent_id = ns.parent_id
                   AND joined_ns.class = 'REQUEST_PERSONAL'
                   AND joined_ns.type = 'TRIP_AFFIRM'
             )
        THEN true
        ELSE false
    END AS is_target_for_update
FROM notifications_settings.notification ns
WHERE
    ns.class = 'REQUEST_PERSONAL'
    AND ns.type = 'FINAL_ROUTE_AFFIRM';