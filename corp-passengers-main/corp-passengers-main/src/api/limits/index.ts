/* eslint-disable @typescript-eslint/no-empty-function */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { IHttpService } from '@sber-sbertransport/mf-core';
import * as t from 'io-ts';
import * as R from 'ramda';
import { Dispatch, SetStateAction } from 'react';
import { MutationResultPair, QueryConfig } from 'react-query';
import {
  APIQueryCache, APIQueryResult, updateQueryCache, useAPI, useAPIMutation
} from 'api';
import { AxiosError, AxiosResponse } from 'axios';

import * as tt from 'utils/io-ts';
import { uuid, UUID } from 'utils/io-ts';
import { isJsonString } from 'utils/isJSONString';
import { getErrorMessage, ignore } from 'utils';

import {
  CREATE_DEP_LIMIT,
  DELETE_ALL_LIMITS_IN_CORP_CLIENT,
  DELETE_DEP_LIMIT,
  DEP_LIMIT_SHARE_ECONOMY,
  DEP_LIMIT_SHARE_PRIMARY,
  DEP_LIMIT_SHARE_SECONDARY,
  EMP_LIMITS_MAKE_EMP_LIMITS,
  GET_ALL_LIMIT_REQUESTS_BY_APPROVER,
  GET_ALL_LIMIT_REQUESTS_BY_AUTHOR,
  GET_DEP_LIMITS,
  GET_EMP_LIMITS,
  GET_LIMIT_BY_REQUEST,
  LIMIT_SETTINGS,
  LIMIT_SHARING,
  LIMIT_SHARING_PER_PERIOD,
  LIMIT_SHARING_PERCENTS,
  LIMIT_TRANSFER_HISTORY,
  CURRENT_LIMIT_SHARING,
  CURRENT_LIMIT_SHARING_PERCENTS,
  GET_LIMIT_CHILDREN,
  GET_ASYNC,
  GET_LIMIT_AUDIT,
  GET_LIMIT_ASYNC,
  GET_LIMIT_CLOSE,
  LIMITS
} from 'constants/constants.api';

import {
  AddEconomyHistory,
  ColorLimitSettings,
  CreateDepartmentLimit,
  DepartmentLimit,
  DepSiblings,
  EconomyResharingRow,
  EconomyRow,
  Limit,
  LIMIT_TYPE,
  LimitByPeriodData,
  LimitByRequest,
  LimitRequest,
  LimitSettingData,
  LimitSettings,
  LimitSettingsRaw,
  LimitSharing,
  LimitStatisticsObject,
  LimitWithoutSharing,
  NewPercentageSharing,
  Percentages,
  PercentageSharing,
  PersonalItem,
  LimitWithoutOwner
} from 'stores/Limits/Models/Limit';

import { LimitRequestByAuthor } from 'stores/Limits/Models/LimitRequest';

type CSVSeparator = 'COMMA' | 'SEMICOLON' | 'WHITESPACE' | 'TAB' | 'OTHER';
type FPSeparator = 'COMMA' | 'DOT';

export interface CSVParams {
  csvSeparator: CSVSeparator;
  fpSeparator: FPSeparator;
  includeHeader: boolean;
}

export const Item = t.type({
  targetParentLimit: t.string,
  targetEmployeeId: tt.uuid,
  transportType: t.string,
  sum: tt.money,
});

export const ItemWithYear = t.type({ ...Item.props, year: t.number });

export const ItemShareEconomy = t.type({
  transportType: t.string,
  targetDepartmentId: tt.uuid,
  sum: tt.money,
});

export const SharingPerTransportList = t.strict({
  transportType: t.string,
  sum: tt.money,
});

export type SharingPerTransportList = t.TypeOf<typeof SharingPerTransportList>;

export const SavingItem = t.strict({
  targetDepartmentId: tt.uuid,
  finalSharing: t.boolean,
  // @ts-ignore
  sharingPerTransportList: t.array(SharingPerTransportList),
});

export type SavingItem = t.TypeOf<typeof SavingItem>;

export const ReshareTransferItem = t.type({
  targetLimitId: t.string,
  targetTransportType: tt.uuid,
  sourceLimitId: t.string,
  sourceTransportType: t.string,
  sum: tt.money,
  fromPeriod: t.number,
  toPeriod: t.number,
});

export const TransportTypesSharingItem = t.strict({
  transportType: t.string,
  sum: tt.money,
});

export type TransportTypesSharingItem = t.TypeOf<typeof TransportTypesSharingItem>;

