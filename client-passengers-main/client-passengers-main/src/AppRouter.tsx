import React from 'react';
import type { FC } from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';
import { IS_REMOTE } from 'constants/constants.env';
import * as routes from 'constants/constants.routes';

import { MaybeBreadcrumbs } from 'shared/components/Breadcrumbs';
import { CustomRoute } from 'shared/components/Breadcrumbs/CustomRoutes';

import ApprovalRouter from 'modules/EmployeeApp/components/ApprovalPage/ApprovalRouter';
import { BonusesPageRouter } from 'modules/EmployeeApp/components/Bonuses/BonusesPage.router';
import FavoriteAddressPage from 'modules/EmployeeApp/components/FavoriteAddressPage/FavoriteAddressPage';
import _SuccessRequestComponent from 'modules/EmployeeApp/components/SuccessRequestComponent/_SuccessRequestComponent';
import SuccessRequestComponent from 'modules/EmployeeApp/components/SuccessRequestComponent/SuccessRequestComponent';
import TripsRouter from 'modules/EmployeeApp/components/TripsPage/TripsRouter';
import VehiclesRouter from 'modules/EmployeeApp/components/Vehicles/VehiclesRouter';

export const AppRouter: FC = () => (
  <>
    <MaybeBreadcrumbs />
    <Switch>
      <Route path={routes.TRIPS_CREATE_SUCCESS} component={SuccessRequestComponent} />
      <Route path={routes.TRANSPORT_2_0_SUCCESS} component={_SuccessRequestComponent} />
      <Route path={routes.TRIPS} component={TripsRouter} />
      <Route path={routes.APPROVEMENT} component={ApprovalRouter} />
      <Route path={routes.FAVORITE} component={FavoriteAddressPage} />
      <Route path={routes.BONUSES} component={BonusesPageRouter} />
      <Route path={routes.PERSONAL_CARS} component={VehiclesRouter} />
      <Route path={routes.VEHICLES} component={VehiclesRouter} />

      <CustomRoute
        path="*"
        component={() => <Redirect to={!IS_REMOTE ? routes.TRIPS_CREATE_TAXI : '/'} />}
      />
    </Switch>
  </>
);

export default AppRouter;
