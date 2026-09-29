import { APIQueryResult, useAPI } from 'api';
import { TripRequestHistory } from 'stores/Trip/Trip.interface';
import { GET_REQUEST_HISTORY } from 'constants/constants.api';

declare module 'api' {
  interface Cache {
    tripRequestHistory: { key: ['tripRequestHistory', string]; value: TripRequestHistory };
  }
}

export const useGetTripRequestHistory = (requestId: string): APIQueryResult<TripRequestHistory, Error> => useAPI(['tripRequestHistory', requestId], ({ http, process }) => http.get<TripRequestHistory>(GET_REQUEST_HISTORY, { urlParams: { requestId } })
  .then(process.decodeResponseData(TripRequestHistory)),
{ suspense: false }
);
