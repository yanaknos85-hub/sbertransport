import { QueryConfig } from 'react-query';

import { APIQueryResult, useAPI } from 'api';
import { TripPurpose } from 'stores/Trip/Trip.interface';
import { FIND_PURPOSE_BY_ID, GET_ALL_PURPOSES, GET_PURPOSES_BY_EMPLOYEE } from 'constants/constants.env';

declare module 'api' {
  interface Cache {
    purposes: {
      key: ['purposes', string];
      value: TripPurpose[];
    };
    employeePurposes: {
      key: ['employeePurposes', string];
      value: TripPurpose[];
    };
    singlePurpose: {
      key: ['singlePurpose', string, string | undefined];
      value: TripPurpose;
    };
  }
}

export const useGetAllPurposes = (
  orgId: string,
  options: QueryConfig<TripPurpose[], unknown>
): APIQueryResult<TripPurpose[], unknown> => useAPI(
  ['purposes', orgId],
  ({ http, process }) => http.get<TripPurpose[]>(GET_ALL_PURPOSES, { urlParams: { orgId } }).then(process.getResponseData),
  options
);

export const useGetEmployeePurposes = (
  orgId: string,
  options: QueryConfig<TripPurpose[], unknown>
): APIQueryResult<TripPurpose[], unknown> => useAPI(
  ['employeePurposes', orgId],
  ({ http, process }) => http.get<TripPurpose[]>(GET_PURPOSES_BY_EMPLOYEE, { urlParams: { orgId } }).then(process.getResponseData),
  options
);

export const useGetPurposeById = (
  {
    orgId,
    purposeId,
  }: {
    orgId: string;
    purposeId: string | undefined;
  },
  options: QueryConfig<TripPurpose, unknown>
): APIQueryResult<TripPurpose, unknown> => useAPI(
  ['singlePurpose', orgId, purposeId],
  ({ http, process }) => http
    .get<TripPurpose>(FIND_PURPOSE_BY_ID, { urlParams: { orgId, purId: purposeId || '' } })
    .then(process.getResponseData),
  options
);
