INSERT INTO external_request.position_transport_type (position_id, transport_type)
SELECT DISTINCT ptc.position_id, 'YANDEX'
FROM corporate.position_taxi_classes ptc
WHERE NOT EXISTS (
    SELECT 1
    FROM external_request.position_transport_type ptt
    WHERE ptt.position_id = ptc.position_id
    AND ptt.transport_type = 'YANDEX'
)
ON CONFLICT DO NOTHING;

INSERT INTO external_request.organization_transport_type (organization_id, transport_type)
SELECT DISTINCT tor.organization_id, 'YANDEX'
FROM corporate.transport_org tor
WHERE tor.transport_type IN ('TAXI')
AND NOT EXISTS (
    SELECT 1
    FROM external_request.organization_transport_type ott
    WHERE ott.organization_id = tor.organization_id
    AND ott.transport_type = 'YANDEX'
)
ON CONFLICT DO NOTHING;