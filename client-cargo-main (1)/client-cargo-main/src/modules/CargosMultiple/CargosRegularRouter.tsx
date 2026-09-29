import React, { FC, ReactNode } from 'react';
import {
  Redirect, Route, Switch, useRouteMatch
} from 'react-router-dom';

import * as routes from 'constants/constants.routes';

import { RegularCargosRequestDetailed, RegularCargosRequestJournal } from '.';

const CargosRegularRouter: FC = () => {
  const match = useRouteMatch();

  return (
    <Switch>
      <Route
        path={routes.REGULAR_CARGOS_JOURNAL}
        component={RegularCargosRequestJournal}
        exact={true}
      />
      <Route
        path={routes.REGULAR_CARGOS_DETAILED}
        component={RegularCargosRequestDetailed}
        exact={true}
      />
      <Route render={(): ReactNode => <Redirect to={`${match.path}/active`} />} />
    </Switch>
  );
};

export default CargosRegularRouter;
