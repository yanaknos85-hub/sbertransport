import * as t from 'io-ts';
import { ApprovalStateStatuses } from 'shared/models/types';

import * as tt from 'utils/io-ts';
import { UUID } from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { ISpentActionsType } from 'modules/LimitsPage/useDetailedLimitsMapper';

import { TransportTypeEnum } from '../TransportTypes/TransportTypes.interface';
import { LimitEmpRequest, LimitSendRequest } from './LimitsRequest.interface';
import { LimitModel } from './Models/LimitModel';

export interface ILimitsStore {
  limitsIsLoaded: boolean;
  currentLimit: LimitModel[] | undefined;
  employeeLimit: Limit | undefined;
  limitRequestsStats: ISpentActionsType[];
  employeeLimits: LimitModel[];
  limitSharing: LimitSharing[];
  currentEmployeeSharing: LimitSharing[];
  currentDepartmentSharing: LimitSharing[];
  limitRequests: LimitRequestInfo[];
  allLimitRequests: LimitRequestInfo[];
  currentRequest: ISpentActionsType | undefined;
  limitTransferHistory: LimitTransferHistory[];
  limitCostHistory: LimitCostHistory[];
  bonuses: Bonuses | undefined;

  // С хера ли их 2 то???
  depLimits: Limit[];
  departmentLimits: LimitModel[];

  getLimitRequestsStats(): Promise<ISpentActionsType[]>;
  getDepartmentLimits(): Promise<LimitModel[]>;
  getDepartmentLimitsByYear(depId: UUID, year: string): Promise<LimitModel[]>;
  getEmployeeLimits(): Promise<void>;
  getLimitRequest(): Promise<LimitRequestInfo[]>;
  getAllLimitRequests(): Promise<LimitRequestInfo[]>;
  getLimitByDepartment(departmentId: UUID): Promise<Limit[]>;
  approveLimitRequest(data: LimitRequestSavingObject): Promise<number>;
  getLimitSharing(limitId: UUID): Promise<LimitSharing[]>;
  getLimitsIdByDepartmentId(depId: string): string | undefined;
  getEmployeeLimit(): Promise<Limit>;
  setCurrentRequest(id: string): void;
  cancelLimitRequest(data: { requestId: UUID; description: string }): Promise<void>;
  changeDepLimitRequest(data: LimitSendRequest, requestId: string): Promise<number>;
  changeEmpLimitRequest(data: LimitEmpRequest, requestId: string): Promise<number>;
  initStore(): void;
  getLimits(): void;
  refreshLimits(): void;
  getLimitTransferHistory(limitId: string | UUID, year: number, maxRecords: number): Promise<LimitTransferHistory[]>;
  getLimitCostHistory(
    limitId: string | UUID,
    maxRecords: number,
    organizationId: string | UUID
  ): Promise<LimitCostHistory[]>;
  getAccountBonuses(): Promise<Bonuses>;
}

export interface ILimitsService {
  getDepartmentLimits(): Promise<Limit[]>;
  getDepartmentLimitsByYear(depId: UUID, year: string): Promise<Limit>;
  getEmployeeLimits(): Promise<Limit[]>;
  getLimitSharing(limitId: string): Promise<LimitSharing[]>;
  getEmployeeLimit(employeeId: string, year: number): Promise<Limit>;
  getLimitRequestsStats(): Promise<ISpentActionsType[]>;
  getLimitRequest(): Promise<LimitRequestInfo[]>;
  cancelLimitRequest(data: { requestId: UUID; description: string }): Promise<number>;
  getAllLimitRequests(): Promise<LimitRequestInfo[]>;
  getLimitByDepartment(departmentId: UUID): Promise<Limit[]>;
  approveLimitRequest(data: LimitRequestSavingObject): Promise<number>;
  changeDepLimitRequest(data: LimitSendRequest, requestId: string): Promise<number>;
  changeEmpLimitRequest(data: LimitEmpRequest, requestId: string): Promise<number>;
  getLimitTransferHistory(limitId: string | UUID, year: number, maxRecords: number): Promise<LimitTransferHistory[]>;
  getLimitCostHistory(
    limitId: string | UUID,
    maxRecords: number,
    organizationId: string | UUID
  ): Promise<LimitCostHistory[]>;
  getAccountBonuses(ownerId: string): Promise<Bonuses>;
}

export interface ILimitDetailed {
  id: string;
  value: ILimitValueDetailed;
  fromDate: string;
  toDate: string;
  transportTypeId: string;
}

export const IOLimitValue = t.type({
  initial: t.number,
  spent: t.number,
  reserved: t.number,
});

export type TLimitValue = t.TypeOf<typeof IOLimitValue>;

export enum LIMIT_TYPE {
  DEPARTMENT = 'DEPARTMENT',
  EMPLOYEE = 'EMPLOYEE',
}

export const LimitTypeTitles = {
  [LIMIT_TYPE.EMPLOYEE]: 'Личный лимит',
  [LIMIT_TYPE.DEPARTMENT]: 'На подразделение',
};

