import React, { FC } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { observer } from 'mobx-react';

import { CustomRoute as R } from 'shared/components/Breadcrumbs';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';
import { StoreNames } from 'stores/StoreNames.enum';
import UserLimitRequestDetailedView from './UserLimitRequestDetailedView';

export const UserLimitRequestDetailedRouter: FC = observer(() => {
  const { isDesktop } = usePlatformDetect();
  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();
  const { limitsRequestsByAuthor } = limitsStore;
  const match = useRouteMatch<{ reqId: string; filter: string }>();

  const { reqId } = match.params;
  const request = limitsRequestsByAuthor.find(({ id }) => id === reqId);

  return (
    <R path={`${match.path}`} bc={`Заявка на пополнение лимита ${isDesktop ? request?.humanReadableId ?? '' : ''}`}>
      <R
        path={`${match.path}`}
        component={UserLimitRequestDetailedView}
        exact
      />
    </R>
  );
});
