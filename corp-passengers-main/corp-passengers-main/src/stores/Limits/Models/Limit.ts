import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

import { TransportType } from '../../TransportTypes/TransportTypes.interface';

export enum LIMIT_TYPE {
  DEPARTMENT = 'DEPARTMENT',
  EMPLOYEE = 'EMPLOYEE',
}

export const limitType = ioTypeFromEnum<LIMIT_TYPE>('limitType', LIMIT_TYPE);

type limitType = t.TypeOf<typeof limitType>;

export enum LIMIT_STATUS {
  PLANNING = 'PLANNING',
  SHARED = 'SHARED',
  CLOSED = 'CLOSED',
}

export enum LIMIT_REQUEST_STATUS {
  PLANNING = 'PLANNING',
  SHARED = 'SHARED',
  CLOSED = 'CLOSED',
  INIT = 'INIT',
  DECLINED = 'DECLINED',
  DONE_FULLY = 'DONE_FULLY',
  DONE_PARTLY = 'DONE_PARTLY',
  CANCELLED = 'CANCELLED',
}

export const Params = t.strict({
  id: tt.uuid,
});

export type Params = t.TypeOf<typeof Params>;

export const limitStatus = ioTypeFromEnum<LIMIT_STATUS>('limitStatus', LIMIT_STATUS);

export type limitStatus = t.TypeOf<typeof limitStatus>;

export const limitRequestStatus = ioTypeFromEnum<LIMIT_REQUEST_STATUS>('limitRequestStatus', LIMIT_REQUEST_STATUS);
export type limitRequestStatus = t.TypeOf<typeof limitRequestStatus>;

export enum LIMIT_SHARING_TYPE {
  MONTHLY = 'MONTHLY',
  QUARTER = 'QUARTER',
  PERCENTS = 'PERCENTS',
}

const limitSharingType = ioTypeFromEnum<LIMIT_SHARING_TYPE>('limitSharingType', LIMIT_SHARING_TYPE);

type limitSharingType = t.TypeOf<typeof limitSharingType>;

export enum LIMIT_SERVICE_TYPE {
  PASSENGER = 'PASSENGER',
  CARGO = 'CARGO',
  REPAIR = 'REPAIR',
}

export const limitServiceType = ioTypeFromEnum<LIMIT_SERVICE_TYPE>('limitServiceType', LIMIT_SERVICE_TYPE);

export type limitServiceType = t.TypeOf<typeof limitServiceType>;

export const limitSharingPerPeriodDTO = t.strict({
  id: tt.uuid,
  author: tt.uuid,
  creationTime: t.string,
  sum: tt.money,
  balance: tt.money,
  periodNumber: t.number,
  limitSharing: tt.uuid,
  sumReservedForCurrentPeriod: tt.optional(t.number),
  sumResharingsPeriod: tt.optional(tt.money),
});
export type limitSharingPerPeriodDTO = t.TypeOf<typeof limitSharingPerPeriodDTO>;

export const LimitSharing = t.strict({
  id: tt.uuid,
  author: tt.uuid,
  creationTime: t.string,
  transportType: t.string,
  sum: tt.money,
  balance: tt.money,
  limitId: tt.uuid,
  limitSharingPerPeriodDTO: tt.optional(limitSharingPerPeriodDTO),
  sumResharingsYear: tt.optional(tt.money),
});

export type LimitSharing = t.TypeOf<typeof LimitSharing>;

export const PercentageSharing = t.strict({
  id: tt.uuid,
  author: tt.uuid,
  creationTime: t.string,
  month0: t.number,
  month1: t.number,
  month2: t.number,
  month3: t.number,
  month4: t.number,
  month5: t.number,
  month6: t.number,
  month7: t.number,
  month8: t.number,
  month9: t.number,
  month10: t.number,
  month11: t.number,
  limitId: tt.uuid,
});

export type PercentageSharing = t.TypeOf<typeof PercentageSharing>;