export enum LIMIT_STATUS {
  PLANNING = 'PLANNING',
  SHARED = 'SHARED',
  CLOSED = 'CLOSED',
  CANCELED = 'CANCELED',
}

const limitStatus = ioTypeFromEnum<LIMIT_STATUS>('limitStatus', LIMIT_STATUS);

type limitStatus = t.TypeOf<typeof limitStatus>;

export enum LIMIT_SHARING_TYPE {
  MONTHLY = 'MONTHLY',
  QUARTER = 'QUARTER',
  PERCENTS = 'PERCENTS',
}

export enum LIMIT_SHARING_TYPE_TITLES {
  MONTHLY = 'На каждый месяц',
  QUARTER = 'Поквартально',
  PERCENTS = 'Проценты',
}

const limitSharingType = ioTypeFromEnum<LIMIT_SHARING_TYPE>('limitSharingType', LIMIT_SHARING_TYPE);

type limitSharingType = t.TypeOf<typeof limitSharingType>;

export enum LIMIT_SERVICE_TYPE {
  PASSENGER = 'PASSENGER',
  CARGO = 'CARGO',
}

const limitServiceType = ioTypeFromEnum<LIMIT_SERVICE_TYPE>('limitServiceType', LIMIT_SERVICE_TYPE);

type limitServiceType = t.TypeOf<typeof limitServiceType>;

export const limitSharingPerPeriodDTO = t.type({
  author: t.string,
  balance: t.number,
  creationTime: t.string,
  id: t.string,
  limitSharing: t.string,
  periodNumber: t.number,
  sum: t.number,
});

export const LimitSharing = t.intersection([
  t.strict({
    id: tt.uuid,
    author: tt.uuid,
    creationTime: t.string,
    transportType: t.string,
    sum: tt.money,
    balance: tt.money,
    limitId: tt.uuid,
  }),
  t.partial({ limitSharingPerPeriodDTO }),
]);

export type LimitSharing = t.TypeOf<typeof LimitSharing>;

const common = {
  year: t.number,
  sum: tt.money,
  limitSharingType,
  limitServiceType,
  finalSharing: t.boolean,
  useThisLimit: t.boolean,
};

const employee = t.strict({
  id: tt.uuid,
  humanReadableId: t.string,
  firstName: t.string,
  lastName: t.string,
  personnelNumber: t.string,
});

const department = t.strict({
  id: tt.uuid,
  code: t.string,
  departmentName: t.string,
});

export type Department = t.TypeOf<typeof department>;

const limitCommon = {
  id: tt.uuid,
  humanReadableId: t.string,
  limitOwner: tt.uuid,
  reserve: tt.money,
  limitStatus,
};

export const Limit = t.intersection([
  t.strict({
    ...common,
    ...limitCommon,
    limitType: t.union([t.literal(LIMIT_TYPE.EMPLOYEE), t.literal(LIMIT_TYPE.DEPARTMENT)]),
  }),
  t.partial({
    employee, department, parentLimitId: tt.uuid,
  }),
]);

export type Limit = t.TypeOf<typeof Limit>;

export type DepartmentLimit = Limit & { limitType: LIMIT_TYPE.DEPARTMENT };
export type EmployeeLimit = Limit & { limitType: LIMIT_TYPE.EMPLOYEE };

export interface ILimitAction {
  actionDate: string;
  sum: number;
}

export interface ILimitEmployeeAction {
  employeeId: string;
  spent: ILimitAction[];
  reserved: ILimitAction[];
}

export interface ILimitValueDetailed {
  initial: number;
  actions: ILimitEmployeeAction[];
}

export const LimitColorsPercent = t.strict({
  name: t.string,
  value: t.string,
});

export type LimitColorsPercent = t.TypeOf<typeof LimitColorsPercent>;

export const IOLimitColorsPercent = t.type({
  name: t.string,
  value: t.string,
});

export type TLimitColorsPercent = t.TypeOf<typeof IOLimitColorsPercent>;

export const author = t.strict({
  id: tt.uuid,
  humanReadableId: t.string,
  firstName: t.string,
  patronymic: tt.optional(t.string),
  lastName: t.string,
  personnelNumber: t.string,
  positionId: tt.optional(t.string),
  organizationId: tt.optional(tt.uuid),
});

export const ApproverDTOList = t.strict({
  id: tt.uuid,
  departmentId: tt.uuid,
  employeeId: tt.uuid,
  limitRequestId: tt.uuid,
  sum: tt.money,
  approvalState: ApprovalStateStatuses,
});

export enum LIMIT_REQUEST_STATUS {
  INIT = 'INIT',
  DONE_FULLY = 'DONE_FULLY',
  DONE_PARTLY = 'DONE_PARTLY',
  CANCELLED = 'CANCELLED',
  DECLINED = 'DECLINED',
}

