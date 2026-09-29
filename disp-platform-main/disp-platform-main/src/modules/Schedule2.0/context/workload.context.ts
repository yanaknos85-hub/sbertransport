import { useState } from 'react';

import { createCallableCtx } from 'utils/createCallableContext';

const useHook = () => {
  const [workloadGeneral, setWorkloadGeneral] = useState<number>();

  return {
    workloadGeneral,
    setWorkloadGeneral,
  };
};

export const [useWorkloadGeneral, WorkloadGeneralProvider] = createCallableCtx(useHook, { name: 'WorkloadGeneralProvider' });
