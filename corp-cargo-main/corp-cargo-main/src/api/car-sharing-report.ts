import { APIQueryResult, useAPI } from 'api';
import * as tt from 'utils/io-ts';
import * as t from 'io-ts';
import { CAR_SHARING_REPORT, GET_CAR_SHАRING_TRIP_STATUS } from 'constants/constants.api';

import {
  CarSharingSearchResponse,
  CarSharingReportFilters,
  Filters
} from 'stores/CarSharingTrip/CarSharingTrip.interface';

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
  .then(process.decodeResponseData(CarSharingSearchResponse))
);
