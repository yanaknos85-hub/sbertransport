import { useCallback, useState } from 'react';

import { ContractorDispatcher } from 'api/dispatchers/dispatchers.types';
import { createCallableCtx } from 'utils/createCallableContext';

const useHook = () => {
  const [activeDispatcher, setActiveDispatcher] = useState<ContractorDispatcher | null>(null);

  const toggleActiveDispatcher = useCallback((dispatcher: ContractorDispatcher) => {
    setActiveDispatcher(prev => prev?.id === dispatcher.id ? null : dispatcher);
  }, []);

  return {
    activeDispatcher,
    setActiveDispatcher,
    toggleActiveDispatcher,
  };
};

export const [useActiveDispatcher, ActiveDispatcherProvider] = createCallableCtx(useHook, {
  name: 'ActiveDispatcherProvider',
});