declare module 'api' {
  interface Cache {
    limits: { key: ['limits']; value: Limit[] };
    currentLimit: { key: ['currentLimit']; value: LimitWithoutSharing[] };
    currentLimits: { key: ['currentLimits']; value: LimitWithoutSharing[] };
    sharing: { key: ['sharing']; value: LimitSharing[] };
    sharingPercentages: { key: ['sharingPercentages']; value: PercentageSharing[] };
    limitChildren: { key: ['limitChildren']; value: any };
    limit: { key: ['limit', UUID]; value: Limit };
    personalLimits: { key: ['personalLimits', UUID]; value: Limit[] };
    sharedLimit: { key: ['sharedLimit', UUID]; value: LimitSharing };
    limitSettings: { key: ['limitSettings']; value: LimitSettings };
    limitRequestsByAuthor: { key: ['limitRequestsByAuthor']; value: LimitRequestByAuthor[] };
    economyRows: { key: ['economyRows', UUID, number]; value: EconomyRow[] };
    limitSettingRows: { key: ['limitSettingRows']; value: LimitSettingData[] };
    economyResharingRows: { key: ['economyResharingRows', UUID, number]; value: EconomyResharingRow[] };
    economyPerPeriod: { key: ['economyPerPeriod', number, string]; value: EconomyRow[] };
    limitDataByPeriod: { key: ['limitDataByPeriod', UUID]; value: LimitByPeriodData[] };
    depSiblings: { key: ['depSiblings', UUID, number, string, number, number]; value: DepSiblings[] };
    sharingImitation: { key: ['sharingImitation', UUID, number]; value: number[] };
    limitByDepartmentAndYear: { key: ['limitByDepartmentAndYear', UUID, number]; value: DepartmentLimit | null };
    limitInfographics: { key: ['limitInfographics', UUID, number]; value: LimitStatisticsObject };
    limitByRequestId: { key: ['limitByRequestId', UUID]; value: LimitByRequest | null };
    limitsAudit: { key: ['limitsAudit']; value: string[] };
  }
}

export const useSharing = (limitId: UUID): MutationResultPair<LimitSharing[], unknown, { limitId: UUID }, unknown> => (
  useAPIMutation(
    ({ http, process }) => http
      .get<LimitSharing[]>(CURRENT_LIMIT_SHARING, { urlParams: { limitId } })
      .then(process.getResponseData),
    {
      onSuccess: () => { },
      onError: ({ t, logger }) => {
        logger.toMessage('error', t.ErrorBoundary.defaultError);
      },
    }
  )
);

export const useSharingPercentages = (
  limitId: UUID
): MutationResultPair<PercentageSharing[], unknown, { limitId: UUID }, unknown> => useAPIMutation(
  ({ http, process }) => http
    .get<PercentageSharing[]>(CURRENT_LIMIT_SHARING_PERCENTS, { urlParams: { limitId } })
    .then(process.getResponseData),
  {
    onSuccess: () => { },
    onError: ({ t, logger }) => {
      logger.toMessage('error', t.ErrorBoundary.defaultError);
    },
  }
);

export const useChildrenLimit = <Limits extends Limit[]>(
  limitId: UUID,
  limitType: LIMIT_TYPE
): MutationResultPair<Limits, unknown, { limitId: UUID; limitType: LIMIT_TYPE }, unknown> => useAPIMutation(
    ({ http, process }) => http
      .get<Limits>(`${GET_LIMIT_CHILDREN}`, { urlParams: { limitId, limitType } })
      .then(process.getResponseData),
    {
      onSuccess: () => { },
      onError: ({ t, logger }) => {
        logger.toMessage('error', t.ErrorBoundary.defaultError);
      },
    }
  );

// TODO избавится от данного запроса
export const useCurrentLimit = (): APIQueryResult<LimitWithoutOwner[], Error> => useAPI(['currentLimits'], ({ http, process }) => Promise.all([
  http.get<LimitWithoutOwner[]>(GET_DEP_LIMITS).then(process.decodeResponseData(t.array(LimitWithoutOwner))),
  http.get<LimitWithoutOwner[]>(GET_EMP_LIMITS).then(process.decodeResponseData(t.array(LimitWithoutOwner))),
])
  .then(([depLimits, empLimits]) => [...depLimits, ...empLimits])
  .catch(() => [])
);

// TODO старый запрос, остался как пример, после того как лимиты будут переписаны - удалить
export const useLimits = (): APIQueryResult<Limit[], Error> => useAPI(['limits'], ({ http, process }) => Promise.all([
  http.get<Limit[]>(GET_DEP_LIMITS).then(process.decodeResponseData(t.array(LimitWithoutSharing))),
  http.get<Limit[]>(GET_EMP_LIMITS).then(process.decodeResponseData(t.array(LimitWithoutSharing))),
  http
    .get<LimitSharing[]>(`${LIMIT_SHARING}/full`)
    .then(process.decodeResponseData(t.array(LimitSharing)))
    .catch(() => []),
  http
    .get<PercentageSharing[]>(LIMIT_SHARING_PERCENTS)
    .then(process.decodeResponseData(t.array(PercentageSharing))),
]).then(([depLimits, empLimits, sharings, percentageSharings]) => {
  const allLimits = [...depLimits, ...empLimits];
  const sharingByLimit = R.groupBy(R.prop('limitId'), sharings);
  const pcSharingByLimit = R.groupBy(R.prop('limitId'), percentageSharings);

  return allLimits.map(limit => ({
    ...limit,
    sharing: sharingByLimit[limit.id] ?? [],
    percentageSharing: pcSharingByLimit[limit.id] ?? [],
  }));
})
);

