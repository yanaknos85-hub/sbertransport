import { useEffect } from 'react';
import { useRouteMatch } from 'react-router-dom';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';

import { ISpentActionsType } from '../../useDetailedLimitsMapper';

const useUserLimitRequestDetailedView = (): {
  currentLimitRequest: ISpentActionsType | undefined;
  goToList: () => void;
} => {
  const { [StoreNames.limitsStore]: limits, [StoreNames.configStore]: configStore } = useAppStoreContext();

  const match = useRouteMatch<{ reqId: string }>();
  const { history } = configStore;
  const { reqId } = match.params;

  useEffect(() => {
    if (reqId) {
      limits.setCurrentRequest(reqId);
    }
  }, [reqId, limits]);

  const goToList = (): void => {
    history.push('../');
  };

  return {
    currentLimitRequest: limits.currentRequest,
    goToList,
  };
};

export default useUserLimitRequestDetailedView;
