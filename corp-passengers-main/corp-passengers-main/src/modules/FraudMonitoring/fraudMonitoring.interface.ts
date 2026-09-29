import * as t from 'io-ts';

import { SortDirection } from 'constants/constants.app';

import { YandexTaxiRequestStatus } from 'api/yandexTaxiRegistry/yandex-taxi-registry.constants';

import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

const SortDirections = ioTypeFromEnum<SortDirection>('SortDirection', SortDirection);
const Status = ioTypeFromEnum<YandexTaxiRequestStatus>('Status', YandexTaxiRequestStatus);

const Sort = t.type({
  field: t.string,
  direction: SortDirections,
});

const Page = t.type({
  number: t.number,
  size: t.number,
  last: t.boolean,
  first: t.boolean,
  total: t.number,
  count: t.number,
});

const FraudMarker = t.type({
  id: tt.uuid,
  comment: t.string,
});

const Passenger = t.type({
  id: tt.uuid,
  firstName: t.string,
  lastName: t.string,
  patronymic: t.string,
  personnelNumber: t.string,
});

const Approver = t.intersection([
  t.type({
    id: tt.uuid,
    firstName: t.string,
    lastName: t.string,
    personnelNumber: t.string,
  }),
  t.partial({
    patronymic: t.string,
  }),
]);

const Purpose = t.type({
  id: tt.uuid,
  purpose: t.string,
});

export const ReportItem = t.type({
  id: tt.uuid,
  humanReadableId: t.string,
  transportType: t.string,
  fraudMarkers: t.array(FraudMarker),
  tripDate: t.string,
  passenger: Passenger,
  approvalDate: t.string,
  approver: Approver,
  autoApproval: t.boolean,
  intermediatePointsCount: t.number,
  departureAddress: t.string,
  destinationAddress: t.string,
  plannedCost: t.number,
  factCost: t.number,
  status: Status,
  purpose: Purpose,
  department: t.string,
});
export type TReportItem = t.TypeOf<typeof ReportItem>;

export const FraudMonitoringReportResponse = t.intersection([
  t.type({
    content: t.array(ReportItem),
  }),
  t.type({ sort: Sort }),
  t.type({ page: Page }),
]);
export type TFraudMonitoringReportResponse = t.TypeOf<typeof FraudMonitoringReportResponse>;

export const FraudMonitoringDetailsResponse = t.intersection([
  t.type({
    id: t.string,
    humanReadableId: t.string,
    costCenter: t.string,
    transportType: t.string,
    fraudMarkers: t.array(FraudMarker),
    intermediateAddresses: t.array(
      t.type({
        address: t.string,
        waitTime: t.number,
      })
    ),
    desiredDate: t.string,
    tripEndDate: t.string,
    passenger: Passenger,
    approvalDate: t.string,
    approver: Approver,
    autoApproval: t.boolean,
    waypointsCount: t.number,
    departureAddress: t.string,
    destinationAddress: t.string,
    plannedCost: t.number,
    actualCost: t.number,
    distance: t.number,
    status: t.string,
    compensationType: t.string,
    purpose: Purpose,
    department: t.string,
    departmentCode: t.string,
  }),
  t.partial({
    approver: Approver,
  }),
]);

export type TFraudMonitoringDetailsResponse = t.TypeOf<typeof FraudMonitoringDetailsResponse>;

const Filter = t.partial({
  transportType: t.array(t.string),
  humanReadableId: t.string,
  passengerName: t.string,
  approverName: t.string,
  passenger: t.array(t.string),
  approver: t.array(t.string),
  tripDateStart: t.string,
  tripDateEnd: t.string,
  approveDateStart: t.string,
  approveDateEnd: t.string,
  purpose: t.array(t.string),
});

export const FraudMonitoringReportRequest = t.intersection([
  t.partial({ filter: Filter }),
  t.type({
    page: t.number,
    size: t.number,
    sort: t.string,
    direction: SortDirections,
  }),
]);
export type TFraudMonitoringReportRequest = t.TypeOf<typeof FraudMonitoringReportRequest>;
