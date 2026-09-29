import React, { useState, useEffect } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { observer } from 'mobx-react';

import { CustomRoute as R } from 'shared/components/Breadcrumbs';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';
import { SpinWrapped } from 'shared/components';
import { StoreNames } from 'stores/StoreNames.enum';
import { LimitRequestInfo } from 'stores/Limits/Limit.interface';
import LimitRequestDetailedView from './LimitRequestDetailedView';

const LimitRequestDetailedRouter = observer(() => {
  const match = useRouteMatch();
  const { isDesktop } = usePlatformDetect();
  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();

  const [request, setRequest] = useState<LimitRequestInfo>(null);
  const [isLoading, setLoading] = useState(true);

  const { reqId } = match.params;

  useEffect(() => {
    limitsStore
      .getLimitRequest(reqId)
      .then(setRequest)
      .finally(() => {
        setLoading(false);
      });
  }, []);

  return (
    <R path={`${match.path}`} bc={`Заявка на пополнение лимита ${isDesktop ? request?.humanReadableId || '' : ''}`}>
      <R
        path={`${match.path}`}
        render={() => (isLoading ? <SpinWrapped /> : <LimitRequestDetailedView request={request} />)}
        exact
      />
    </R>
  );
});

export default LimitRequestDetailedRouter;
