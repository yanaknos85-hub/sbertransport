import * as t from 'io-ts';
import { TaxiClass } from 'stores/Trip/Trip.interface';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { Waypoint } from '../Geo/Geo.interface';

const SharedRideStop = t.type({
  address: t.string, // Улица номер дома
});

export type SharedRideStop = t.TypeOf<typeof SharedRideStop>;

const SharedRideStopDetailed = t.type({
  orderId: t.number,
  requestId: t.string,
  active: t.boolean,
  eventType: t.string,
  startTime: t.string,
  endTime: t.string,
  waypoint: Waypoint,
});

export type SharedRideStopDetailed = t.TypeOf<typeof SharedRideStop>;

export const SharedRide = t.type({
  id: t.string,
  passengers: t.number, // количество пассажиров для текущего заказа
  tariffId: ioTypeFromEnum<TaxiClass>('TaxiClassEnum', TaxiClass), // id тарифа
  pickupStartTime: t.string, // желаемое время отправления (может быть пустым, если заполнено dropStartTime).
  dropStartTime: t.string, // желаемое время прибытия во вторую точку маршрута (может быть пустым, если заполнено pickupStartTime)
  stops: t.array(SharedRideStop),
});

export type SharedRide = t.TypeOf<typeof SharedRide>;

const EmployeePassenger = t.type({
  id: t.string,
  humanReadableId: t.string,
  userId: t.string,
  firstName: t.string,
  lastName: t.string,
  patronymic: t.string,
  personnelNumber: t.string,
  delegatedById: t.string,
  supervisorId: t.string,
  positionId: t.string,
  organizationId: t.string,
  departmentId: t.string,
});

export type EmployeePassenger = t.TypeOf<typeof EmployeePassenger>;

const SharedRideOrderKpi = t.type({
  orderId: t.string,
  costSharePart: t.number,
  rideTimeMin: t.number,
  savings: t.number,
  savingsPct: t.number,
  orderDistanceKm: t.number,
});

export type SharedRideOrderKpi = t.TypeOf<typeof SharedRideOrderKpi>;

const SharedRideKpi = t.type({
  totalCost: t.number,
  totalDistanceKm: t.number,
  totalTimeMin: t.number,
  ordersKpi: t.array(SharedRideOrderKpi),
});

export const SharedRideDetailed = t.type({
  magentaId: t.string,
  passengers: t.number,
  employeePassengers: t.array(EmployeePassenger),
  tariffId: ioTypeFromEnum<TaxiClass>('TaxiClassEnum', TaxiClass),
  active: t.boolean,
  stops: t.array(SharedRideStopDetailed),
  kpi: SharedRideKpi,
});
