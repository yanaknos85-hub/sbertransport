import moment from 'moment';

import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';
import { TTripStops } from 'stores/Trip/Trip.interface';
import { MESSAGES } from 'constants/constants.app';

const findNewOrderStops = (stops: TTripStops[], eventType: string): TTripStops | undefined => stops.find(element => element.orderId === -1 && element.eventType === eventType);

const findOldOrderStops = (stops: TTripStops[], eventType: string): TTripStops | undefined => stops.find(element => element.orderId !== -1 && element.eventType === eventType);

const findStartAndEndPoints = (
  stops: TTripStops[]
): {
  newOrderStart: TTripStops | undefined;
  orderStart: TTripStops | undefined;
  newOrderEnd: TTripStops | undefined;
  orderEnd: TTripStops | undefined;
} => {
  const newOrderStart = findNewOrderStops(stops, MESSAGES.seat);
  const orderStart = findOldOrderStops(stops, MESSAGES.seat);
  const newOrderEnd = findNewOrderStops(stops, MESSAGES.landing);
  const orderEnd = findOldOrderStops(stops, MESSAGES.landing);

  return {
    newOrderStart,
    orderStart,
    newOrderEnd,
    orderEnd,
  };
};

const isDeviationsByTime = (
  newStart: string | number,
  oldStart: string | number,
  newEnd: string | number,
  oldEnd: string | number
): boolean => !(moment(newStart).valueOf >= moment(oldStart).valueOf && moment(newEnd).valueOf <= moment(oldEnd).valueOf);

const isDeviationsByRoute = (stops: TTripStops[]): boolean => {
  const newAddressCount = stops.filter(element => element.orderId === -1).length;
  let allNewStops = 0;
  let addressPassed = 0;
  stops.forEach(element => {
    if (element.orderId === -1) {
      addressPassed += 1;
      allNewStops += 1;
    } else if (addressPassed < newAddressCount) {
      allNewStops += 1;
    }
  });
  return allNewStops <= newAddressCount;
};

export enum CoincidenceStatuses {
  fullMatch = 'fullMatch',
  deviationsByRoute = 'deviationsByRoute',
  deviationsByTime = 'deviationsByTime',
  deviationsByBoth = 'deviationsByBoth',
}

export const calculateCoincidenceStatus = (trip: TripSuitableModel): string => {
  const { stops } = trip;
  const {
    newOrderStart, orderStart, newOrderEnd, orderEnd,
  } = findStartAndEndPoints(stops);

  if (newOrderStart && orderStart && newOrderEnd && orderEnd) {
    if (
      !isDeviationsByTime(newOrderStart.startTime, orderStart.startTime, newOrderEnd.startTime, orderEnd.startTime)
      && !isDeviationsByRoute(stops)
    ) {
      return CoincidenceStatuses.fullMatch;
    }
    if (!isDeviationsByRoute(stops)) {
      return CoincidenceStatuses.deviationsByRoute;
    }
    if (!isDeviationsByTime(newOrderStart.startTime, orderStart.startTime, newOrderEnd.startTime, orderEnd.startTime)) {
      return CoincidenceStatuses.deviationsByTime;
    }
  }
  return CoincidenceStatuses.deviationsByBoth;
};
