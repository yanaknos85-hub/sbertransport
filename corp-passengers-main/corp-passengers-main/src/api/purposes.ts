import * as t from 'io-ts';
import {
  APIQueryResult, TypeAtKey, useAPI, useAPIMutation
} from 'api';
import {
  DELETE_TRIP_PURPOSE,
  GET_ACTIVE_TRIP_PURPOSES,
  GET_ALL_TRIP_PURPOSES,
  TRIP_PURPOSE_CREATE,
  TRIP_PURPOSE_UPDATE
} from 'constants/constants.api';

import { TripPurpose } from 'stores/TripPurposes/TripPurpose.interface';
import indexById from 'utils/indexById';
import { UUID } from 'utils/io-ts';
import { SubmitTripPurposeData } from '../modules/TripPurposes/types/types';
import { mkUseUploadEntity } from './upload';

declare module 'api' {
  interface Cache {
    purposeJsons: {
      key: ['purposes', UUID];
      value: { purposes: TripPurpose[]; byId: Record<string, TripPurpose> };
    };
    allPurposes: {
      key: ['allPurposes', UUID];
      value: { purposes: TripPurpose[]; byId: Record<string, TripPurpose> };
    };
  }
}

const raw2JsonsCache = (purposes: TripPurpose[]) => ({
  purposes,
  byId: indexById(purposes),
});

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const useActiveTripPurposes = (orgId: UUID): APIQueryResult<TypeAtKey<['purposes', UUID]>, any> => useAPI(['purposes', orgId], ({ http, process }) => http
  .get<TripPurpose[]>(GET_ACTIVE_TRIP_PURPOSES, { urlParams: { orgId } })
  .then(process.decodeResponseData(t.array(TripPurpose)))
  .then(raw2JsonsCache)
);

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const useAllTripPurposes = (orgId: UUID): APIQueryResult<TypeAtKey<['purposes', UUID]>, any> => useAPI(['allPurposes', orgId], ({ http, process }) => http
  .get<TripPurpose[]>(GET_ALL_TRIP_PURPOSES, { urlParams: { orgId } })
  .then(process.decodeResponseData(t.array(TripPurpose)))
  .then(raw2JsonsCache)
);

export const useDeleteTripPurpose = () => useAPIMutation(
  ({ http }, { orgId, purId }: { orgId: UUID; purId: UUID }) => (
    http.delete<number>(DELETE_TRIP_PURPOSE, { urlParams: { orgId, purId } })
  ),
  {
    onSuccess: ({
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      cache, variables: { orgId, purId }, process, t,
    }) => {
      process.processStatus(200, t.TripPurposes.DeleteSuccess);
      cache.refetchQueries(['purposes']);
    },
  }
);

export const useCreateTripPurpose = () => useAPIMutation(
  ({ http, process }, { purpose, orgId }: { purpose: SubmitTripPurposeData; orgId: UUID }) => (
    // @ts-ignore
    http.post(TRIP_PURPOSE_CREATE, purpose, { urlParams: { orgId } }).then<TripPurpose>(process.getResponseData)
  ),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.TripPurposes.AddSuccess);
      cache.refetchQueries(['purposes']);
    },
    onError: ({
      error, logger, t,
    }) => {
      if (error?.request?.status === 409) {
        logger.toMessage('error', t.TripPurposes.duplicateError);
      }
    },
  }
);

export const useUpdateTripPurpose = () => useAPIMutation(
  ({ http }, {
    purpose, orgId, purId,
  }: { purpose: SubmitTripPurposeData; orgId: UUID; purId: UUID }) => (
    http.put(TRIP_PURPOSE_UPDATE, purpose, { urlParams: { orgId, purId } })
  ),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.TripPurposes.EditSuccess);
      cache.refetchQueries(['purposes']);
    },
  }
);

export const useUploadTripPurposes = mkUseUploadEntity('tripPurpose', {
  onSuccess: ({ cache }) => cache.invalidateQueries(['purposes']),
});
