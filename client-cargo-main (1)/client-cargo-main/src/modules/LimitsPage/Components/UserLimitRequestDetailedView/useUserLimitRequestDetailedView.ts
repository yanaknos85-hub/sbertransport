import { useEffect } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';

import { ISpentActionsType } from '../../useDetailedLimitsMapper';

const useUserLimitRequestDetailedView = (): {
  currentLimitRequest: ISpentActionsType | undefined;
  goToList: () => void;
} => {
  const { [StoreNames.limitsStore]: limits } = useAppStoreContext();

  const match = useRouteMatch<{ reqId: string }>();
  const history = History();
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
