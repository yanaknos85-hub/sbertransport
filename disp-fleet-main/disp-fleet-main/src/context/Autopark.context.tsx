import React, { useEffect, useMemo, useState } from 'react';

import useAutoparkFilters from 'hooks/useAutoparkFilters';
import { isEmptyObject } from 'utils/utils';

export interface Props {
  autoparkId?: string;
  setAutoparkId: React.Dispatch<React.SetStateAction<string | undefined>>;
}

export const useAutopark = (): Props => {
  const { filters, saveFilters } = useAutoparkFilters();

  const [autoparkId, setAutoparkId] = useState(filters.autoparkId ?? undefined);

  useEffect(() => {
    if (autoparkId) {
      saveFilters({ autoparkId });
    }
  }, [autoparkId, saveFilters]);

  return useMemo(() => ({
    autoparkId,
    setAutoparkId,
  }), [autoparkId]);
};

export const AutoparkContext = React.createContext({} as Props);

export interface AutoparkContextOptions {
  /** Если есть необходимость вызова контекста вне провайдера. Не будет выдавать ошибку */
  out: boolean;
}

export const useAutoparkContext = (options: AutoparkContextOptions = { out: false }) => {
  const context = React.useContext(AutoparkContext);

  if (!options.out && isEmptyObject(context)) {
    throw new Error('AutoparkContext must be used within a AutoparkContext.Provider');
  }

  return context;
};