export const refetchLimits = ({ cache }: { cache: APIQueryCache }): Promise<void> => cache.refetchQueries(['limits'], { exact: true }).then(ignore);

export const useCreateDepartmentLimit = (): MutationResultPair<
  LimitWithoutSharing,
  unknown,
  { limit: CreateDepartmentLimit },
  unknown
> => useAPIMutation(
  ({ http, process }, { limit }) => http
    .post<Limit>(`${CREATE_DEP_LIMIT}/add/${limit.organizationId}`, CreateDepartmentLimit.encode(limit))
    .then(process.decodeResponseData(LimitWithoutSharing)),
  {
    onSuccess: ({ cache, result: newLimit }) => {
      updateQueryCache(cache, ['limits'], limits => [...limits, {
        ...newLimit, sharing: [], percentageSharing: [],
      }]);
    },
    onError: ({ error, logger }) => {
      logger.toMessage('error', getErrorMessage(error as AxiosError));
    },
  }
);

export const useDeleteLimit = (): MutationResultPair<void, unknown, { limitId: UUID }, unknown> => useAPIMutation(({ http }, { limitId }) => http.delete(`${DELETE_DEP_LIMIT}/${limitId}`).then(ignore), {
  onSuccess: ({ cache, variables: { limitId } }) => {
    updateQueryCache(cache, ['limits'], limits => limits.filter(({ id }) => id !== limitId));
  },
});

export const useSharePrimary = (): MutationResultPair<
  AxiosResponse<void>,
  unknown,
  { parentLimitId: UUID; items: TransportTypesSharingItem[] },
  unknown
> => useAPIMutation(
  ({ http }, { parentLimitId, items }) => {
    const sharedData = items.map(sharingItem => ({ ...sharingItem, sum: tt.money.encode(sharingItem.sum) }));

    return http.post<void>(`${DEP_LIMIT_SHARE_PRIMARY}/${parentLimitId}`, sharedData);
  },

  { onSuccess: refetchLimits }
);

export const useShareSecondary = (): MutationResultPair<
  UUID[],
  unknown,
  { parentLimitId: UUID; items: SavingItem[] },
  unknown
> => useAPIMutation(
  ({ http, process }, { parentLimitId, items }): Promise<UUID[]> => http
    .post<any>(`${DEP_LIMIT_SHARE_SECONDARY}/${parentLimitId}`, t.array(SavingItem).encode(items))
    .then<UUID[]>(process.decodeResponseData(t.array(uuid))),
  { onSuccess: refetchLimits }
);

export const useUseThisLimit = (): MutationResultPair<
  AxiosResponse<number>,
  unknown,
  {
    limitId: string;
    useThisLimit: boolean;
  },
  unknown
> => useAPIMutation(({ http }, { limitId, useThisLimit }) => http.put(`limits/deplimits/${limitId}/${useThisLimit}`, {}), {
  onSuccess: refetchLimits,
  onError: ({ error, logger }) => {
    logger.toMessage('error', getErrorMessage(error as AxiosError));
  },
});

export const useShareByEmployee = (): MutationResultPair<number, unknown, { items: PersonalItem[] }, unknown> => (
  useAPIMutation(
    ({ http, process }, { items }) => http
      .post<void>(EMP_LIMITS_MAKE_EMP_LIMITS, t.array(ItemWithYear).encode(items))
      .then(res => process.getResponseStatus(res)),
    { onSuccess: refetchLimits }
  )
);

// Получение ВСЕХ ЛИЧНЫХ лимитов сотрудника
export const useGetShareByEmployee = (empLimitId: string): APIQueryResult<Limit[], Error> => useAPI(['personalLimits', empLimitId as UUID], ({ http, process }) => http
  .get<Limit[]>(`${GET_EMP_LIMITS}/getByEmployee/${empLimitId}`, {})
  .then(process.decodeResponseData(t.array(Limit)))
);

// Получение данных личного лимита через id
export const useGetShareByLimit = (empLimitId: string): APIQueryResult<Limit, Error> => useAPI(['limit', empLimitId as UUID], ({ http, process }) => http.get<Limit>(`${GET_EMP_LIMITS}/getByEmployee/${empLimitId}`, {}).then(process.decodeResponseData(Limit))
);