export type Percentages = [
  number,
  number,
  number,
  number,
  number,
  number,
  number,
  number,
  number,
  number,
  number,
  number
];

export const NewPercentageSharing = t.strict({
  month0: t.number,
  month1: t.number,
  month2: t.number,
  month3: t.number,
  month4: t.number,
  month5: t.number,
  month6: t.number,
  month7: t.number,
  month8: t.number,
  month9: t.number,
  month10: t.number,
  month11: t.number,
  limitId: tt.uuid,
});

export type NewPercentageSharing = t.TypeOf<typeof NewPercentageSharing>;

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
  patronymic: t.string,
  personnelNumber: t.string,
});

export const department = t.strict({
  id: tt.uuid,
  code: t.string,
  departmentName: t.string,
  humanReadableId: t.string,
});

const Owner = t.strict({
  id: tt.uuid,
  humanReadableId: t.string,
  firstName: t.string,
  lastName: t.string,
  patronymic: tt.nullable(t.string),
  personnelNumber: t.string,
  positionId: tt.uuid,
  organizationId: tt.uuid,
});

export type OwnerType = t.TypeOf<typeof Owner>;

const limitCommon = {
  id: tt.uuid,
  humanReadableId: t.string,
  limitOwner: tt.optional(tt.nullable(tt.uuid)),
  owner: tt.optional(tt.nullable(Owner)),
  limitStatus,
  parentLimitId: tt.optional(tt.nullable(tt.uuid)),
  limitSharingDTOList: tt.nullable(t.array(LimitSharing)),
  year: t.number,
};

export const EconomyRow = t.strict({
  department: t.string,
  period: t.number,
  sumEconomy: tt.money,
  sumPerPeriod: tt.money,
  transportType: t.string,
  percent: t.number,
});
export type EconomyRow = t.TypeOf<typeof EconomyRow>;

export const LimitSettingData = t.strict({
  name: t.string,
  value: t.string,
});

export type LimitSettingData = t.TypeOf<typeof LimitSettingData>;

export const EconomyResharingRow = t.strict({
  id: t.string,
  author: t.string,
  creationTime: t.string,
  sum: tt.money,
  sourceLimit: tt.nullable(t.string),
  targetLimit: tt.nullable(t.string),
  sourceTransportType: tt.nullable(t.string),
  targetTransportType: tt.nullable(t.string),
  year: t.number,
  period: tt.optional(t.string),
  historyType: t.string,
});
export type EconomyResharingRow = t.TypeOf<typeof EconomyResharingRow>;

export const LimitByPeriodData = t.strict({
  id: t.string,
  author: t.string,
  creationTime: t.string,
  sum: t.number,
  balance: t.number,
  periodNumber: t.number,
  limitSharing: t.string,
});
export type LimitByPeriodData = t.TypeOf<typeof LimitByPeriodData>;

// todo should be deleted after adding data
export const AddEconomyHistory = t.strict({
  sum: tt.money,
  sourceLimit: t.string,
  targetLimit: t.string,
  sourceTransportType: t.string,
  targetTransportType: t.string,
  year: t.number,
  period: t.number,
  historyType: t.string,
});
export type AddEconomyHistory = t.TypeOf<typeof AddEconomyHistory>;

export const ColorLimitSettings = t.strict({
  emp_LIMIT_GREEN_FROM: t.number,
  emp_LIMIT_GREEN_UNTIL: t.number,
  emp_LIMIT_YELLOW_FROM: t.number,
  emp_LIMIT_YELLOW_UNTIL: t.number,
  emp_LIMIT_RED_FROM: t.number,
  emp_LIMIT_RED_UNTIL: t.number,
  dep_LIMIT_GREEN_FROM: t.number,
  dep_LIMIT_GREEN_UNTIL: t.number,
  dep_LIMIT_YELLOW_FROM: t.number,
  dep_LIMIT_YELLOW_UNTIL: t.number,
  dep_LIMIT_RED_FROM: t.number,
  dep_LIMIT_RED_UNTIL: t.number,
});

