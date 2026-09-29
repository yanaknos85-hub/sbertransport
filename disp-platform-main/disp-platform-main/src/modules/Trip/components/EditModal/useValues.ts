import { useMemo } from 'react';
import { PassTrip } from 'api/trips/trips.types';

export const useValues = (trip: PassTrip) => {
  const defaultValues = useMemo(
    () => ({
      ...trip,
      driverWaitingTime: trip.driverWaitingTime
        ? Math.round(trip.driverWaitingTime / 60)
        : null,
      factCost: trip.factCost ? trip.factCost / 100 : undefined,
    }),
    [trip]
  );

  return { defaultValues };
};
