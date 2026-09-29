import React, { FC, ReactNode } from 'react';
import {
  Redirect, Route, Switch, useRouteMatch
} from 'react-router-dom';

import * as routes from 'constants/constants.routes';

import { CargosRequestDetailed, CargosRequestJournal } from '.';

const CargosRouter: FC = () => {
  const match = useRouteMatch();

  return (
    <Switch>
      <Route
        path={routes.CARGOS_JOURNAL}
        component={CargosRequestJournal}
        exact={true}
      />
      <Route
        path={routes.CARGOS_DETAILED}
        component={CargosRequestDetailed}
        exact={true}
      />
      <Route render={(): ReactNode => <Redirect to={`${match.path}/active`} />} />
    </Switch>
  );
};

export default CargosRouter;
