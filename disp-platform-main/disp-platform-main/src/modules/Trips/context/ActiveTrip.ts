import { PassTrip } from 'api/trips/trips.types';
import { useState } from 'react';
import { createCallableCtx } from 'utils/createCallableContext';

const useHook = () => {
  const [activeTrip, setActiveTrip] = useState<PassTrip | undefined>();

  return {
    activeTrip,
    setActiveTrip,
  };
};

export const [useActiveTrip, ActiveTripProvider] = createCallableCtx(useHook, { name: 'ActiveTripProvider' });
