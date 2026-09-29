import { LabeledValue } from 'antd/lib/select';
import * as t from 'io-ts';

import { Employee } from '@sber-sbertransport/mf-core';

import { IOWaypoint } from 'shared/models/geo/types';

import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

import { TransportTypeEnum } from '../../stores/TransportTypes/TransportTypes.interface';
import { TaxiClassEnum } from '../../stores/Trip/Trip.interface';
import { ApprovalsStatusEnum } from './Approval.model';

export enum ApprovalTypeEnum {
  REQUEST = 'TRIP_REQUEST',
  TRIP = 'FINAL_TRIP',
  SHARED_RIDE = 'SHARED_RIDE_JOIN',
  UPDATED_TRIP = 'UPDATE_TRIP_REQUEST',
}
export const activeOptions: LabeledValue[] = [
  { value: 'TRIP_REQUEST', label: 'На согласовании' },
  { value: 'UPDATE_TRIP_REQUEST', label: 'На изменении' },
  { value: 'FINAL_TRIP', label: 'На утверждении' },
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

export const FraudCommentResponseSchema = t.intersection([
  t.type({
    text: t.string,
  }),
  t.partial({
    id: t.string,
    humanReadableId: t.string,
  }),
]);

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
    purpose: t.string,
    purposeId: t.string,
    purposeLabel: t.string,
    taxiClass: ioTypeFromEnum<TaxiClassEnum>('TaxiClassEnum', TaxiClassEnum),
    id: t.string,
    timeZone: t.string,
    passenger: Employee,
    fraudMessage: t.string,
    fraudComment: t.array(FraudCommentResponseSchema),
    coopTrip: t.boolean,
    isCoopTrip: t.boolean,
  }),
]);

export const ResponseApprovalSearch = t.type({
  content: t.array(IOApprovement),
  number: t.number,
  totalElements: t.number,
});

export type TApprovement = t.TypeOf<typeof IOApprovement>;
export type TResponseApprovalSearch = t.TypeOf<typeof ResponseApprovalSearch>;
export type FraudComment = t.TypeOf<typeof FraudCommentResponseSchema>;
