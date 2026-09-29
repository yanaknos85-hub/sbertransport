DELETE FROM request.roles
WHERE url_id IN (
    SELECT id
    FROM request.urls
    WHERE url = '/finish/{requestId}/' and method = 'POST'
);
DELETE FROM request.roles
WHERE url_id IN (
    SELECT id
    FROM request.urls
    WHERE url = '/{requestId}/' and method = 'DELETE'
);

DELETE FROM request.urls WHERE url = '/finish/{requestId}/' and method = 'POST';
DELETE FROM request.urls WHERE url = '/{requestId}/' and method = 'DELETE';