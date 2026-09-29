import * as t from 'io-ts';
import { QueryConfig } from 'react-query';

import { APIQueryResult, useAPI } from 'api';

import { GET_UPDATED_REQUEST_TRIP_INFO } from 'constants/constants.env';

import { TripRequestModel } from 'stores/Trip/models';
import { TripRequest } from 'stores/Trip/Trip.interface';
import { plainToNew } from 'utils';

import { UUID } from 'utils/io-ts';

export interface RequestStatus {
  name: string;
  rusName: string;
}

export const RequestStatus = t.type({
  name: t.string,
  rusName: t.string,
});

declare module 'api' {
  interface Cache {
    updatedTripRequestInfo: {
      key: ['updatedTripRequestInfo', UUID];
      value: TripRequestModel;
    };
  }
}

export const useGetUpdatedRequestTripInfoById = (
  reqId: UUID,
  options: QueryConfig<TripRequestModel, unknown>
): APIQueryResult<TripRequestModel, unknown> => useAPI(
  ['updatedTripRequestInfo', reqId],
  ({ http, process }) => http
    .get<TripRequest>(GET_UPDATED_REQUEST_TRIP_INFO, { urlParams: { reqId } })
    .then(process.decodeResponseData(TripRequest))
    .then(approvals => plainToNew<TripRequestModel>(TripRequestModel, approvals)),
  options
);
