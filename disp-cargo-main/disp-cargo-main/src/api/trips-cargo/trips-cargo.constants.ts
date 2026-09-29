export const CARGO_TRIPS_SERVICE = '/trips-cargo';

export const CARGO_TRIPS_BY_CONTRACTOR = `${CARGO_TRIPS_SERVICE}/contractor/:contractorId/`;
export const CARGO_TRIP = `${CARGO_TRIPS_SERVICE}/contractor/:contractorId/trip/:tripId/`;
export const CARGO_TRIPS_BY_DISPATCHER = `${CARGO_TRIPS_SERVICE}/contractor/:contractorId/dispatcher/:dispatcherId/`;
export const CARGO_DRIVERS_LOCATIONS = `${CARGO_TRIPS_SERVICE}/contractor/:contractorId/driver/location/`;
export const CARGO_DRIVERS_LOCATION_WEBSOCKET = `${CARGO_TRIPS_SERVICE}/ws/driverPositions/v2`;
export const CARGO_CHECKIN_INFO = `${CARGO_TRIPS_SERVICE}/contractor/:contractorId/trip/:tripId/checkin-info/`;
export const CARGO_DRIVER_ONLINE_SWITCHER = `${CARGO_TRIPS_SERVICE}/self/dispatcher/driver/:driverId/online-switcher/`;
export const CARGO_EXPORT_TRIPS = `${CARGO_TRIPS_SERVICE}/files/trips/`;
export const CARGO_EXPORT_TRIPS_CARGO = `${CARGO_TRIPS_SERVICE}/files/cargo/`;
export const CARGO_TRIPS_WEBSOCKET = `${CARGO_TRIPS_SERVICE}/ws/trips/v2`;
export const CARGO_BUSYNESS = `${CARGO_TRIPS_SERVICE}/self/dispatcher/driver/busyness/`;
export const CARGO_TRIPS_STATISTIC = `${CARGO_TRIPS_SERVICE}/contractor/:contractorId/statistic/`;

export const CARGO_TRIPS_KEY = 'cargo-trips';
export const CARGO_DRIVERS_LOCATIONS_KEY = 'cargo-drivers-locations';
export const CARGO_ALL_DRIVERS_LOCATIONS_KEY = 'cargo-all-drivers-locations';
export const CARGO_TRIPS_STATISTIC_KEY = 'cargo-trips-statistic';

export const DRIVERS_CACHE_TIME = 15 * 1000;
export const ALL_DRIVERS_PAGE_SIZE = 20;
