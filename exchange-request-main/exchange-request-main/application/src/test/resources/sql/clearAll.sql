DELETE FROM exchange_request.attachments;
DELETE FROM exchange_request.special_conditions;
DELETE FROM exchange_request.vehicle_requirements;
DELETE FROM exchange_request.cargo_details;
DELETE FROM exchange_request.waypoint;
DELETE FROM exchange_request.request_carrier_reply;

-- 2. Удалить основную сущность
DELETE FROM exchange_request.request_history;
DELETE FROM exchange_request.request;

-- 3. Удалить пользователей и организации (если нужно)
DELETE FROM exchange_request.users;
DELETE FROM exchange_request.organization;

-- 4. Счётчик human-readable ID (если нужно сбросить)
DELETE FROM exchange_request.humanreadable_id_counter;
