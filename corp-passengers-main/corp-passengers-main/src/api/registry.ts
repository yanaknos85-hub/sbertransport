import { TripResponse } from 'stores/Registry/Registry.interface';
import { TaxiTripFactData } from 'stores/TaxiRegistry/models/TaxiRegistry.interface';
import { UUID } from '../utils/io-ts';
import {
  APIQueryResult, useAPI, useAPIMutation
} from './index';
import {
  GET_DEFAULT_USERS_ATTRIBUTES,
  GET_TRIP_REQUESTS,
  GET_TRIP_REQUESTS_FACT_DATA,
  REINTEGRATION
} from '../constants/constants.api';

import { UsersAttributes } from './register-search';

declare module 'api' {
  interface Cache {
    tripRequest: { key: ['tripRequest', UUID]; value: TripResponse };
    factDataTripRequest: { key: ['factDataTripRequest', UUID]; value: TaxiTripFactData };
    defaultColumns: { key: ['defaultColumns']; value: UsersAttributes };
    reintegration: { key: ['reintegration', UUID] };
  }
}

export const useTripRequest = ({
  transportType,
  requestId,
}: {
  transportType: string;
  requestId: UUID;
}): APIQueryResult<TripResponse, Error> => useAPI(['tripRequest', requestId], ({ http, process }) => http
  .get<TripResponse>(GET_TRIP_REQUESTS, { urlParams: { transportType, requestId } })
  .then(process.decodeResponseData(TripResponse))
);

export const useTripRequestWithFactData = ({
  transportType,
  requestId,
}: {
  transportType: string;
  requestId: UUID;
}): APIQueryResult<TaxiTripFactData, Error> => useAPI(['factDataTripRequest', requestId], ({ http, process }) => http
  .get<TaxiTripFactData>(GET_TRIP_REQUESTS_FACT_DATA, { urlParams: { transportType, requestId } })
  .then(process.decodeResponseData(TaxiTripFactData))
);

export const useDefaultColumnVisibilitySettings = () => useAPI(['defaultColumns'], ({ http, process }) => http.get<UsersAttributes>(GET_DEFAULT_USERS_ATTRIBUTES).then(process.decodeResponseData(UsersAttributes))
);

export const useTaxiReintegration = () => useAPIMutation(
  ({ http }, {
    requestId,
  }: { requestId: UUID }) => http.post(REINTEGRATION, {
    requestIds: [requestId],
  }),
  {
    onSuccess: ({
      process, t,
    }) => {
      process.processStatus(200, t.Registry.registryReintegrationSuccess);
    },
  }
);
