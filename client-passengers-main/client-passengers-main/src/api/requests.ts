import { APIQueryResult, useAPI } from 'api';

import { GET_REQUEST_WITH_CARSHARING_DEEPLINK } from 'constants/constants.env';

declare module 'api' {
  interface Cache {
    carsharingDeeplink: {
      key: ['carsharingDeeplink', string];
      value: { deepLink: string };
    };
  }
}

export const useGetCarsharingDeepLink = (requestId: string): APIQueryResult<{ deepLink: string }, unknown> => useAPI(['carsharingDeeplink', requestId], ({ http, process }) => http
  .get<{ deepLink: string }>(GET_REQUEST_WITH_CARSHARING_DEEPLINK, { urlParams: { requestId } })
  .then(process.decodeResponseData())
);
