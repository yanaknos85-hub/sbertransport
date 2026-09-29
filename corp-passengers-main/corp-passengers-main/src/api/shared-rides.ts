import * as t from 'io-ts';

import { APIQueryResult, useAPI, TypeAtKey } from 'api';
import { SEARCH_SHARED_RIDES, SHARED_RIDE_DETAILED } from 'constants/constants.api';
import { SharedRideDetailed, SharedRide } from 'stores/SharedRide/SharedRide.interface';

type SharedRideDetailedValue = t.TypeOf<typeof SharedRideDetailed>;

declare module 'api' {
  interface Cache {
    sharedRides: {
      key: ['sharedRides'];
      value: { sharedRides: SharedRide[] };
    };

    sharedRideDetailed: {
      key: ['sharedRideDetailed', string];
      value: SharedRideDetailedValue;
    };
  }
}

type SharedRidesCacheItem = TypeAtKey<['sharedRides']>;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type SharedRideSearchParams = Partial<Record<keyof SharedRide, any>>;

export const useSearchSharedRides = (
  searchParams: SharedRideSearchParams
): APIQueryResult<SharedRidesCacheItem, unknown> => (
  // TODO check once backend for this endpoint is implemented
  useAPI(['sharedRides'], ({ http, process }) => http
    .get<SharedRide[]>(SEARCH_SHARED_RIDES, { urlParams: { ...searchParams } })
    .then(process.decodeResponseData(t.array(SharedRide)))
    .then((sharedRides: SharedRide[]): SharedRidesCacheItem => ({ sharedRides }))
  )
);

export const useSharedRideDetailed = (requestId: string): APIQueryResult<SharedRideDetailedValue, unknown> => useAPI(['sharedRideDetailed', requestId], ({ http, process }) => http
  .get<SharedRideDetailedValue>(SHARED_RIDE_DETAILED, { urlParams: { requestId } })
  .then(process.decodeResponseData(SharedRideDetailed))
);
