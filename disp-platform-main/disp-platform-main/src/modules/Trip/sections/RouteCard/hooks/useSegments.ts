import { usePlanRoute, useFactRoute } from 'api/track/track.api';
import { RouteTypes, SourceTypes } from 'api/track/track.constants';
import { UUID } from 'utils/io-ts';

const useSegments = (tripId: UUID, isFinishedTrip: boolean) => {
  const { data: routePlanData, isFetching: isPlanFetching } = usePlanRoute(
    {
      tripId,
      routeType: RouteTypes.EXPECTED,
      sourceType: SourceTypes.TWO_GIS,
    },
    {
      suspense: false,
    }
  );

  const { data: routeFactData, isFetching: isFactFetching } = useFactRoute(
    {
      tripId,
      routeType: RouteTypes.FACT,
      sourceType: SourceTypes.TWO_GIS,
    },
    {
      enabled: isFinishedTrip,
      suspense: false,
    }
  );

  const { segments: planSegments, distance: planDistance } = routePlanData ?? {};
  const { segments: factSegments, distance: factDistance } = routeFactData ?? {};
  const isFetching = isPlanFetching || isFactFetching;

  return {
    planSegments, factSegments, planDistance, factDistance, isFetching,
  };
};

export default useSegments;
