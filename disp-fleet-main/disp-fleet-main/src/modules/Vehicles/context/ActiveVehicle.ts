import { useCallback, useState } from 'react';
import { createCallableCtx } from 'utils/createCallableContext';
import { Vehicle } from 'api/vehicles/vehicles.types';

const useHook = () => {
  const [activeVehicle, setActiveVehicle] = useState<Vehicle | null>(null);

  const toggleActiveVehicle = useCallback((vehicle: Vehicle) => {
    setActiveVehicle(prev => prev?.id === vehicle?.id ? null : vehicle);
  }, []);

  return {
    activeVehicle,
    setActiveVehicle,
    toggleActiveVehicle,
  };
};

export const [useActiveVehicle, ActiveVehicleProvider] = createCallableCtx(useHook, {
  name: 'ActiveVehicleProvider',
});
