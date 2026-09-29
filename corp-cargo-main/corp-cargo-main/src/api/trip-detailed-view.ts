import { MutationResultPair } from 'react-query';

import { useAPIMutation } from '.';
import {
  CLEAR_QUERY_CONFIG,
  UPDATE_STATUS
} from '../constants/constants.api';
import { FeedContent } from '../stores/Engineer/Models/Feed/Feed.content';
import { UUID } from '../utils/io-ts';

declare module 'api' {
  interface Cache {
    trip: { key: ['trip', UUID]; value: FeedContent };
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    statusChange: { key: ['statusChange']; value: any };
    tripRequestInfo: { key: ['tripRequestInfo', UUID]; value: FeedContent };
  }
}

export const useChangeRequestStatus = (): MutationResultPair<
  FeedContent,
  unknown,
  { requestId: UUID; status: string },
  unknown
> => useAPIMutation(
  // eslint-disable-next-line @stylistic/max-len
  ({ http, process }, { requestId, status }) => http.post<FeedContent>(UPDATE_STATUS, {}, { urlParams: { requestId, status } }).then(process.getResponseData),
  {
    ...CLEAR_QUERY_CONFIG,
    onSuccess: ({
      cache, result: trip, process, t,
    }) => {
      cache.refetchQueries(['trip', trip.id], { exact: true, active: true });
      cache.refetchQueries(['tripRequest', trip.id], { exact: true, active: true });
      process.processStatus(200, t.DetailedView.StatusEditMessages.isEditedSuccessfully);
    },
    onError: ({ process, t }) => {
      process.processStatus(409, t.DetailedView.StatusEditMessages.isEditedWithErrors);
    },
  }
);

