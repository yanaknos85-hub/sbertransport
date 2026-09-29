import { useCallback } from 'react';
import { useRouteMatch } from 'react-router-dom';

import { StoreNames } from 'stores/StoreNames.enum';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import * as routes from 'constants/constants.routes';

export interface UseDelegateListInterface {
  namesWithInitials: Record<string, string>;
  goToNewDelegate(): void;
  deleteHandler(id?: string): void;
  path: string;
}

export const useDelegateList = (): UseDelegateListInterface => {
  const {
    [StoreNames.delegatesStore]: delegatesStore,
    [StoreNames.configStore]: configStore,
  } = useAppStoreContext();

  const { namesWithInitials } = delegatesStore;
  const { history } = configStore;

  const match = useRouteMatch();

  const goToNewDelegate = useCallback(() => history.push(routes.APPROVEMENT_DELEGATE_CREATE), [history, match]);
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
