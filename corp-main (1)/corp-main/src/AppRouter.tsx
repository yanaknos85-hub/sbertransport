/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC, Suspense } from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';

import mfLoader from 'mf/MFLoader';

import * as routes from 'constants/constants.routes';

import { CustomRoute } from 'shared/components/Breadcrumbs/CustomRoutes';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import useErrorBoundary from 'shared/hooks/useErrorBoundary';
import useRedirect from 'shared/hooks/useRedirect';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';

const MF_Platform = React.lazy(() => mfLoader(import('platform/App')));
const MF_Passengers = React.lazy(() => mfLoader(import('passengers/App')));
const MF_Cargo = React.lazy(() => mfLoader(import('cargo/App')));
const MF_Fleet = React.lazy(() => mfLoader(import('fleet/App')));

const ImportReport = React.lazy(() => import('modules/ImportReport/ImportReport'));
const Home = React.lazy(() => import('modules/Home/Home'));
const RedesignHome = React.lazy(() => import('modules/RedesignHome/Home'));
const Page404 = React.lazy(() => import('modules/Page404/Page404'));
const OrderExecutionRouter = React.lazy(() => import('modules/OrderExecution/OrderExecutionRouter'));

// У реестров есть внешний загрузчик mfDataLoader, поэтому через lazy он не работает!
import Reports from 'modules/Reports/Reports';

// import PlannerSRM from 'modules/PlannerSRM/PlannerSRM';

const TripSettings = React.lazy(() => import('modules/TripSettings/TripSettings'));
const ServiceSettings = React.lazy(() => import('modules/ServiceSettings/ServiceSettings'));
const TariffSettings = React.lazy(() => import('modules/TariffSettings/TariffSettings'));
const CustomersRouter = React.lazy(() => import('modules/Customers/CustomersRouter'));
const Autopark = React.lazy(() => import('modules/Autopark'));

export const AppRouter: FC = (): JSX.Element => {
  const { errorBoundaryRef } = useErrorBoundary();
  useRedirect();

  return (
    <ErrorBoundary ref={errorBoundaryRef}>
      <Suspense fallback={<SpinWrapped />}>
        <Switch>
          <CustomRoute path={routes.HOME} component={Home} />
          <CustomRoute path={routes.REDESIGN_HOME} component={RedesignHome} />
          <CustomRoute path={routes.CUSTOMERS} component={CustomersRouter} />
          <CustomRoute path={routes.PAGE_404} component={Page404} />
          <CustomRoute path={routes.IMPORT_REPORT} component={ImportReport} />

          <Route path={routes.MF_PLATFORM} component={MF_Platform} />
          <Route path={routes.MF_PASSENGERS} component={MF_Passengers} />
          <Route path={routes.MF_CARGO} component={MF_Cargo} />
          <Route path={routes.MF_FLEET} component={MF_Fleet} />

          <Route path={routes.ORDER_EXECUTION} component={OrderExecutionRouter} />

          <Route path={routes.TRIP_SETTINGS} component={TripSettings} />

          <Route path={routes.SERVICE_SETTINGS} component={ServiceSettings} />

          <Route path={routes.TARIFF_SETTINGS} component={TariffSettings} />

          <Route path={routes.REPORTS} component={Reports} />

          <Route path={routes.AUTOPARKS} component={Autopark} />

          {/* <Route path={routes.PLANNER_SRM} component={PlannerSRM} /> */}

          <Route exact path={routes.MAIN} component={() => <Redirect to={routes.HOME} />} />
          <Route path="*" component={() => <Redirect to={routes.PAGE_404} />} />
        </Switch>
      </Suspense>
    </ErrorBoundary>
  );
};

export default AppRouter;
