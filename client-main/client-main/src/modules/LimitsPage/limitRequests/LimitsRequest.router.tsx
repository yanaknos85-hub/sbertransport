import React, { FC, useEffect, ReactNode } from 'react';
import { useRouteMatch, Redirect } from 'react-router-dom';
import { observer } from 'mobx-react';

import { CustomRoute as R, CustomSwitch as S } from 'shared/components/Breadcrumbs';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';

import RequestsList from './LimitRequests';
import { UserLimitRequestDetailedRouter } from '../UserLimitRequestDetailedView/UserLimitRequestDetailedRouter';

export const LimitsRequestsRouter: FC = observer(() => {
  const match = useRouteMatch();
  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();
  const { getLimitsRequestsByAuthor } = limitsStore;

  useEffect(() => {
    getLimitsRequestsByAuthor();
  }, [getLimitsRequestsByAuthor]);

  return (
    <S>
      <R path={`${match.path}/:filter`} bc="Лимиты">
        <S>
          <R path={`${match.path}/:filter/:reqId`} component={UserLimitRequestDetailedRouter} />
          <R path={`${match.path}/:filter`}>
            <RequestsList />
          </R>
        </S>
      </R>
      <R render={(): ReactNode => <Redirect to={`${match.path}/active`} />} />
    </S>
  );
});
