
/* eslint-disable @stylistic/jsx-max-props-per-line */
import { observer } from 'mobx-react';
import React, { FC, lazy } from 'react';
import { Route, Switch } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import Panel from 'components/Panel';

const Organizations = lazy(() => import('modules/Organizations/Organizations'));
const OrganizationsDetailed = lazy(() => import('modules/Organizations/Organizations/OrganizationsDetailed'));

export const Router: FC = observer((): JSX.Element => {
  return (
    <Panel>
      <Switch>
        <Route path={routes.ORGANIZATIONS} component={Organizations} exact />
        <Route path={routes.ORGANIZATION} component={OrganizationsDetailed} />
      </Switch>
    </Panel>
  );
});

export default Router;