export type ColorLimitSettings = t.TypeOf<typeof ColorLimitSettings>;

const LimitTypesUnion = t.union([
  t.strict({ limitType: t.literal(LIMIT_TYPE.EMPLOYEE), employee }),
  t.strict({
    limitType: t.literal(LIMIT_TYPE.DEPARTMENT),
    department,
    reserve: tt.money,
    economy: tt.money,
    isUseUserDepLimit: tt.optional(t.boolean),
  }),
]);

export const LimitWithoutSharing = t.intersection([t.strict({ ...common, ...limitCommon }), LimitTypesUnion]);

export type LimitWithoutSharing = t.TypeOf<typeof LimitWithoutSharing>;

export const LimitWithoutOwner = t.intersection([
  t.strict({
    ...common, ...limitCommon, owner: t.any,
  }),
  LimitTypesUnion,
]);

export type LimitWithoutOwner = t.TypeOf<typeof LimitWithoutOwner>;

export const Limit = t.intersection([
  LimitWithoutSharing,
  t.type({ sharing: t.array(LimitSharing), percentageSharing: t.array(PercentageSharing) }),
  t.partial({
    employee,
    limitSharingDTOList: tt.optional(t.any),
  }),
]);

export const LimitTypeDepartments = t.intersection([Limit, t.type({ department })]);

export const LimitByRequest = t.intersection([
  LimitWithoutSharing,
  t.partial({
    creationTime: tt.nullable(t.union([t.number, t.string])),
    limitSharingDTOList: tt.nullable(t.array(LimitSharing)),
  }),
]);

export type LimitByRequest = t.TypeOf<typeof LimitByRequest>;

export type DepartmentLimit = Limit & { limitType: LIMIT_TYPE.DEPARTMENT };
export type EmployeeLimit = Limit & { limitType: LIMIT_TYPE.EMPLOYEE };

export type Limit = t.TypeOf<typeof Limit>;

export type LimitTypeDepartments = t.TypeOf<typeof LimitTypeDepartments>;

export const CreateDepartmentLimit = t.strict({
  ...common,
  organizationId: tt.uuid,
});

export type CreateDepartmentLimit = t.TypeOf<typeof CreateDepartmentLimit>;

export const limitStatusDescriptions: Record<limitStatus, string> = {
  PLANNING: 'Планируется',
  SHARED: 'Распределен',
  CLOSED: 'Закрыт',
};

export const limitRequestStatusRusName: Record<limitRequestStatus, string> = {
  PLANNING: 'Планируется',
  SHARED: 'Распределена',
  CLOSED: 'Закрыта',
  INIT: 'Создана',
  DECLINED: 'Отклонена',
  DONE_FULLY: 'Согласована',
  CANCELLED: 'Отменена',
  DONE_PARTLY: 'Согласована частично',
};

export const limitServiceTypeDescriptions: Record<limitServiceType, string> = {
  PASSENGER: 'Перевозка сотрудников',
  CARGO: 'Перевозка грузов',
  REPAIR: 'Ремонт',
};

export const limitTypeDescriptions: Record<limitType, string> = {
  DEPARTMENT: 'На подразделение',
  EMPLOYEE: 'На сотрудника',
};

export enum EconomyHistoryTypeEnum {
  TO_ECONOMY = 'TO_ECONOMY',
  FROM_ECONOMY = 'FROM_ECONOMY',
  GENERAL = 'GENERAL',
}

export const limitEconomyHistoryDescriptions: Record<EconomyHistoryTypeEnum, string> = {
  TO_ECONOMY: 'В экономию',
  FROM_ECONOMY: 'Из экономии',
  GENERAL: 'Общий',
};

export const limitSharingTypeDescriptions: Record<limitSharingType, string> = {
  MONTHLY: 'Месяц',
  QUARTER: 'Квартал',
  PERCENTS: 'Процент',
};

