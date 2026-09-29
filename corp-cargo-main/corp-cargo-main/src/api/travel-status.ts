import * as t from 'io-ts';

import { APIQueryResult, useAPI } from 'api';
import { GET_ALL_TRIP_STATUS } from 'constants/constants.api';

export const TripStatus = t.intersection([
  t.type({ name: t.string, rusName: t.string }),
  t.partial({
    finalStatus: t.boolean,
    color: t.string,
  }),
]);

export type TripStatus = t.TypeOf<typeof TripStatus>;

declare module 'api' {
  interface Cache {
    tripStatus: { key: ['tripStatus']; value: TripStatus[] };
  }
}

export const useGettingAllTravelStatuses = (): APIQueryResult<TripStatus[], Error> => useAPI(['tripStatus'], ({ http, process: { decodeResponseData } }) => http.get<TripStatus[]>(GET_ALL_TRIP_STATUS).then(decodeResponseData(t.array(TripStatus)))
);
