import React, { useEffect, useMemo, useState } from 'react';

import useAutoparkFilters from 'hooks/useAutoparkFilters';
import { isEmptyObject } from 'utils/utils';

export interface Props {
  branchId?: string;
  setBranchId: React.Dispatch<React.SetStateAction<string | undefined>>;
}

/** Значение branchId может быть "null" */
export const useBranch = (): Props => {
  const { filters, saveFilters } = useAutoparkFilters();

  const [branchId, setBranchId] = useState(filters.branchId ?? undefined);

  useEffect(() => {
    saveFilters({ branchId: branchId ?? 'null' });
  }, [branchId, saveFilters]);

  return useMemo(() => ({
    branchId,
    setBranchId,
  }), [branchId]);
};

export const BranchContext = React.createContext({} as Props);
export interface BranchContextOptions {
  /** Если есть необходимость вызова контекста вне провайдера. Не будет выдавать ошибку */
  out: boolean;
}

/** Значение branchId может быть "null" */
export const useBranchContext = (options: BranchContextOptions = { out: false }) => {
  const context = React.useContext(BranchContext);

  if (!options.out && isEmptyObject(context)) {
    throw new Error('BranchContext must be used within a BranchContext.Provider');
  }

  return context;
};

