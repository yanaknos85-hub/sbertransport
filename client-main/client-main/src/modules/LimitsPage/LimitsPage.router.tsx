import React, { FC } from 'react';
import { Switch, useRouteMatch } from 'react-router-dom';
import { observer } from 'mobx-react';
import { CustomRoute as R, CustomSwitch as S } from 'shared/components/Breadcrumbs';
import { LimitsPage } from './LimitsPage';
import LimitSharing from './LimitSharing/LimitSharing';
import { LimitsRequestsRouter } from './limitRequests/LimitsRequest.router';

export const LimitsPageRouter: FC = observer(() => {
  const match = useRouteMatch();

  return (
    <Switch>
      <R path={`${match.path}/limitsInfo`} bc="Лимиты">
        <S>
          <R path={`${match.path}/limitsInfo/settings`} bc="Настройка лимитов">
            <R
              path={`${match.path}/limitsInfo/settings/:id`}
              component={LimitSharing}
              exact
            />
          </R>
          <R path={`${match.path}`}>
            <LimitsPage />
          </R>
        </S>
      </R>
      <R path={`${match.path}/limitRequests`} component={LimitsRequestsRouter} />
    </Switch>
  );
});
