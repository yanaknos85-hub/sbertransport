import * as t from 'io-ts';
import { PaginationParams, SortParams, createPagination } from 'utils/io-ts/pagination';
import { finalTripStatuses } from 'constants/trips.constants';

/** Поездка из отчетности */
export const TripsReport = t.intersection([
  t.type({
    id: t.string,
    humanReadableId: t.string,
    requestHumanReadableId: t.string,
    desiredDate: t.string,
    routeStart: t.array(t.string),
    routeWaypoints: t.array(t.string),
    routeEnd: t.array(t.string),
    expectedTime: t.number,
    expectedWaitTime: t.number,
    expectedDistance: t.number,
    minRideDistanceCost: t.number,
    waitCostPerMin: t.number,
    rideCostPerKm: t.number,
    totalWithoutVAT: t.number,
    status: t.string,
    comment: t.string,
    tripType: t.string,
    contractNumber: t.string,
    driverFullName: t.string,
    vehicleNumber: t.string,
    passengerFullNames: t.array(t.string),
    authorFullName: t.string,
    creationTime: t.string,
  }),
  t.partial({
    factDistance: t.number,
    factCost: t.number,
    factTime: t.number,
    factWaitTime: t.number,
  }),
]);
export type TripsReport = t.TypeOf<typeof TripsReport>;

/** Отчетность */
export const TripsReports = createPagination(TripsReport);
export type TripsReports = t.TypeOf<typeof TripsReports>;

export interface TripsReportsFilters extends PaginationParams, SortParams {
  statuses?: typeof finalTripStatuses;
  tripHumanReadableId?: string;
  requestHumanReadableId?: string;
  vehicleNumber?: string;
  contractNumber?: number;
  startTimeFrom?: string;
  startTimeTo?: string;
}