export enum LimitRequestTitlesEnum {
  INIT = 'Открыта',
  DONE_FULLY = 'Выполнена полностью',
  DONE_PARTLY = 'Выполнена частично',
  CANCELLED = 'Отменена',
  DECLINED = 'Закрыта',
}

export enum LIMIT_REQUEST_LEVEL {
  SIBLINGS = 'SIBLINGS',
  PARENT = 'PARENT',
}

export enum LIMIT_REQUEST_LEVEL_TITLES {
  SIBLINGS = 'Смежные подразделения',
  PARENT = 'Вышестоящее подразделение',
}

export enum LimitRequestStatusesTitlesEnum {
  INIT = 'На согласовании',
  DONE_FULLY = 'Выполнена полностью',
  DONE_PARTLY = 'Выполнена частично',
  CANCELLED = 'Отменена',
  DECLINED = 'Закрыта',
}

export const LimitRequestData = t.strict({
  id: tt.uuid,
  humanReadableId: t.string,
  year: t.number,
  period: t.number,
  transportType: ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum),
  sum: tt.money,
  description: tt.optional(t.string),
  declineReason: tt.optional(t.string),
  status: ioTypeFromEnum<LIMIT_REQUEST_STATUS>('LIMIT_REQUEST_STATUS', LIMIT_REQUEST_STATUS),
  creationTime: t.string,
  limitType: ioTypeFromEnum<LIMIT_TYPE>('LIMIT_TYPE', LIMIT_TYPE),
});

export const LimitRequestInfo = t.intersection([
  LimitRequestData,
  t.type({
    approverDtoList: t.array(ApproverDTOList),
    author,
  }),
  t.partial({
    limitId: tt.uuid || t.string,
    limitSum: tt.money,
    limitBalance: tt.money,
    limitHumanreadableid: t.string || tt.uuid,
    plannedSum: tt.money,
    askTargets: tt.oneOf(['PARENT', 'SIBLINGS']),
    limitSharingType: ioTypeFromEnum<LIMIT_SHARING_TYPE>('LIMIT_SHARING_TYPE', LIMIT_SHARING_TYPE),
  }),
]);

export type LimitRequestInfo = t.TypeOf<typeof LimitRequestInfo>;

export const LimitRequestSavingObject = t.type({
  requestId: tt.uuid,
  sum: tt.money,
  approvalState: ApprovalStateStatuses,
});

export type LimitRequestSavingObject = t.TypeOf<typeof LimitRequestSavingObject>;

export const LimitRequestSaving = t.type({
  sum: tt.money,
  year: t.number,
  description: t.string,
  period: t.number,
  transportType: t.string,
});

export type LimitRequestSaving = t.TypeOf<typeof LimitRequestSaving>;

export const DepSiblings = t.strict({
  id: tt.uuid,
  humanReadableId: t.string,
  organizationId: tt.uuid,
  code: t.string,
  departmentName: t.string,
  departmentHeadId: tt.uuid,
  parentId: tt.uuid,
  active: t.boolean,
});

export type DepSiblings = t.TypeOf<typeof DepSiblings>;

export enum ECONOMY_HISTORY_TYPE {
  FROM_ECONOMY = 'FROM_ECONOMY',
  TO_ECONOMY = 'TO_ECONOMY',
  GENERAL = 'GENERAL',
}

export const LimitTransferHistory = t.strict({
  id: tt.uuid,
  author: t.string,
  creationTime: t.string,
  sum: tt.money,
  sourceLimit: tt.uuid,
  targetLimit: tt.uuid,
  sourceTransportType: ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum),
  targetTransportType: ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum),
  year: t.number,
  period: tt.optional(t.number),
  historyType: ioTypeFromEnum<ECONOMY_HISTORY_TYPE>('ECONOMY_HISTORY_TYPE', ECONOMY_HISTORY_TYPE),
});

export type LimitTransferHistory = t.TypeOf<typeof LimitTransferHistory>;

export const LimitCostHistory = t.strict({
  limitId: tt.uuid,
  sumReserved: tt.money,
  sumSpent: tt.optional(tt.money),
});

export type LimitCostHistory = t.TypeOf<typeof LimitCostHistory>;

export enum BonusOperation {
  DEPOSIT = 'DEPOSIT',
  SPEND = 'SPEND',
}

export enum BonusStatus {
  RESERVED = 'RESERVED',
  DONE = 'DONE',
  CANCELED = 'CANCELED',
}

export const BonusesRequest = t.type({
  id: tt.uuid,
  sum: t.number,
  operation: ioTypeFromEnum<BonusOperation>('BonusOperation', BonusOperation),
  status: ioTypeFromEnum<BonusStatus>('BonusStatus', BonusStatus),
  updateTime: t.string,
  reason: t.string,
});

export const Bonuses = t.type({
  ownerId: tt.uuid,
  sum: t.number,
  balance: t.number,
  requests: t.array(BonusesRequest),
});

export type Bonuses = t.TypeOf<typeof Bonuses>;
export type BonusesRequest = t.TypeOf<typeof BonusesRequest>;
