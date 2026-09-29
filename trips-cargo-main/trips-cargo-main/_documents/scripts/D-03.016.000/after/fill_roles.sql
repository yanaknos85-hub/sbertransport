call migrations.fill_roles('trips_cargo', 'POST /integration/', 'ROLE_EXTERNAL_REQUEST', true);
call migrations.fill_roles('trips_cargo', 'GET /integration/{humanReadableId}/', 'ROLE_EXTERNAL_REQUEST', true);
call migrations.fill_roles('trips_cargo', 'POST /integration/{humanReadableId}/cancel/', 'ROLE_EXTERNAL_REQUEST', true);