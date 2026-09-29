import { useCallback, useState } from 'react';
import { Driver } from 'api/drivers/drivers.types';
import { createCallableCtx } from 'utils/createCallableContext';

const useHook = () => {
  const [activeDriver, setActiveDriver] = useState<Driver | null>(null);

  const toggleActiveDriver = useCallback((driver: Driver) => {
    setActiveDriver(prev => (prev?.id === driver?.id ? null : driver));
  }, []);

  return {
    activeDriver,
    setActiveDriver,
    toggleActiveDriver,
  };
};

export const [useActiveDriver, ActiveDriverProvider] = createCallableCtx(useHook, {
  name: 'ActiveDriverProvider',
});
