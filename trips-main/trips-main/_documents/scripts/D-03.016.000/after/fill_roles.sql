call migrations.fill_roles('trips', 'POST /integration/', 'ROLE_EXTERNAL_REQUEST', true);
call migrations.fill_roles('trips', 'GET /integration/{humanReadableId}/', 'ROLE_EXTERNAL_REQUEST', true);
call migrations.fill_roles('trips', 'POST /integration/{humanReadableId}/cancel/', 'ROLE_EXTERNAL_REQUEST', true);

call migrations.fill_roles('trips', 'POST /self/dispatcher/driver/busyness/', 'ROLE_DRIVER_CONTRACTOR', true);