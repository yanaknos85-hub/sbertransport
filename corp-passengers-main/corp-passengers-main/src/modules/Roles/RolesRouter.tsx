/* eslint-disable @stylistic/jsx-max-props-per-line */
import { observer } from 'mobx-react';
import React, { FC, lazy } from 'react';
import { Route, Switch } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import Panel from 'components/Panel';

const Roles = lazy(() => import('modules/Roles'));
const RoleDetailed = lazy(() => import('modules/Roles/Permissions'));

export const Router: FC = observer((): JSX.Element => {
  return (
    <Panel>
      <Switch>
        <Route path={routes.ROLES} component={Roles} exact />
        <Route path={routes.ROLE} component={RoleDetailed} />
      </Switch>
    </Panel>
  );
});

export default Router;
