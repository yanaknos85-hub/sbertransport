DELETE FROM telemechanic.roles
WHERE url_id IN (
    SELECT id
    FROM telemechanic.urls
    WHERE url = '/transport/statenumber/'
);

DELETE FROM telemechanic.urls WHERE url = '/transport/statenumber/';
