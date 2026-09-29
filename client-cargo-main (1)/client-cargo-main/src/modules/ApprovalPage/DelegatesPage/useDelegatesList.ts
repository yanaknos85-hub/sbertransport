import { useCallback } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';

export interface UseDelegateListInterface {
  namesWithInitials: Record<string, string>;
  goToNewDelegate(): void;
  deleteHandler(id?: string): void;
  path: string;
}

export const useDelegateList = (): UseDelegateListInterface => {
  const { [StoreNames.delegatesStore]: delegatesStore } = useAppStoreContext();

  const { namesWithInitials } = delegatesStore;

  const history = History();
  const match = useRouteMatch();

  const goToNewDelegate = useCallback(() => history.push(`${match.path}/newDelegate`), [history, match]);
  const deleteHandler = (id?: string): void => {
    if (id) {
      delegatesStore.deleteDelegate(id);
    }
  };

  return {
    goToNewDelegate,
    namesWithInitials,
    deleteHandler,
    path: match.path,
  };
};
