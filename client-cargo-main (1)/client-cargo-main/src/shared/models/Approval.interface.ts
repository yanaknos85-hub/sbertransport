import { LabeledValue } from 'antd/lib/select';
import * as t from 'io-ts';
import { IOWaypoint } from 'shared/models/geo/types';

import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

import { TransportTypeEnum } from '../../stores/TransportTypes/TransportTypes.interface';
import { TaxiClassEnum } from '../../stores/Trip/Trip.interface';
import { ApprovalsStatusEnum } from './Approval.model';

export enum ApprovalTypeEnum {
  REQUEST = 'TRIP_REQUEST',
  TRIP = 'FINAL_TRIP',
  SHARED_RIDE = 'SHARED_RIDE_JOIN',
  UPDATED_TRIP = 'UPDATED_TRIP_REQUEST',
}
export const activeOptions: LabeledValue[] = [
  { value: 'NEW', label: 'На согласовании' },
  { value: 'EDITED', label: 'На изменении' },
  { value: 'TRIP_REQUEST', label: 'На утверждении' },
  { value: 'SHARED_RIDE_JOIN', label: 'Совместная поездка' },
];
export const closedOptions: LabeledValue[] = [
  { value: 'APPROVED', label: 'Согласовано' },
  { value: 'CANCELLED', label: 'Отменено' },
  { value: 'DECLINED', label: 'Отклонено' },
];
export interface ApprovalSetting {
  status: string | string[];
  type?: string;
  transportType?: TransportTypeEnum;
  page?: number;
  size?: number;
}
export const activeSettings = ['NEW', 'EDITED'];
export const closedSettings = ['APPROVED', 'DECLINED', 'CANCELLED'];
export const IOApprovement = t.intersection([
  t.type({
    id: t.string,
    cost: t.number,
    desiredDate: t.string,
    expectedTime: t.number,
    expectedDistance: t.number,
    passengerCount: t.number,
    requestHumanReadableId: t.string,
    waypoints: t.array(IOWaypoint),
    transportType: ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum),
    status: ioTypeFromEnum<ApprovalsStatusEnum>('ApprovalsStatusEnum', ApprovalsStatusEnum),
    requestId: t.string,
  }),
  t.partial({
    passengerId: t.string,
    type: ioTypeFromEnum<ApprovalTypeEnum>('ApprovalTypeEnum', ApprovalTypeEnum),
    restOfLimit: t.number,
    purpose: t.string,
    purposeId: t.string,
    purposeLabel: t.string,
    taxiClass: ioTypeFromEnum<TaxiClassEnum>('TaxiClassEnum', TaxiClassEnum),
    id: t.string,
  }),
]);

export const ResponseApprovalSearch = t.type({
  content: t.array(IOApprovement),
  number: t.number,
  totalElements: t.number,
});

export type TApprovement = t.TypeOf<typeof IOApprovement>;
export type TResponseApprovalSearch = t.TypeOf<typeof ResponseApprovalSearch>;