// Удаление личного лимита с распределением
export const useDeleteShareByEmployee = (): MutationResultPair<
  AxiosResponse<void>,
  unknown,
  { empLimitId: string },
  unknown
> => useAPIMutation(({ http }, { empLimitId }) => http.put(`${GET_EMP_LIMITS}/close_emp_limit/${empLimitId}`, {}), {
  onSuccess: refetchLimits,
});

// Увеличение или уменьшение личного лимита с распределением
export const useEditShareByEmployee = (): MutationResultPair<
  AxiosResponse<number>,
  unknown,
  {
    employeeId: string;
    transportTypeEN: string;
    updatedSum: number;
    year: number;
  },
  unknown
> => useAPIMutation(
  ({ http }, {
    employeeId, transportTypeEN, updatedSum, year,
  }) => http.put(`${GET_EMP_LIMITS}/change_emp_limit`, {
    targetEmployeeId: employeeId,
    transportType: transportTypeEN,
    sum: updatedSum,
    year,
  }),
  { onSuccess: refetchLimits } // TODO: log mutation result
);

// Получение  shared лита по его id
export const useGetSharedLimitById = (limitSharingId: string): APIQueryResult<LimitSharing, Error> => useAPI(['sharedLimit', limitSharingId as UUID], ({ http, process }) => http.get<LimitSharing>(`${LIMIT_SHARING}/${limitSharingId}`, {}).then(process.decodeResponseData(LimitSharing))
);

// Удаление данных распределения
export const useDeleteSharing = (): MutationResultPair<
  AxiosResponse<void>,
  unknown,
  { limitSharingId: UUID },
  unknown
> => useAPIMutation(({ http }, { limitSharingId }) => http.delete(`${LIMIT_SHARING}/${limitSharingId}`), {
  onSuccess: ignore,
});

// Изменение данных распределения
export const useEditSharedLimit = (): MutationResultPair<
  AxiosResponse<void>,
  unknown,
  {
    limitSharingId: string;
    transportType: string;
    sum: number;
    parentLimitId: string;
    targetDepartmentId: string;
    targetEmployeeId: string;
  },
  unknown
> => useAPIMutation(({ http }, { limitSharingId: id, ...sharing }) => http.put(`${LIMIT_SHARING}/${id}`, sharing), {
  onSuccess: ignore, // TODO: log mutation result
});

// Изменение лимита подразделения
export const useEditDepLimit = (): MutationResultPair<
  AxiosResponse<void>,
  unknown,
  {
    limitId: string;
    year: number;
    limitSharingType: string;
    limitServiceType: string;
    sum: number;
    parentId: string;
    targetDepartment: string;
    finalSharing: boolean;
    useThisLimit: boolean;
  },
  unknown
> => useAPIMutation(({ http }, { limitId: id, ...limit }) => http.put<void>(`${GET_DEP_LIMITS}/${id}`, limit), {
  onSuccess: ignore, // TODO: log mutation result
});

export const useNewPercentageSharing = (): MutationResultPair<
  PercentageSharing,
  unknown,
  { limitId: UUID; percentages: Percentages },
  unknown
> => useAPIMutation(
  ({ http, process }, { limitId, percentages }) => {
    const p = Object.fromEntries(percentages.map((pc, i) => [`month${i}`, pc]));

    return http
      .post<PercentageSharing>(
        LIMIT_SHARING_PERCENTS,
        NewPercentageSharing.encode({ limitId, ...p } as NewPercentageSharing)
      )
      .then(process.decodeResponseData(PercentageSharing));
  },
  {
    onSuccess: ({
      cache, result: sharing, variables: { limitId },
    }) => (
      // TODO: log mutation result
      updateQueryCache(cache, ['limits'], limits => limits.map(limit => limit.id === limitId ? { ...limit, percentageSharing: [...limit.percentageSharing, sharing] } : limit))
    ),
  }
);

export const useUpdatePercentageSharing = (): MutationResultPair<
  AxiosResponse<void>,
  unknown,
  {
    limitId: UUID;
    percentages: Percentages;
    sharingId: UUID;
  },
  unknown
> => useAPIMutation(
  ({ http }, {
    limitId, percentages, sharingId,
  }) => {
    const p = Object.fromEntries(percentages.map((pc, i) => [`month${i}`, pc]));

    return http.put<void>(
      `/limits/limitsharingprocents/${sharingId}`,
      NewPercentageSharing.encode({ limitId, ...p } as NewPercentageSharing)
    );
  },
  {
    onSuccess: ({
      cache, variables: {
        limitId, percentages, sharingId,
      },
    }) => {
      const p = Object.fromEntries(percentages.map((pc, i) => [`month${i}`, pc]));
      const sharing = {
        id: sharingId, limitId, ...p,
      };
        // TODO: log mutation result
      return updateQueryCache(cache, ['limits'], limits => limits.map(limit => limit.id === limitId
        ? {
          ...limit,
          percentageSharing: limit.percentageSharing.map(s => (s.id === sharingId ? { ...s, sharing } : s)),
        }
        : limit
      )
      );
    },
  }
);

