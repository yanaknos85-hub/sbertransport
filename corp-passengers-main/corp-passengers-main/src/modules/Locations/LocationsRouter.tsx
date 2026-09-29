/* eslint-disable @stylistic/jsx-max-props-per-line */
import { observer } from 'mobx-react';
import React, { FC, lazy } from 'react';
import { Route, Switch } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import Panel from 'components/Panel';

const Locations = lazy(() => import('modules/Locations/Locations'));
const LocationDetailed = lazy(() => import('modules/Locations/Components/LocationDetailed'));

export const Router: FC = observer((): JSX.Element => {
  return (
    <Panel>
      <Switch>
        <Route path={routes.LOCATIONS} component={Locations} exact />
        <Route path={routes.LOCATION} component={LocationDetailed} />
      </Switch>
    </Panel>
  );
});

export default Router;
