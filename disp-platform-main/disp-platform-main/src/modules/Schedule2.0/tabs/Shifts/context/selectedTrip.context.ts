import { useState } from 'react';

import { BusynessTrip, ScheduleVehicle } from 'api/schedule2.0/schedule.types';

import { createCallableCtx } from 'utils/createCallableContext';

interface TripWithVehicle extends BusynessTrip {
  vehicle: ScheduleVehicle;
}

const useHook = () => {
  const [selectedTrip, setSelectedTrip] = useState<TripWithVehicle | null>(null);

  return {
    selectedTrip,
    setSelectedTrip,
  };
};

export const [useSelectedTrip, SelectedTripProvider] = createCallableCtx(useHook, { name: 'SelectedTripProvider' });
