import { APIQueryResult, useAPI } from 'api';

import {
  GET_REQUEST_WITH_SUBURB_COMPENSATION,
  GET_REQUEST_WITH_TRAVEL_CARD_COMPENSATION
} from 'constants/constants.env';

import { InnerCityTransportRequestInfo } from 'stores/Trip/Trip.interface';

declare module 'api' {
  interface Cache {
    requestSuburbCompensation: {
      key: ['requestSuburbCompensation', string];
      value: InnerCityTransportRequestInfo;
    };
    requestTravelCardCompensation: {
      key: ['requestTravelCardCompensation', string];
      value: InnerCityTransportRequestInfo;
    };
  }
}

export const useGetRequestSuburbCompensation = (
  requestId: string
): APIQueryResult<InnerCityTransportRequestInfo, unknown> => useAPI(['requestSuburbCompensation', requestId], ({ http, process }) => http
  .get<InnerCityTransportRequestInfo>(GET_REQUEST_WITH_SUBURB_COMPENSATION, { urlParams: { requestId } })
  .then(process.decodeResponseData(InnerCityTransportRequestInfo))
);

export const useGetRequestTravelCardCompensation = (
  requestId: string
): APIQueryResult<InnerCityTransportRequestInfo, unknown> => useAPI(['requestTravelCardCompensation', requestId], ({ http, process }) => http
  .get<InnerCityTransportRequestInfo>(GET_REQUEST_WITH_TRAVEL_CARD_COMPENSATION, { urlParams: { requestId } })
  .then(process.decodeResponseData(InnerCityTransportRequestInfo))
);
