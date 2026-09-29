/* eslint-disable @stylistic/jsx-max-props-per-line */
import { observer } from 'mobx-react';
import React, { FC, lazy } from 'react';
import { Route, Switch } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import Panel, { PanelTitle } from 'components/Panel';
import { ToolbarProvider } from 'components/Toolbar';

const Positions = lazy(() => import('modules/Positions/Positions'));
const PositionDetailed = lazy(() => import('modules/Positions/Components/PositionDetailed'));

export const Router: FC = observer((): JSX.Element => {
  return (
    <Panel>
      <ToolbarProvider dataName="passenger_dir_positions">
        <PanelTitle>Должности</PanelTitle>
        <Switch>
          <Route path={routes.EMPLOYEE_POSITIONS} component={Positions} exact />
          <Route path={routes.EMPLOYEE_POSITION} component={PositionDetailed} />
        </Switch>
      </ToolbarProvider>
    </Panel>
  );
});

export default Router;
