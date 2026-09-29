import * as t from 'io-ts';
import { QueryConfig } from 'react-query';

import { APIQueryResult, useAPI } from 'api';
import { plainToNew } from 'utils';

import { GET_UPDATED_REQUEST_TRIP_INFO } from 'constants/constants.env';

import { TripRequestModel } from 'stores/Trip/models';
import { TripRequest } from 'stores/Trip/Trip.interface';

import { UUID } from 'utils/io-ts';

export interface RequestStatus {
  name: string;
  rusName: string;
  approvable: boolean;
  cancelable: boolean;
  editable: boolean;
  color: string;
  finalStatus: boolean;
}

export const RequestStatus = t.type({
  name: t.string,
  rusName: t.string,
  approvable: t.boolean,
  cancelable: t.boolean,
  editable: t.boolean,
  color: t.string,
  finalStatus: t.boolean,
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
