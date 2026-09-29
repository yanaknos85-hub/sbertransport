import { useEffect } from 'react';
import { useRouteMatch } from 'react-router-dom';

import { LimitRequestModel } from 'stores/Limits/Models/LimitRequest.model';
import { StoreNames } from 'stores/StoreNames.enum';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';

export const useLimitRequestDetailedView = (): {
  currentLimitRequest?: LimitRequestModel;
  goToList: () => void;
} => {
  const {
    [StoreNames.configStore]: configStore,
    [StoreNames.limitsRequestStore]: limitsRequestStore,
  } = useAppStoreContext();

  const match = useRouteMatch<{ reqId: string }>();
  const { reqId } = match.params;

  useEffect(() => {
    if (reqId) {
      limitsRequestStore.setCurrentRequest(reqId);
    }
  }, [reqId, limitsRequestStore]);

  const goToList = (): void => {
    configStore.history.push('../');
  };

  return {
    currentLimitRequest: limitsRequestStore.currentRequest,
    goToList,
  };
};
