import { CargoTrip } from 'api/trips-cargo/trips-cargo.types';
import { TRIP_STATUSES } from 'constants/trips.constants';
import moment from 'moment';

export const isRequestDeadline = (trip: CargoTrip): boolean => (trip.status === TRIP_STATUSES.SENT_TO_CONTRACTOR
  || trip.status === TRIP_STATUSES.WAITING_FOR_ASSIGNMENT
  || trip.status === TRIP_STATUSES.DRIVER_ASSIGNED)
  && moment().diff(trip.startTime, 'minutes') >= 15;

export const isDriverAssigned = (status: TRIP_STATUSES) => status === TRIP_STATUSES.DRIVER_ARRIVED
  || status === TRIP_STATUSES.DRIVER_ASSIGNED
  || status === TRIP_STATUSES.DRIVER_ON_THE_WAY
  || status === TRIP_STATUSES.TRIP_IN_PROGRESS;
