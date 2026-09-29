import React, { FC, lazy } from 'react';
import { Switch } from 'react-router-dom';
import * as routes from 'constants/routes.constants';
import { CustomRoute } from 'components/Breadcrumbs/CustomRoutes';

const WaybillMassCreate = lazy(() => import('./WaybillMassCreate'));
const WaybillDetailed = lazy(() => import('./WaybillDetailed'));

const WaybillRouter: FC = () => (
  <Switch>
    <CustomRoute
      exact
      path={routes.WAYBILL}
      bc="Путевые листы"
    >
      <WaybillMassCreate />
    </CustomRoute>
    <CustomRoute
      path={routes.WAYBILL_DETAILED}
      bc="Путевой лист"
    >
      <WaybillDetailed />
    </CustomRoute>
  </Switch>
);

export default WaybillRouter;