export const useDeletePercentageSharing = (): MutationResultPair<AxiosResponse<void>, unknown, { id: UUID }, unknown> => useAPIMutation(({ http }, { id }) => http.delete(`/limits/limitsharingprocents/${id}`), {
  onSuccess: ({ cache, variables: { id } }) => {
    // TODO: log mutation result
    updateQueryCache(cache, ['limits'], limits => limits.map(limit => ({
      ...limit,
      percentageSharing: limit.percentageSharing.filter(p => p.id !== id),
    }))
    );
  },
});

export const useLimitSettings = (): APIQueryResult<LimitSettings, unknown> => useAPI(['limitSettings'], ({ http, process }) => http
  .get<LimitSettingsRaw>('/limits/limitsettings')
  .then(process.decodeResponseData(LimitSettingsRaw))
  .then<LimitSettings>(xs => Object.fromEntries(xs.map(({ name, value }) => [name, value])) as LimitSettings)
);

// Перераспределение от дочернего подразделения к родительскому
// @ts-ignore
export const useReshareTransfer = (): MutationResultPair<number, unknown, { item: ReshareTransferItem }, unknown> => (
  useAPIMutation(
    // @ts-ignore
    ({ http, process }, { item }: { item: ReshareTransferItem }) => http
      .post<any>(`/limits/deplimits/reshare_transfer`, {
        // @ts-ignore
        ...ReshareTransferItem.encode({ ...item } as ReshareTransferItem),
      })
      .then(res => process.getResponseStatus(res)),
    { onSuccess: refetchLimits }
  )
);

// Распределение из экономии
export const useShareEconomy = (): MutationResultPair<
  number,
  unknown,
  // @ts-ignore
  { parentLimit: Limit; items: ItemShareEconomy[] },
  unknown
> => useAPIMutation(
  // @ts-ignore
  ({ http, process }, { parentLimit, items }: { parentLimit: Limit; items: ItemShareEconomy[] }) => http
    .post<any>(`${DEP_LIMIT_SHARE_ECONOMY}/${parentLimit.id}`, t.array(ItemShareEconomy).encode(items))
    .then(res => process.getResponseStatus(res)),
  {
    onSuccess: ignore, // TODO: log mutation result
  }
);

// Получение экономии за год
export const useGetEconomyHistoryByYear = (
  limitId: string,
  year: number | undefined,
  options: QueryConfig<EconomyRow[], Error>
): APIQueryResult<EconomyRow[], Error> => useAPI(
  ['economyRows', limitId as UUID, year as number],
  ({ http, process }) => http
    .get<EconomyRow[]>(`${LIMIT_TRANSFER_HISTORY}/economy/to/year/${year}/limit/${limitId}`, {})
    .then(process.decodeResponseData(t.array(EconomyRow)))
    .catch(() => []),
  options
);

// Получение движений ДС по лимиту
export const useGetLimitHistoryByYear = (
  limitId: string,
  year: number | undefined,
  maxRecords: number,
  options: QueryConfig<EconomyResharingRow[], Error>
): APIQueryResult<EconomyResharingRow[], Error> => useAPI(
  ['economyResharingRows', limitId as UUID, year as number],
  ({ http, process }) => http
    .get<EconomyResharingRow[]>(`${LIMIT_TRANSFER_HISTORY}/getByLimit/${limitId}/${year}/${maxRecords}`, {})
    .then(process.decodeResponseData(t.array(EconomyResharingRow)))
    .catch(() => []),
  options
);

// Получение экономии за год
export const useGetEconomyResharingHistory = (
  limitId: string,
  year: number | undefined,
  options: QueryConfig<EconomyResharingRow[], Error>
): APIQueryResult<EconomyResharingRow[], Error> => useAPI(
  ['economyResharingRows', limitId as UUID, year as number],
  ({ http, process }) => http
    .get<EconomyResharingRow[]>(`${LIMIT_TRANSFER_HISTORY}/economy/from/year/${year}/limit/${limitId}`, {})
    .then(process.decodeResponseData(t.array(EconomyResharingRow)))
    .catch(() => []),
  options
);

// Получение экономии за период
export const useGetEconomyPerPeriod = (
  year: number | undefined,
  period: string | undefined
): APIQueryResult<EconomyRow[], Error> => useAPI(['economyPerPeriod', year as number, period as string], ({ http, process }) => http
  .get<EconomyRow[]>(`${LIMIT_TRANSFER_HISTORY}/economy/year/${year}/period/${period}`, {})
  .then(process.decodeResponseData(t.array(EconomyRow)))
  .catch(() => [])
);

