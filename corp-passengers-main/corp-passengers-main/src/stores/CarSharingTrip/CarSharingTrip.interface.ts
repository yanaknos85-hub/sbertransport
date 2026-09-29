import * as t from 'io-ts';
import { Purpose } from 'stores/Trip/Trip.interface';
import * as tt from 'utils/io-ts';
import { UUID } from 'utils/io-ts';
import { RangeNumber } from 'api/register-search';
import { PageSetting, SortSetting } from 'modules/CarSharingRegistry/types';
import { createPagination } from 'stores/Pagination/Pagination.interface';
import { Value } from 'shared/components/DateInput/types';

export const DistanceRange = t.partial({
  start: t.number,
  end: t.number,
});

export const DateRange = t.strict({
  start: tt.EpochMS,
  end: tt.EpochMS,
});

export type DateRange = t.TypeOf<typeof DateRange>;

export const DateRangeISO = t.strict({
  start: t.string,
  end: t.string,
});

export type DateRangeISO = t.TypeOf<typeof DateRangeISO>;

export type DistanceRange = t.TypeOf<typeof DistanceRange>;

const TripRequestReport = t.intersection([
  t.type({
    id: tt.uuid,
    humanReadableId: tt.nullable(t.string),
    desiredDate: tt.nullable(t.number),
    approveDate: tt.nullable(t.number),
    status: t.string,
    creationTime: tt.nullable(t.number),
    department: t.intersection([
      t.strict({ id: tt.uuid }),
      t.partial({ code: t.string }),
      t.partial({ departmentName: t.string }),
    ]),
    position: tt.nullable(t.string),
    purpose: tt.nullable(Purpose),
    coopTrip: tt.nullable(t.boolean),
  }),
  t.partial({
    costCenter: tt.nullable(t.string),
    organization: tt.nullable(t.string),
    contractor: tt.nullable(t.string),
    rentId: tt.nullable(t.string),
    fio: tt.nullable(t.string),
    personnelNumber: tt.nullable(t.string),
    position: tt.nullable(t.string),
    phoneNumber: tt.nullable(t.string),
    timeZone: tt.nullable(t.string),
    car: tt.nullable(t.string),
    rentCreatedTime: tt.nullable(t.number),
    rentFinishedTime: tt.nullable(t.number),
    startAddress: tt.nullable(t.string),
    finishAddress: tt.nullable(t.string),
    expectedTime: tt.nullable(t.number),
    reserveTime: tt.nullable(t.number),
    drivingTime: tt.nullable(t.number),
    parkingTime: tt.nullable(t.number),
    expectedDistance: tt.nullable(t.number),
    drivingLength: tt.nullable(t.number),
    reserveTimeCost: tt.nullable(t.number),
    expectedCost: tt.nullable(t.number),
    drivingTimeCost: tt.nullable(t.number),
    parkingTimeCost: tt.nullable(t.number),
    drivingLengthCost: tt.nullable(t.number),
    totalCost: tt.nullable(t.number),
    tariff: tt.nullable(t.string),
    passengerDepartment1: tt.nullable(t.string),
    passengerDepartment2: tt.nullable(t.string),
    passengerDepartment3: tt.nullable(t.string),
    passengerDepartment4: tt.nullable(t.string),
    passengerDepartment5: tt.nullable(t.string),
    passengerDepartment6: tt.nullable(t.string),
  }),
]);

export type TripRequestReport = t.TypeOf<typeof TripRequestReport>;

export const CarSharingSearchResponse = createPagination(TripRequestReport);

export type CarSharingSearchResponse = t.TypeOf<typeof CarSharingSearchResponse>;

export const Filters = t.intersection([
  t.type({
    pageSetting: t.type({ page: t.number, size: t.number }),
  }),
  t.partial({
    organizationId: tt.uuid,
    requestHumanId: t.string,
    desiredDate: DateRangeISO,
    requestStatusSet: t.array(t.string),
    creationDate: DateRangeISO,
    purposeSet: t.array(t.type({ id: t.string })),
    expectedCost: RangeNumber,
    expectedDistance: DistanceRange,
    employeeFIO: t.string,
    costCenter: t.string,
    tariffIdSet: t.array(t.string),
    contractorSet: t.array(tt.uuid),
    employeePositionSet: t.array(tt.uuid),
    departmentCode: t.string,
    organizationSetting: t.partial({ employeeOrganizationSet: t.array(t.string) }),
    departureAddress: t.string,
    destinationAddress: t.string,
    personnelNumber: t.string,
    employeeItinerantTypeSet: t.array(t.string),
    sortSetting: SortSetting,
    ratingMarkSet: t.array(t.number),
    finishedDate: DateRangeISO,
    empty: t.boolean,
    approveDate: DateRangeISO,
    requestClosedDatetime: DateRangeISO,
  }),
]);

export type CarSharingReportFilters = t.TypeOf<typeof Filters>;

export interface CarSharingSearchQuery {
  requestHumanId?: UUID;
  creationDate?: Value;
  desiredDate?: Value;
  requestStatusSet?: string[];
  expectedCost?: number | RangeNumber;
  passengerCountSet?: number[];
  contractorSet?: UUID[];
  tariffIdSet?: string | number;
  drivingLength?: number | RangeNumber;
  ratingMarkSet?: number[];
  costCenter?: string;
  employeeFIO?: string;
  personnelNumber?: string;
  departmentCode?: string;
  employeeDepartmentI?: string;
  employeeDepartmentII?: string;
  employeeDepartmentIII?: string;
  employeeDepartmentIV?: string;
  employeeDepartmentV?: string;
  employeeDepartmentVI?: string;
  sortSetting?: SortSetting;
  pageSetting?: PageSetting;
  purposeSet?: string[];
  savings?: string;
  requestClosedDatetime?: Value;
  department1?: string[];
  department2?: string[];
  department3?: string[];
  department4?: string[];
  department5?: string[];
  department6?: string[];
  employeeOrganizationSet?: string[];
}
