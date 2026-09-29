/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC } from 'react';
import { Switch } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import { CustomRoute } from 'shared/components/Breadcrumbs/CustomRoutes';

import ProdRouter from '../prod/AppRouter';

import Registry from 'modules/Registry/Registry';
import OrderExecution from 'modules/OrderExecution/OrderExecution';
import TripSettings from 'modules/TripSettings/TripSettings';
import ServiceSettings from 'modules/ServiceSettings/ServiceSettings';
import TariffSettings from 'modules/TariffSettings/TariffSettings';
import BusinessReports from 'modules/BusinessReports/BusinessReports';

export const AppRouter: FC = (): JSX.Element => {
  return (
    <Switch>
      <CustomRoute path={routes.REGISTRY} component={Registry} bc="Реестр" />
      <CustomRoute path={routes.BUSINESS_REPORTS} component={BusinessReports} bc="Бизнес-отчеты" />
      <CustomRoute path={routes.ORDER_EXECUTION} component={OrderExecution} bc="Исполнение заявок" />
      <CustomRoute path={routes.TRIP_SETTINGS} component={TripSettings} />
      <CustomRoute path={routes.SERVICE_SETTINGS} component={ServiceSettings} />
      <CustomRoute path={routes.TARIFF_SETTINGS} component={TariffSettings} />

      <ProdRouter />
    </Switch>
  );
};

export default AppRouter;