// Получение всех настроек
export const useGetLimitSettings = (): APIQueryResult<LimitSettingData[], Error> => useAPI(['limitSettingRows'], ({ http, process }) => http
  .get<LimitSettingData[]>(LIMIT_SETTINGS, {})
  .then(process.decodeResponseData(t.array(LimitSettingData)))
  .catch(() => [])
);

// Сохранение значений настроек EMP_LIMIT_REMAINS_TARGET and DEP_LIMIT_REMAINS_TARGET
export const useSaveLimitRemainsTarget = (): MutationResultPair<
  number,
  unknown,
  { emptarget: string; deptarget: string },
  unknown
> => useAPIMutation(
  ({ http, process }, { emptarget, deptarget }: { emptarget: string; deptarget: string }) => http
    .post<any>(`${LIMIT_SETTINGS}/target/emp/${emptarget}/dep/${deptarget}`, {})
    .then(res => process.getResponseStatus(res)),
  {
    onSuccess: ignore, // TODO: log mutation result
  }
);

// Сохранение значения настроек цветового отображения лимитов
export const useSaveColorLimitSettings = (): MutationResultPair<
  number,
  unknown,
  { item: ColorLimitSettings },
  unknown
> => useAPIMutation(
  ({ http, process }, { item }: { item: ColorLimitSettings }) => http
    .post<any>(`${LIMIT_SETTINGS}/boundaries`, {
      ...ColorLimitSettings.encode({ ...item } as ColorLimitSettings),
    })
    .then(res => process.getResponseStatus(res)),
  {
    onSuccess: ignore, // TODO: log mutation result
  }
);

export const useAddEconomyHistory = (): MutationResultPair<number, unknown, { item: AddEconomyHistory }, unknown> => (
  useAPIMutation(
    ({ http, process }, { item }: { item: AddEconomyHistory }) => http
      .post<any>(LIMIT_TRANSFER_HISTORY, {
        ...AddEconomyHistory.encode({ ...item } as AddEconomyHistory),
      })
      .then(res => process.getResponseStatus(res)),
    {
      onSuccess: ignore, // TODO: log mutation result
    }
  )
);

// Получение данных распределения по периодам
export const useGetLimitDataByPeriod = (limitSharingId: UUID): APIQueryResult<LimitByPeriodData[], Error> => useAPI(['limitDataByPeriod', limitSharingId], ({ http, process }) => http
  .get<LimitByPeriodData[]>(LIMIT_SHARING_PER_PERIOD, { urlParams: { limitSharingId } })
  .then(process.decodeResponseData(t.array(LimitByPeriodData)))
  .catch(() => [])
);

export const useGetLimitByPeriodMutation = (): MutationResultPair<
  LimitByPeriodData[],
  unknown,
  { limitSharingId: UUID },
  unknown
> => useAPIMutation(
  ({ http, process }, { limitSharingId }: { limitSharingId: UUID }) => http
    .get<LimitByPeriodData[]>(LIMIT_SHARING_PER_PERIOD, { urlParams: { limitSharingId } })
    .then(process.decodeResponseData(t.array(LimitByPeriodData))),
  {
    onSuccess: ({ cache, variables }) => updateQueryCache(cache, ['limitDataByPeriod', variables.limitSharingId], limits => limits),
  }
);

// Получение всех заявок на лимиты по автору
export const useGetLimitRequestsByAuthor = (): APIQueryResult<LimitRequestByAuthor[], Error> => useAPI(['limitRequestsByAuthor'], ({ http, process }) => http
  .get<LimitRequestByAuthor[]>(`${GET_ALL_LIMIT_REQUESTS_BY_AUTHOR}`, {})
  .then(process.decodeResponseData(t.array(LimitRequestByAuthor)))
  .catch(() => [])
);
export const useGetLimitRequestsByApprover = (): APIQueryResult<LimitRequestByAuthor[], Error> => useAPI(['limitRequestsByAuthor'], ({ http, process }) => http
  .get<LimitRequestByAuthor[]>(`${GET_ALL_LIMIT_REQUESTS_BY_APPROVER}`, {})
  .then(process.decodeResponseData(t.array(LimitRequestByAuthor)))
  .catch(() => [])
);
// Добавление новой заявки на лимит
export const useLimitRequest = (): MutationResultPair<void, unknown, LimitRequest, unknown> => useAPIMutation(
  ({ http }, req: LimitRequest) => http
    .post('/limits/requests/dep', LimitRequest.encode(req), { headers: { 'X-Version': 2 } })
    .then(ignore),
  { onSuccess: ignore }
);

