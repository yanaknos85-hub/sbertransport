/* eslint-disable @stylistic/jsx-max-props-per-line */
import { observer } from 'mobx-react';
import React, { FC, lazy } from 'react';
import { Route, Switch } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import Panel from 'components/Panel';

const EmployeeAttributes = lazy(() => import('modules/EmployeesAttributes/Components/Table/EmployeesAttributeJournal'));

export const Router: FC = observer((): JSX.Element => {
  return (
    <Panel>
      <Switch>
        <Route path={routes.EMPLOYEE_ATTRIBUTES} component={EmployeeAttributes} exact />
      </Switch>
    </Panel>
  );
});

export default Router;
