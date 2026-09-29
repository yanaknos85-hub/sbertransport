call migrations.fill_roles('trips_cargo', 'GET /contractor/{contractorId}/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('trips_cargo', 'PATCH /contractor/{contractorId}/trip/{tripId}/', 'ROLE_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('trips_cargo', 'PATCH /contractor/{contractorId}/trip/{tripId}/', 'ROLE_DRIVER_CONTRACTOR', true);

call migrations.fill_roles('trips_cargo', 'GET /contractor/{contractorId}/dispatcher/{dispatcherId}/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('trips_cargo', 'GET /contractor/{contractorId}/trip/{tripId}/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('trips_cargo', 'GET /contractor/{contractorId}/driver/{driverId}/', 'ROLE_DISPATCHER_CONTRACTOR', true);
call migrations.fill_roles('trips_cargo', 'GET /contractor/{contractorId}/driver/{driverId}/', 'ROLE_DRIVER_CONTRACTOR', true);

call migrations.fill_roles('trips_cargo', 'GET /contractor/{contractorId}/driver/{driverId}/trip/{tripId}/', 'ROLE_DRIVER_CONTRACTOR', true);


call migrations.fill_roles('trips_cargo', 'GET /contractor/{contractorId}/driver/{driverId}/trip/current/', 'ROLE_DRIVER_CONTRACTOR', true);
call migrations.fill_roles('trips_cargo', 'GET /contractor/{contractorId}/driver/{driverId}/trip/current/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('trips_cargo', 'PUT /contractor/{contractorId}/driver/{driverId}/trip/{tripId}/final/', 'ROLE_DRIVER_CONTRACTOR', true);

call migrations.fill_roles('trips_cargo', 'PUT /self/last-point/', 'ROLE_DRIVER_CONTRACTOR', true);

call migrations.fill_roles('trips_cargo', 'PUT /self/online/', 'ROLE_DRIVER_CONTRACTOR', true);

call migrations.fill_roles('trips_cargo', 'GET /self/trip/{tripId}/checkin-info/', 'ROLE_DRIVER_CONTRACTOR', true);


call migrations.fill_roles('trips_cargo', 'PUT /self/dispatcher/driver/{driverId}/online-switcher/', 'ROLE_DISPATCHER_CONTRACTOR', true);

call migrations.fill_roles('trips_cargo', 'GET /contractor/{contractorId}/driver/location/', 'ROLE_DISPATCHER_CONTRACTOR', true);
