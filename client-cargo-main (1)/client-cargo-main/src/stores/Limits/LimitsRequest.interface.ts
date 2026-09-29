import * as t from 'io-ts';

import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

import { TransportTypeEnum } from '../TransportTypes/TransportTypes.interface';
import { DepSiblings } from './Limit.interface';
import { LimitRequestModel } from './Models/LimitRequest.model';

export interface ILimitsRequestService {
  getLimitRequestList(): Promise<TLimitRequestNew[]>;
  getLimitRequest(reqId: string): Promise<LimitRequestModel>;
  editLimitRequest(reqId: string, data: TLimitRequestNew): Promise<number>;
  approveLimitRequest(reqId: string): Promise<number>;
  cancelLimitRequest(reqId: string, reason: string): Promise<number>;
  deleteLimitRequest(reqId: string): Promise<number>;
  getDepSiblings(data: SiblingsParams): Promise<DepSiblings[]>;
  addLimitRequest(data: LimitSendRequest): Promise<number>;
  addEmpLimitRequest(data: LimitEmpRequest): Promise<number>;
}

export interface ILimitsRequestStore {
  list: LimitRequestModel[];
  currentRequest: LimitRequestModel | undefined;
  siblings: DepSiblings[];
  setCurrentRequest(id: string): void;
  cancelRequest(id: string, reason: string): void;
  getList(): Promise<void>;
  createRequest(data: LimitSendRequest): void;
  addLimitRequest(data: LimitSendRequest): Promise<number>;
  addEmpLimitRequest(data: LimitEmpRequest): Promise<number>;
  editLimitRequest(reqId: string, data: TLimitRequestNew): void;
  getRequest(reqId: string): Promise<LimitRequestModel>;
  initStore(): void;
  getDepSiblings(data: SiblingsParams): Promise<DepSiblings[]>;
}

export const SiblingsParams = t.strict({
  departmentId: t.string,
  percent: t.number,
  transportType: t.string,
  year: t.number,
  sum: tt.money,
});

export type SiblingsParams = t.TypeOf<typeof SiblingsParams>;

export enum LimitRequestStatusEnum {
  AWAITING_APPROVAL = 'AWAITING_APPROVAL',
  APPROVED = 'APPROVED',
  DONE = 'DONE',
  CANCELLED = 'CANCELLED',
}

export enum LimitRequestStatusTitlesEnum {
  AWAITING_APPROVAL = 'Ожидает согласования',
  APPROVED = 'Согласована',
  DONE = 'Выполнена',
  CANCELLED = 'Отменена',
}

export enum LimitRequestApprovalStateEnum {
  AWAITING_APPROVAL = 'AWAITING_APPROVAL',
  APPROVED = 'APPROVED',
  DECLINED = 'DECLINED',
}

export enum LimitRequestApprovalStateTitlesEnum {
  AWAITING_APPROVAL = 'Ожидает согласования',
  APPROVED = 'Согласована',
  DECLINED = 'Отклонена',
}

export const LimitRequestStatusesCancellable = [LimitRequestStatusEnum.AWAITING_APPROVAL];

export const LimitRequestStatusesFinal = [
  LimitRequestStatusEnum.APPROVED,
  LimitRequestStatusEnum.CANCELLED,
  LimitRequestStatusEnum.DONE,
];

export const IOLimitRequestNew = t.intersection([
  t.type({
    sum: t.number,
    id: t.string,
    humanReadableId: t.string,
    creationTime: t.union([t.string, t.number]),
  }),
  t.partial({
    month: t.number,
    transportType: ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum),
    status: t.string,
    authorId: t.string,
    approvalState: t.string,
    description: t.string,
    askTargets: t.string,
    departments: t.array(t.string),
  }),
]);

export type TLimitRequestNew = t.TypeOf<typeof IOLimitRequestNew>;

export const LimitGeneralRequest = t.strict({
  year: t.number,
  period: t.number,
  transportType: t.string,
  askTargets: t.string,
  sum: tt.money,
  description: t.string,
});

export const LimitSendRequest = t.intersection([LimitGeneralRequest, t.partial({ departments: t.array(tt.uuid) })]);

export type LimitSendRequest = t.TypeOf<typeof LimitSendRequest>;

export const LimitEmpRequest = t.strict({
  year: t.number,
  period: t.number,
  transportType: t.string,
  sum: tt.money,
  description: t.string,
});

export type LimitEmpRequest = t.TypeOf<typeof LimitEmpRequest>;
