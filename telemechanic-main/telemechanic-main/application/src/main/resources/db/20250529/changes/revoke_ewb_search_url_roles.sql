DELETE FROM telemechanic.roles
WHERE url_id IN (
    SELECT id
    FROM telemechanic.urls
    WHERE url = '/ewb/search/'
);

DELETE FROM telemechanic.urls WHERE url = '/ewb/search/';
