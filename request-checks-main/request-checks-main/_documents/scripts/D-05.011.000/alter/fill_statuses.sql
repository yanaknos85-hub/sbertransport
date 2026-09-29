BEGIN;

WITH latest AS (
    SELECT
        x.id,
        x.request_status,
        x.creation_time,
        row_number() OVER (PARTITION BY x.id ORDER BY x.creation_time DESC) AS rn
    FROM (
             SELECT id, request_status::text AS request_status, creation_time AS creation_time
             FROM request.request_for_taxi
             WHERE creation_time >= now() - interval '30 days'
             UNION ALL
             SELECT id, request_status::text AS request_status, creation_time
             FROM request.request_for_carsharing
             WHERE creation_time >= now() - interval '30 days'
             UNION ALL
             SELECT id, request_status::text AS request_status, creation_time
             FROM request.request_for_public
             WHERE creation_time >= now() - interval '30 days'
             UNION ALL
             SELECT id, request_status::text AS request_status, creation_time
             FROM request.request_for_personal
             WHERE creation_time >= now() - interval '30 days'
             UNION ALL
             SELECT
                 id,
                 status::text AS request_status,
                 "date"::timestamptz AS creation_time
             FROM external_request.trip_order
             WHERE "date" >= now() - interval '30 days'
         ) x
)
UPDATE request_checks.trip_request t
SET status = l.request_status
FROM latest l
WHERE l.rn = 1
    AND t.id = l.id;

COMMIT;