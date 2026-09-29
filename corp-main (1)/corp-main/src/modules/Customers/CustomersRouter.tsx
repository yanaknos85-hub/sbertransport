import React, { FC } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import { observer } from 'mobx-react';

import * as routes from 'constants/constants.routes';
import { CustomRoute } from 'shared/components/Breadcrumbs';
import Customers from './Customers';

export const Router: FC = observer((): JSX.Element => {
  return (
    <Switch>
      <CustomRoute
        bc="Клиенты"
        path={`${routes.CUSTOMERS}/:tab`}
        bcPath={routes.CUSTOMERS}
        component={Customers}
      />

      <Route
        exact
        path={routes.CUSTOMERS}
        component={() => <Redirect to={routes.CUSTOMERS_ORGANIZATIONS} />}
      />
    </Switch>
  );
});

export default Router;
