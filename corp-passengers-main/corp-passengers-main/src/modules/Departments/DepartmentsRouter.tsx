/* eslint-disable @stylistic/jsx-max-props-per-line */
import { observer } from 'mobx-react';
import React, { FC, lazy } from 'react';
import { Route, Switch } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import Panel from 'components/Panel';

const Departments = lazy(() => import('modules/Departments/Departments'));
const DepartmentDetailed = lazy(() => import('modules/Departments/Components/DepartmentDetailedComponent'));

export const Router: FC = observer((): JSX.Element => {
  return (
    <Panel>
      <Switch>
        <Route path={routes.DEPARTMENTS} component={Departments} exact />
        <Route path={routes.DEPARTMENT} component={DepartmentDetailed} />
      </Switch>
    </Panel>
  );
});

export default Router;
