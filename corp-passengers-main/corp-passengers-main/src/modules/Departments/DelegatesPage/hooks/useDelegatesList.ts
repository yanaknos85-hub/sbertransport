import { useCallback } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames, UseDelegateListInterface } from 'stores/Delegates/Delegates.interface';

export const useDelegateList = (): UseDelegateListInterface => {
  const { [StoreNames.delegatesStore]: delegatesStore } = useAppStoreContext();

  const { namesWithInitials } = delegatesStore;

  const history = useHistory();
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
