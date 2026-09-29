import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const AnalysisTaxiResponse = t.partial({
  serviceName: tt.nullable(t.string),
  transportType: tt.nullable(t.string),
  organizationName: tt.nullable(t.string),
  departmentName: tt.nullable(t.string),

  todayRequestsCount: t.union([t.null, t.number, t.undefined]),
  fulfilmentRate: t.union([t.type({ count: t.number, value: t.number }), t.null]),
  declinedRate: t.union([t.type({ count: t.number, value: t.number }), t.null]),
  csiRate: t.union([t.type({ count: t.number, value: t.number }), t.null]),
  transportTypeExpensesRate: t.union([t.type({ count: t.number, value: t.number }), t.number, t.null]),
  limitSpentRate: t.union([t.type({ count: t.number, value: t.number }), t.number, t.null]),
  expiredRate: t.union([t.type({ count: t.number, value: t.number }), t.null]),
  createdYearToDateCount: t.union([t.type({ count: t.number, value: t.number }), t.number, t.null]),
  closedYearToDateCount: t.union([t.type({ count: t.number, value: t.number }), t.number, t.null]),
});

export type AnalysisTaxiResponse = t.TypeOf<typeof AnalysisTaxiResponse>;

export interface AnalysisTaxiResponseTable {
  serviceName?: AnalysisTaxiResponse['serviceName'];
  departmentName?: AnalysisTaxiResponse['departmentName'];
  todayRequestsCount?: AnalysisTaxiResponse['todayRequestsCount'];

  expiredRateCount?: number;
  expiredRateValue?: number;

  fulfilmentRateValue?: number;
  csiRateValue?: number;
  declinedRateValue?: number;
  pities?: number;
}

export const SortSettings = t.type({
  property: t.string,
  directionAsc: t.boolean,
});

export const PageSettings = t.type({
  page: t.number,
  size: t.number,
});

export const AnalysisTaxiQuery = t.intersection([
  t.strict({
    organizationId: tt.uuid,
  }),
  t.partial({
    requestHumanId: t.string,
    requestStatusSet: t.array(t.string),
    creationDate: t.type({ start: t.number, end: t.number }),
    purposeSet: t.array(t.type({ id: t.string, purpose: t.string })),
    expectedCost: t.type({ start: t.number, end: t.number }),
    expectedDistance: t.type({ start: t.number, end: t.number }),
    employeeFIO: t.string,
    costCenter: t.string,
    contractorSet: t.array(t.string),
    employeePositionSet: t.array(t.string),
    employeeDepartmentSet: t.array(t.string),
    departureAddress: t.string,
    destinationAddress: t.string,
    employeeItinerantTypeSet: t.array(t.string),
    sortSetting: SortSettings,
    pageSetting: PageSettings,
    sharedRideId: t.string,
    coopTrip: t.boolean,
    ratingMarkSet: t.array(t.number),
    factCost: t.type({ start: t.number, end: t.number }),
    factDistance: t.type({ start: t.number, end: t.number }),
    actualDepartureDate: t.type({ start: t.number, end: t.number }),
    waypointWaitTime: t.type({ start: t.number, end: t.number }),
    empty: t.boolean,
  }),
]);

export type AnalysisTaxiQuery = t.TypeOf<typeof AnalysisTaxiQuery>;
