-- Скрипт 1: SELECT с проверкой условий
-- Выбирает записи уведомлений класса REQUEST_PERSONAL с типом FINAL_ROUTE_AFFIRM_STATUS
-- и показывает boolean-флаг is_target_for_update.
-- Флаг = true, если:
--   - ns.description = 'notice_309'
--   - не существует ни одной записи с тем же parent_id, классом REQUEST_PERSONAL
--     и type = 'TRIP_AFFIRM_STATUS' (т.е. дубль по (parent_id, class, type) не возникнет)

SELECT
    ns.description,
    ns.parent_id,
    CASE
        WHEN ns.description = 'notice_309'
             AND NOT EXISTS (
                 SELECT 1
                 FROM notifications_settings.notification joined_ns
                 WHERE joined_ns.parent_id = ns.parent_id
                   AND joined_ns.class = 'REQUEST_PERSONAL'
                   AND joined_ns.type = 'TRIP_AFFIRM_STATUS'
             )
        THEN true
        ELSE false
    END AS is_target_for_update
FROM notifications_settings.notification ns
WHERE
    ns.class = 'REQUEST_PERSONAL'
    AND ns.type = 'FINAL_ROUTE_AFFIRM_STATUS';