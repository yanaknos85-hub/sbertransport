import React from 'react';
import moment from 'moment';

import { PassTrip } from 'api/trips/trips.types';
import { TRIP_STATUSES } from 'constants/trips.constants';

import { ReactComponent as Lightning } from 'assets/icons/lightning.svg';
import { ReactComponent as Warning } from 'assets/icons/warning.svg';
import { ReactComponent as Fire } from 'assets/icons/fire.svg';

export const isRequestDeadline = (trip: PassTrip): boolean => (trip.status === TRIP_STATUSES.SENT_TO_CONTRACTOR
  || trip.status === TRIP_STATUSES.WAITING_FOR_ASSIGNMENT
  || trip.status === TRIP_STATUSES.DRIVER_ASSIGNED)
  && moment().diff(trip.expectedStartTime, 'minutes') >= 15;

export const isDriverAssigned = (status: TRIP_STATUSES) => status === TRIP_STATUSES.DRIVER_ARRIVED
  || status === TRIP_STATUSES.DRIVER_ASSIGNED
  || status === TRIP_STATUSES.DRIVER_ON_THE_WAY
  || status === TRIP_STATUSES.TRIP_IN_PROGRESS;

export const isVehicleBooked = (trip: PassTrip): boolean => !!trip.vehicle && !(trip.driver ?? trip.planned?.driver);

export const getRequestIcon = (trip: PassTrip) => {
  if (isVehicleBooked(trip)) return <Warning />;

  if (isRequestDeadline(trip)) return <Fire />;

  if (trip.status === TRIP_STATUSES.WAITING_FOR_ASSIGNMENT) return <Lightning />;

  return '';
};
