import * as t from 'io-ts';
import { QueryConfig } from 'react-query';

import { APIQueryResult, useAPI } from 'api';
import { plainToNew } from 'utils';

import {
  GET_DEPLIMITS_BY_DEP,
  GET_EMP_LIMIT,
  GET_LIMIT_BY_REQUEST_ID,
  GET_LIMIT_SHARING,
  MOCKED_API_PREFIX
} from 'constants/constants.env';

import { Limit, LimitRequestInfo, LimitSharing } from 'stores/Limits/Limit.interface';
import { LimitModel } from 'stores/Limits/Models/LimitModel';

import { UUID } from 'utils/io-ts';

declare module 'api' {
  interface Cache {
    singleRequestLimit: {
      key: ['singleRequestLimit', string | undefined];
      value: LimitRequestInfo;
    };
    departmentLimits: {
      key: ['departmentLimits'];
      value: Limit[];
    };
    departmentLimit: {
      key: ['departmentLimit', string];
      value: LimitModel[];
    };
    employeeLimit: {
      key: ['employeeLimit', string, number];
      value: Limit;
    };
    limitSharing: {
      key: ['limitSharing', UUID];
      value: LimitSharing[];
    };
  }
}

export const useGetLimitByRequestId = (
  requestId: string | undefined,
  options: QueryConfig<LimitRequestInfo, unknown>
): APIQueryResult<LimitRequestInfo, unknown> => useAPI(
  ['singleRequestLimit', requestId],
  ({ http, process }) => http
    .get<LimitRequestInfo>(GET_LIMIT_BY_REQUEST_ID, { urlParams: { requestId: requestId || '' } })
    .then(process.getResponseData),
  options
);

const transformToLimitModel = (limits: Limit[]): LimitModel[] => plainToNew<LimitModel[]>(LimitModel, limits);

export const useGetLimitByDepartment = (
  departmentId: string,
  options: QueryConfig<LimitModel[], unknown>
): APIQueryResult<LimitModel[], unknown> => useAPI(
  ['departmentLimit', departmentId],
  ({ http, process }) => http
    .get<Limit[]>(`${MOCKED_API_PREFIX}${GET_DEPLIMITS_BY_DEP}/${departmentId}`)
    .then(process.decodeResponseData(t.array(Limit)))
    .then(transformToLimitModel)
    .catch(() => []),
  options
);

export const useGetEmployeeLimit = (
  employeeId: string,
  year: number,
  options: QueryConfig<Limit, unknown>
): APIQueryResult<Limit, unknown> => useAPI(
  ['employeeLimit', employeeId, year],
  ({ http, process }) => http.get<Limit>(`${GET_EMP_LIMIT}${employeeId}/year/${year}`).then(process.getResponseData),
  options
);

export const useGetLimitSharing = (
  limitId: UUID,
  options: QueryConfig<LimitSharing[], unknown>
): APIQueryResult<LimitSharing[], unknown> => useAPI(
  ['limitSharing', limitId],
  ({ http, process }) => http.get<LimitSharing[]>(`${GET_LIMIT_SHARING}${limitId}`).then(process.getResponseData),
  options
);
