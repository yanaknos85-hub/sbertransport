import { useEffect, useState } from 'react';
import { QueryConfig } from 'react-query';
import { useRouteMatch } from 'react-router-dom';

import { APIQueryResult } from 'api';

import { CLEAR_QUERY_CONFIG } from 'constants/constants.app';

import { TripRequestModel } from 'stores/Trip/models';

import { UUID } from 'utils/io-ts';

export interface TripRequestHook {
  refetchRequestTrip: (options?: any) => Promise<TripRequestModel>;
  tripRequest: TripRequestModel | undefined;
  inProgress: boolean;
}

interface HookProp {
  fetchTripRequest(
    reqId: UUID,
    options: QueryConfig<TripRequestModel, unknown>
  ): APIQueryResult<TripRequestModel, unknown>;
  outReqId?: UUID;
}

export const useGetTripRequest = ({ fetchTripRequest, outReqId }: HookProp): TripRequestHook => {
  const [tripRequest, setTripRequest] = useState<TripRequestModel>();
  const [dataIsFetched, setDataIsFetched] = useState(false);
  const match = useRouteMatch<{ reqId: UUID }>();
  const { reqId } = match.params;

  const finalReqId = reqId ?? outReqId;

  const fetchEnabled = Boolean(finalReqId && !tripRequest && dataIsFetched);

  const {
    refetch: refetchRequestTrip,
    data,
    isLoading,
    isFetching,
  } = fetchTripRequest(finalReqId, {
    ...CLEAR_QUERY_CONFIG,
    enabled: fetchEnabled,
    cacheTime: 1000,
  });

  // update & init
  useEffect(() => {
    setTripRequest(data);
    setDataIsFetched(true);
  }, [data]);

  // unmount
  useEffect(
    () => (): void => {
      setTripRequest(undefined);
      setDataIsFetched(false);
    },
    []
  );

  const inProgress = isLoading || isFetching;

  return {
    refetchRequestTrip,
    tripRequest,
    inProgress,
  };
};
