import { useCallback, useMemo } from 'react';
import { useTripsQuery } from '../../context/TripsQuery';
import { TripStatisticStatuses } from '../../constants';
import {
  TRIP_STATUSES, activeTripStatuses, distribTripStatuses, notDistribTripStatuses, tripStatuses
} from 'constants/trips.constants';

export const useStatisticQuery = (): [
  TripStatisticStatuses | null,
  (type: TripStatisticStatuses, statuses: TRIP_STATUSES[]) => void
] => {
  const { query, setQuery } = useTripsQuery();

  const isSameTripStatuses = (statuses: TRIP_STATUSES[]) => (
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    query.statuses!.length === statuses.length && query.statuses!.every((status: any) => statuses.includes(status))
  );

  const activeStatisticType = useMemo(() => {
    if (!query.statuses?.length) return null;

    if (isSameTripStatuses(activeTripStatuses)) return TripStatisticStatuses.TotalCount;
    if (isSameTripStatuses(distribTripStatuses)) return TripStatisticStatuses.AssignCount;
    if (isSameTripStatuses(notDistribTripStatuses)) return TripStatisticStatuses.NotAssignCount;

    return null;
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query.statuses]);

  const setActiveStatistic = useCallback(
    (type, statuses) => {
      setQuery({
        ...query,
        statuses: activeStatisticType !== type ? statuses : tripStatuses,
        requestHumanReadableId: undefined,
      });
    },
    [query, setQuery, activeStatisticType]
  );

  return [activeStatisticType, setActiveStatistic];
};
