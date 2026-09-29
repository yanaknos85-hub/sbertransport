import { useState } from 'react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';

export interface UseDelegateListInterface {
  isLoading: boolean;
  fullDelegateNames: Record<string, string>;
  deleteHandler(id?: string): void;
}

export const useDelegateList = (): UseDelegateListInterface => {
  const { [StoreNames.delegatesStore]: delegatesStore } = useAppStoreContext();
  const [isLoading, setLoading] = useState(false);

  const { fullDelegateNames } = delegatesStore;

  const deleteHandler = async (id?: string): Promise<void> => {
    if (id) {
      setLoading(true);
      await delegatesStore.deleteDelegate(id);
      setLoading(false);
    }
  };

  return {
    isLoading,
    fullDelegateNames,
    deleteHandler,
  };
};
