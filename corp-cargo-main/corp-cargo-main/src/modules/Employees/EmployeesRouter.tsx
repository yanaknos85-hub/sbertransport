/* eslint-disable @stylistic/jsx-max-props-per-line */
import { observer } from 'mobx-react';
import React, { FC, lazy } from 'react';
import { Route, Switch } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import Panel from 'components/Panel';

const Employees = lazy(() => import('modules/Employees/Employees'));
const EmployeeCreate = lazy(() => import('modules/Employees/CreateUser'));
const EmployeeDetailed = lazy(() => import('modules/Employees/EditEmployee'));

export const Router: FC = observer((): JSX.Element => {
  return (
    <Panel>
      <Switch>
        <Route path={routes.EMPLOYEES} component={Employees} exact />
        <Route path={routes.EMPLOYEE_ADD} component={EmployeeCreate} />
        <Route path={routes.EMPLOYEE} component={EmployeeDetailed} />
      </Switch>
    </Panel>
  );
});

export default Router;
