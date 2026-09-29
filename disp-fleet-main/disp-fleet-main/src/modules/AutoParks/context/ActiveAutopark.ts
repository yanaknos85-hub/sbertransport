import { useState } from 'react';

import { AutoPark } from 'api/autopark/autopark.types';
import { createCallableCtx } from 'utils/createCallableContext';

const useHook = () => {
  const [activeAutopark, setActiveAutopark] = useState<AutoPark | null>(null);

  const toggleActiveAutopark = (autopark: AutoPark) => {
    if (activeAutopark?.id === autopark?.id) {
      setActiveAutopark(null);
      return;
    }

    setActiveAutopark(autopark);
  };

  return {
    activeAutopark,
    setActiveAutopark,
    toggleActiveAutopark,
  };
};

export const [useActiveAutopark, ActiveAutoparkProvider] = createCallableCtx(useHook, {
  name: 'ActiveAutoparkProvider',
});
