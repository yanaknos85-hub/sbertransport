import React, { FC, ReactNode } from 'react';
import { Redirect, useRouteMatch } from 'react-router-dom';
import { observer } from 'mobx-react';

import { CustomRoute as R, CustomSwitch as S } from 'shared/components/Breadcrumbs';
import LimitRequestDetailedRouter from './LimitRequestPage/LimitRequestDetailedView/LimitRequestDetailedRouter';
import LimitRequestList from './LimitRequestPage/LimitRequestList/LimitRequestList';

const LimitRequestRouter: FC = observer(() => {
  const match = useRouteMatch();

  return (
    <S>
      <R path={`${match.path}/:filter`} bc="Лимиты">
        <S>
          <R path={`${match.path}/:filter/:reqId`} component={LimitRequestDetailedRouter} />
          <R path={`${match.path}/:filter`}>
            <LimitRequestList />
          </R>
        </S>
      </R>
      <R render={(): ReactNode => <Redirect to={`${match.path}/active`} />} />
    </S>
  );
});

export default LimitRequestRouter;
