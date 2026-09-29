import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import * as tt from 'utils/io-ts';
import * as t from 'io-ts';
import { CAR_SHARING_REPORT, CAR_SHARING_REPORT_EXECUTOR, GET_CAR_SHАRING_TRIP_STATUS } from 'constants/constants.api';

import {
  CarSharingSearchResponse,
  CarSharingReportFilters,
  Filters
} from 'stores/CarSharingTrip/CarSharingTrip.interface';
import { MutationResultPair } from 'react-query';

import { TripStatus } from './travel-status';

declare module 'api' {
  interface Cache {
    carSharingReport: { key: ['carSharingReport']; value: CarSharingSearchResponse };
    carSharingTripStatuses: { key: ['carSharingTripStatuses']; value: TripStatus[] };
  }
}

export const useGetCarShаringTripStatuses = (): APIQueryResult<TripStatus[], Error> => useAPI(['carSharingTripStatuses'], ({ http, process }) => http.get<TripStatus[]>(GET_CAR_SHАRING_TRIP_STATUS).then(process.decodeResponseData(t.array(TripStatus)))
);

export const useCarSharingReport = (params: CarSharingReportFilters, orgId: tt.UUID) => useAPI(['carSharingReport'], ({ http, process }) => http
  .post<CarSharingSearchResponse>(CAR_SHARING_REPORT, Filters.encode({ ...params, organizationId: orgId }), {
    urlParams: { orgId },
  })
  .then(process.decodeResponseData(CarSharingSearchResponse)),
{
  cacheTime: 1, staleTime: 1, refetchOnMount: false,
}
);

export const useCarSharingReportExecutor = (params: CarSharingReportFilters, orgId: tt.UUID) => useAPI(['carSharingReport'], ({ http, process }) => http
  .post<CarSharingSearchResponse>(CAR_SHARING_REPORT_EXECUTOR, Filters.encode({ ...params, organizationId: orgId }), {
    urlParams: { orgId },
  })
  .then(process.decodeResponseData(CarSharingSearchResponse))
);

export const useCarSharingReportDeffered = (
  orgId: string | null | undefined
): MutationResultPair<CarSharingReportFilters, unknown, CarSharingReportFilters, unknown> => useAPIMutation(
  // @ts-ignore
  ({ http, process }, query) => orgId
    ? http
      .post<CarSharingSearchResponse>(
        CAR_SHARING_REPORT,
        {
          ...query, organizationId: orgId,
        },
        { urlParams: { orgId } }
      )
      .then(process.decodeResponseData(CarSharingSearchResponse))
    : ({} as CarSharingSearchResponse),
  {
    onSuccess: ({
      process,
    }) => {
      process.decodeResponseData(CarSharingSearchResponse);
    },
  }
);

export const useCarSharingReportExecutorDeffered = (
  execId?: string[] | undefined
): MutationResultPair<CarSharingReportFilters, unknown, CarSharingReportFilters, unknown> => useAPIMutation(
  // @ts-ignore
  ({ http, process }, query) => (execId)
    ? http
      .post<CarSharingSearchResponse>(
        CAR_SHARING_REPORT_EXECUTOR,
        {
          ...query, executorGroupIds: execId,
        }
      )
      .then(process.decodeResponseData(CarSharingSearchResponse))
    : ({} as CarSharingSearchResponse),
  {
    onSuccess: ({
      process,
    }) => {
      process.decodeResponseData(CarSharingSearchResponse);
    },
  }
);
