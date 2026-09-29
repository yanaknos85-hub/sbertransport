UPDATE tariff.tariff
SET price_details = (
    SELECT regexp_replace(replace(price_details::text, ',}', '}'), '\\"taxiClass\\": ([A-Z]*)', '\"taxiClass\": \"\1\"')::json
    FROM tariff.tariff AS t
    WHERE t.id = tariff.id)
WHERE tariff.id IS NOT NULL;

UPDATE tariff.tariff
SET price_details = (SELECT replace(replace(regexp_replace(price_details::text, '^\{', '{'), '\"', '"'), '}"}', '}}')
                     FROM tariff.tariff AS t
                     WHERE t.id = tariff.id)::json
WHERE tariff.id IS NOT NULL;