// Получение смежников подразделения
export const useGetDepSiblings = (
  departmentId: UUID | string,
  percent: number,
  transportType: string,
  year: number,
  sum: number,
  options: QueryConfig<DepSiblings[], Error>
): APIQueryResult<DepSiblings[], Error> => useAPI(
  ['depSiblings', departmentId as UUID, percent as number, transportType as string, year as number, sum as number],
  ({ http, process }) => http
    .get<DepSiblings[]>(`/limits/deplimits/siblings/${departmentId}/${percent}/${transportType}/${year}/${sum}`, {})
    .then(process.decodeResponseData(t.array(DepSiblings)))
    .catch(() => []),
  options
);

// Имитация распределения денежных средств
export const useGetSharingImitation = (
  limitId: UUID | string,
  totalSum: number,
  options: QueryConfig<number[], Error> = {}
): APIQueryResult<number[], Error> => useAPI(
  ['sharingImitation', limitId as UUID, totalSum],
  ({ http, process }) => http
    .get<number[]>(`/limits/limitsharing/getSharingImitation/${limitId}/${totalSum}`, {})
    .then(process.decodeResponseData(t.array(t.number)))
    .catch(() => []),
  {
    cacheTime: 0, refetchOnMount: true, ...options,
  }
);

export const useGetLimitInfographics = (
  year: number,
  departmentId: UUID | string,
  orgId: UUID,
  options: QueryConfig<LimitStatisticsObject, Error> = {}
): APIQueryResult<LimitStatisticsObject, Error> => useAPI(
  ['limitInfographics', departmentId as UUID, year],
  ({ http, process }) => http
  // @ts-ignore
    .get<LimitStatisticsObject>(`/limits/limits/getstats/${orgId}/${year}/${departmentId}`, {}, false)
    .then(process.decodeResponseData(LimitStatisticsObject))
    .catch(() => ({} as LimitStatisticsObject)),
  {
    cacheTime: 200, refetchOnMount: true, ...options,
  }
);

export type updateRule = 'ADD' | 'UPDATE';
type UploadLimitsParams = { file: File; simulate: boolean; updateRule: updateRule } & Partial<CSVParams>;

const LimitsUploadReport = t.strict({
  deleted: t.number,
  errorDescrs: t.array(t.string),
  errors: t.number,
  inserted: t.number,
  resultStatus: tt.oneOf('FAIL', 'SUCCESS'),
  total: t.number,
  unchanged: t.number,
  updated: t.number,
});

export type LimitsUploadReport = t.TypeOf<typeof LimitsUploadReport>;

export const useUploadLimits = ({
  organizationId,
}: {
  organizationId: UUID;
}): MutationResultPair<LimitsUploadReport, unknown, UploadLimitsParams, unknown> => (
  useAPIMutation<LimitsUploadReport, unknown, UploadLimitsParams, unknown>(
    (
      { http, process },
      {
        file, updateRule, simulate, includeHeader = false, csvSeparator = 'SEMICOLON', fpSeparator = 'COMMA',
      }
    ) => {
      const formData = new FormData();
      formData.append('file', file);

      return http
        // @ts-ignore
        .request({
          url: `/limits/limits/import/${simulate ? 'simulation/' : ''}${organizationId}`,
          method: 'POST',
          data: formData,
          params: {
            updateRule,
            includeHeader,
            csvSeparator,
            fpSeparator,
          },
        })
        .then(process.decodeResponseData(LimitsUploadReport));
    },
    { onSuccess: ({ cache }) => cache.invalidateQueries(['limits']) }
  )
);

export const useUpdateLimitStatus = () => useAPIMutation(
  ({ http }, limit: Limit) => http.put(`/limits/deplimits/editStatusOrFlags/${limit.id}`, { limitStatus: limit.limitStatus }, {}).then(ignore),
  {
    onSuccess: ({ process, t }) => {
      process.processStatus(200, t.LimitEditMessages.isEditedSuccessfully);
    },
    onError: ({ logger, t }) => {
      logger.toMessage('error', t.LimitEditMessages.isEditedWithErrors);
    },
  }
);

// Изменение суммы лимита - сумму нельзя изменить в меньшую сторону, только в большую. Такая логика на беке
export const useUpdateLimitSum = () => useAPIMutation(({ http }, limit: Limit) => http.put(`/limits/deplimits/${limit.id}`, limit), {
  onSuccess: ({ process, t }) => {
    process.processStatus(200, t.LimitEditMessages.isEditedSuccessfully);
  },
  onError: ({ logger, t }) => {
    logger.toMessage('error', t.LimitEditMessages.isEditedWithErrors);
  },
});

export const useToggleFinalSharingFlag = () => useAPIMutation(
  ({ http }, { isFinalSharing, limitId }: { isFinalSharing: boolean; limitId: string }) => http.put(`/limits/deplimits/editStatusOrFlags/${limitId}`, { finalSharing: Boolean(isFinalSharing) }),
  {
    onSuccess: ({ process, t }) => {
      process.processStatus(200, t.LimitEditMessages.isEditedSuccessfully);
    },
    onError: ({
      error, logger, t,
    }) => {
      logger.toMessage('error', `${t.LimitEditMessages.isEditedWithErrors} ${getErrorMessage(error)}`);
    },
  }
);

