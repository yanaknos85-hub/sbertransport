import { useEffect } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { LimitRequestModel } from 'stores/Limits/Models/LimitRequest.model';
import { StoreNames } from 'stores/StoreNames.enum';

export const useLimitRequestDetailedView = (): {
  currentLimitRequest?: LimitRequestModel;
  goToList: () => void;
} => {
  const { [StoreNames.limitsRequestStore]: limitsRequestStore } = useAppStoreContext();

  const match = useRouteMatch<{ reqId: string }>();
  const history = History();
  const { reqId } = match.params;

  useEffect(() => {
    if (reqId) {
      limitsRequestStore.setCurrentRequest(reqId);
    }
  }, [reqId, limitsRequestStore]);

  const goToList = (): void => {
    history.push('../');
  };

  return {
    currentLimitRequest: limitsRequestStore.currentRequest,
    goToList,
  };
};
