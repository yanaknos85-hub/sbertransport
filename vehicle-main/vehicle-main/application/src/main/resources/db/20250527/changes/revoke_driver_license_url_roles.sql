DELETE FROM vehicle.roles
WHERE url_id IN (
    SELECT id
    FROM vehicle.urls
    WHERE url IN ('/drivinglicense/', '/drivinglicense/{drivingLicenseId}/', '/drivinglicense/fio/', '/drivinglicense/search/')
);

DELETE FROM vehicle.urls
WHERE url IN ('/drivinglicense/', '/drivinglicense/{drivingLicenseId}/', '/drivinglicense/fio/', '/drivinglicense/search/');
