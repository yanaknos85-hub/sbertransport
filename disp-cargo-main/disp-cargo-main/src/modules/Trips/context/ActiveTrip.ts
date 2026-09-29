import { CargoTrip } from 'api/trips-cargo/trips-cargo.types';
import { useState } from 'react';
import { createCallableCtx } from 'utils/createCallableContext';

const useHook = () => {
  const [activeTrip, setActiveTrip] = useState<CargoTrip | undefined>();

  return {
    activeTrip,
    setActiveTrip,
  };
};

export const [useActiveTrip, ActiveTripProvider] = createCallableCtx(useHook, { name: 'ActiveTripProvider' });
