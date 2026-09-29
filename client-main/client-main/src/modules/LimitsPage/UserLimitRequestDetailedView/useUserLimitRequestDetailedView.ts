import { useEffect } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';

import { ISpentActionsType } from '../useDetailedLimitsMapper';

const useUserLimitRequestDetailedView = (): {
  currentLimitRequest: ISpentActionsType | undefined;
  goToList: () => void;
} => {
  const { [StoreNames.limitsStore]: limits } = useAppStoreContext();

  const match = useRouteMatch<{ reqId: string; filter: string }>();
  const history = useHistory();
  const { reqId, filter } = match.params;

  useEffect(() => {
    if (reqId) {
      limits.setCurrentRequest(reqId);
    }
  }, [reqId, limits]);

  const goToList = (): void => {
    history.push(`../${filter}`);
  };

  return {
    currentLimitRequest: limits.currentRequest,
    goToList,
  };
};

export default useUserLimitRequestDetailedView;
