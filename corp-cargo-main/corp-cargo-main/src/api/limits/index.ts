import * as t from 'io-ts';
import { MutationResultPair } from 'react-query';
import {
  APIQueryCache, APIQueryResult, useAPI, useAPIMutation
} from 'api';

import * as tt from 'utils/io-ts';
import { UUID } from 'utils/io-ts';
import { ignore } from 'utils';

import { GET_LIMIT_BY_REQUEST } from 'constants/constants.api';

import {
  DepartmentLimit,
  DepSiblings,
  EconomyResharingRow,
  EconomyRow,
  Limit,
  LimitByPeriodData,
  LimitByRequest,
  LimitSettingData,
  LimitSettings,
  LimitSharing,
  LimitStatisticsObject,
  LimitWithoutSharing,
  PercentageSharing
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
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
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

export const refetchLimits = ({ cache }: { cache: APIQueryCache }): Promise<void> => cache.refetchQueries(['limits'], { exact: true }).then(ignore);

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
// eslint-disable-next-line @stylistic/max-len
}): MutationResultPair<LimitsUploadReport, unknown, UploadLimitsParams, unknown> => useAPIMutation<LimitsUploadReport, unknown, UploadLimitsParams, unknown>(
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
);

export const useGetLimitByRequestId = (requestId: UUID): APIQueryResult<LimitByRequest | null, Error> => useAPI(['limitByRequestId', requestId], ({ http, process }) => http
  .get<LimitByRequest>(GET_LIMIT_BY_REQUEST, { urlParams: { requestId } })
  .then(process.decodeResponseData(LimitByRequest))
);