export const LimitSettingKV = t.union([
  t.type({ name: t.literal('EMP_LIMIT_GREEN_UNTIL'), value: t.number }),
  t.type({ name: t.literal('EMP_LIMIT_YELLOW_UNTIL'), value: t.number }),
  t.type({ name: t.literal('DEP_LIMIT_GREEN_UNTIL'), value: t.number }),
  t.type({ name: t.literal('DEP_LIMIT_YELLOW_UNTIL'), value: t.number }),
]);

type LimitSettingKV = t.TypeOf<typeof LimitSettingKV>;

export const LimitSettingsRaw = t.array(LimitSettingKV);

export type LimitSettingsRaw = t.TypeOf<typeof LimitSettingsRaw>;

export type LimitSettings = Partial<{ [key in LimitSettingKV['name']]: LimitSettingKV['value'] }>;

export interface Fields {
  transportType: TransportType['name'];
  level: 'siblings' | 'parent';
  reason: string;
  sum: number;
  departments: tt.UUID[];
}

export const LimitRequest = t.strict({
  year: t.number,
  period: t.string,
  transportType: t.string,
  askTargets: t.string,
  sum: tt.money,
  description: t.string,
  departments: t.array(tt.uuid),
});

export type LimitRequest = t.TypeOf<typeof LimitRequest>;

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

export const PersonalItem = t.type({
  targetParentLimit: t.string,
  targetEmployeeId: tt.uuid,
  transportType: t.string,
  sum: tt.money,
  year: t.number,
});

export type PersonalItem = t.TypeOf<typeof PersonalItem>;

export const SiblingsParams = t.strict({
  departmentId: t.string,
  percent: t.number,
  transportType: t.string,
  year: t.number,
  sum: tt.money,
});

export type SiblingsParams = t.TypeOf<typeof SiblingsParams>;

export enum LimitRequestLevel {
  SIBLINGS = 'siblings',
  PARENT = 'parent',
}

export const limitSharingStatsDTOList = t.strict({
  balance: tt.money,
  balancePeriod: tt.money,
  budgetPerYear: tt.money,
  currentDateEconomy: tt.money,
  currentDateEconomyPeriod: tt.money,
  perEmployeeBudget: tt.money,
  perEmployeeSpent: tt.money,
  perEmployeeSpentPeriod: tt.money,
  procentSpent: t.number,
  procentSpentPeriod: t.number,
  procentUsed: t.number,
  procentUsedPeriod: t.number,
  sumReserved: tt.money,
  sumSpent: tt.money,
  sumSpentPeriod: tt.money,
  transportType: t.string,
});

export type LimitSharingStatsDTOList = t.TypeOf<typeof limitSharingStatsDTOList>;

export const LimitStatisticsObject = t.intersection([
  t.strict({
    departmentId: t.string,
    departmentName: t.string,
    departmentLevel: t.number,
    year: t.number,
    employeesNumber: t.number,
    budgetYear: tt.money,
    budgetPeriod: tt.money,
    sumSpentYear: tt.money,
    sumSpentPeriod: t.number,
    sumReservedYear: tt.money,
    sumBalanceYear: tt.money,
    perEmployeeBudget: tt.money,
    perEmployeeSpent: tt.money,
    currentDateEconomy: tt.money,
    procentUsedYear: t.number,

    limitSharingStatsDTOList: t.array(limitSharingStatsDTOList),
  }),
  t.partial({ procentUsedMonth: t.number }),
]);

export type LimitStatisticsObject = t.TypeOf<typeof LimitStatisticsObject>;

export const LimitChildrenDepartment = t.strict({
  ...common,
  ...limitCommon,
  creationTime: t.string,
  department,
  economy: tt.money,
  limitType,
  reserve: tt.money,
});

export type LimitChildrenDepartment = t.TypeOf<typeof LimitChildrenDepartment>;

export const LimitChildrenEmployee = t.strict({
  ...common,
  ...limitCommon,
  creationTime: t.string,
  employee,
  economy: tt.money,
  limitType,
  reserve: tt.money,
});

export type LimitChildrenEmployee = t.TypeOf<typeof LimitChildrenEmployee>;
