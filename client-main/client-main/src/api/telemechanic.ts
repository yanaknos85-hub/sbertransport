import { APIQueryResult, useAPI } from 'api';
import { AxiosError } from 'axios';

import { CLEAR_QUERY_CONFIG } from 'constants/constants.app';
import { GET_ON_THE_LINE_STATUS } from 'constants/constants.env';
import { OnTheLineResponse } from 'modules/OnTheLineSwitch/onTheLineSwitch.types';

declare module 'api' {
  interface Cache {
    onTheLine: {
      key: ['onTheLine'];
      value: OnTheLineResponse;
    };
  }
}

export const defaultOptions = {
  ...CLEAR_QUERY_CONFIG,
  enabled: true,
};

export const useOnTheLineStatus = (): APIQueryResult<OnTheLineResponse> => useAPI(
  ['onTheLine'],
  ({ http, process }) => http
    .get<OnTheLineResponse>(GET_ON_THE_LINE_STATUS)
    .then(process.decodeResponseData(OnTheLineResponse))
    .catch((error: AxiosError): any => {
      if (error.response.status === 404) {
        console.log('Выключи ЭПЛ!');
        console.log(error.response.data.message);
      } else {
        throw error;
      }
    }),
  {
    ...defaultOptions, suspense: true, cacheTime: 100,
  }
);