export const useGetLimitByRequestId = (requestId: UUID): APIQueryResult<LimitByRequest | null, Error> => useAPI(['limitByRequestId', requestId], ({ http, process }) => http
  .get<LimitByRequest>(GET_LIMIT_BY_REQUEST, { urlParams: { requestId } })
  .then(process.decodeResponseData(LimitByRequest))
);

// Получение аудита лимитов
export const useGetLimitsAudit = (
  orgId: string,
  year: number,
  options: QueryConfig<string[], Error>
): APIQueryResult<string[], Error> => useAPI(
  ['limitsAudit'],
  ({ http, process }) => http
    .get<string[]>(`/limits/limits/audit/org/${orgId}/year/${year}`, {})
    .then(process.decodeResponseData(t.array(t.string)))
    .catch(() => []),
  {
    cacheTime: 0, refetchOnMount: true, ...options,
  }
);

export const AsyncReportResponse = t.type({
  result_url: t.string,
  started: t.boolean,
});

export type AsyncReportResponse = t.TypeOf<typeof AsyncReportResponse>;

export interface ProgressDownloading {
  in_progress: boolean;
}

// Получение аудита лимитов (асинхрон)
export const downloadAuditAsync = (
  http: IHttpService,
  orgId: string,
  year: number,
  setAudits: Dispatch<SetStateAction<string[]>>,
  finishLoading?: () => void
): Promise<void> => {
  // TODO: рассмотреть остановку таймера
  const checkResponse = (filePath: string): void => {
    const directory = `/${LIMITS}/${LIMITS}/${GET_ASYNC}/`;

    http
      .get<ArrayBuffer>(directory + filePath, { responseType: 'arraybuffer' })
      .then(fileResponse => {
        const decoder = new TextDecoder();
        const data = decoder.decode(fileResponse.data);

        let dataObj: ProgressDownloading = {
          in_progress: false,
        };

        if (isJsonString(data)) {
          dataObj = JSON.parse(data);
        }

        if (!dataObj.in_progress) {
          setAudits(data.split('|'));
          if (finishLoading) {
            finishLoading();
          }
        } else {
          setTimeout(() => checkResponse(filePath), 1000);
        }
      })
      .catch(() => {
        if (finishLoading) {
          finishLoading();
        }
      });
  };

  return http
    .get<AsyncReportResponse>(`/${LIMITS}/${LIMITS}${GET_LIMIT_AUDIT}`, { urlParams: { orgId, year: year.toString() } })
    .then(resp => {
      setTimeout(() => checkResponse(resp.data.result_url), 1000);
    })
    .catch(() => {
      if (finishLoading) {
        finishLoading();
      }
    });
};

// Закрытие дочерних лимитов (асинхрон)
export const closeChildLimitsAsync = (
  http: IHttpService,
  orgId: string,
  limitId: string,
  finishLoading?: (params: { success: boolean }) => void
): Promise<void> => {
  const checkResponse = (filePath: string): void => {
    http
      .get<ArrayBuffer>(`${GET_LIMIT_ASYNC}/${filePath}`, { responseType: 'arraybuffer' })
      .then(fileResponse => {
        const decoder = new TextDecoder();
        const data = decoder.decode(fileResponse.data);

        let dataObj: ProgressDownloading = {
          in_progress: false,
        };

        if (isJsonString(data)) {
          dataObj = JSON.parse(data);
        }

        if (!dataObj.in_progress) {
          if (finishLoading) {
            finishLoading({ success: true });
          }
        } else {
          setTimeout(() => checkResponse(filePath), 1000);
        }
      })
      .catch(() => {
        if (finishLoading) {
          finishLoading({ success: false });
        }
      });
  };

  return http
    .get<AsyncReportResponse>(GET_LIMIT_CLOSE, { urlParams: { orgId, limitId } })
    .then(resp => {
      if (resp.data.started) {
        setTimeout(() => checkResponse(resp.data.result_url), 1000);
      } else if (finishLoading) {
        finishLoading({ success: false });
      }
    })
    .catch(() => {
      if (finishLoading) {
        finishLoading({ success: false });
      }
    });
};

export const useDeleteAllLimitsByCorpId = (): MutationResultPair<
  unknown,
  unknown,
  { organizationId: UUID; serviceType: string; year: string },
  unknown
> => useAPIMutation(
  ({ http }, {
    organizationId, serviceType, year,
  }) => http.delete(DELETE_ALL_LIMITS_IN_CORP_CLIENT, {
    urlParams: {
      organizationId, serviceType, year,
    },
  }),
  {
    onSuccess: ({ logger, t }) => {
      logger.toMessage('success', t.global.successDelete);
    },
  }
);
