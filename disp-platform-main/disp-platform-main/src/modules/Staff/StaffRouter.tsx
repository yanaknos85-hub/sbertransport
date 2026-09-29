import React, { FC, lazy } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';

import { STAFF } from 'constants/routes.constants';
import { StaffRoles } from 'constants/app.constants';

const Staff = lazy(() => import('./Staff'));

const StaffRouter: FC = () => (
  <Switch>
    <Route path={`${STAFF}/:role`} component={Staff} />

    <Redirect path={STAFF} to={`${STAFF}/${StaffRoles.Drivers}`} />
  </Switch>
);

export default StaffRouter;
