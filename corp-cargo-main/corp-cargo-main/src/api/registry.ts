import { TripResponse } from 'stores/Registry/Registry.interface';
import { TaxiTripFactData } from 'stores/TaxiRegistry/models/TaxiRegistry.interface';
import { UUID } from '../utils/io-ts';
import { APIQueryResult, TypeAtKey, useAPI } from './index';
import {
  GET_DEFAULT_USERS_ATTRIBUTES,
  GET_TRIP_REQUESTS,
  GET_TRIP_REQUESTS_FACT_DATA
} from '../constants/constants.api';

import { UsersAttributes } from './register-search';

declare module 'api' {
  interface Cache {
    tripRequest: { key: ['tripRequest', UUID]; value: { tripResponse: TripResponse } };
    factDataTripRequest: { key: ['factDataTripRequest', UUID]; value: TaxiTripFactData };
    defaultColumns: { key: ['defaultColumns']; value: UsersAttributes };
  }
}

type CacheItem = TypeAtKey<['tripRequest', UUID]>;

const raw2Cache = (tripResponse: TripResponse): CacheItem => ({ tripResponse });

export const useTripRequest = ({
  transportType,
  requestId,
}: {
  transportType: string;
  requestId: UUID;
}): APIQueryResult<CacheItem, Error> => useAPI(['tripRequest', requestId], ({ http, process }) => http
  .get<TripResponse>(GET_TRIP_REQUESTS, { urlParams: { transportType, requestId } })
  .then(process.decodeResponseData(TripResponse))
  .then(raw2Cache)
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
