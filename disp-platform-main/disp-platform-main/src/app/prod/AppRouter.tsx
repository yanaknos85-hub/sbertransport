/* eslint-disable @stylistic/jsx-max-props-per-line */
import { observer } from 'mobx-react';
import React, { FC, Suspense, lazy } from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';

import * as routes from 'constants/routes.constants';

// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { CustomRoute } from 'components/Breadcrumbs/CustomRoutes';
import ErrorBoundary from 'components/ErrorBoundary';
import useErrorBoundary from 'hooks/useErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';

const Trip = lazy(() => import('modules/Trip/Trip'));
const Staff = lazy(() => import('modules/Staff/StaffRouter'));
const Schedule = lazy(() => import('modules/Schedule2.0/ScheduleRouter'));
const Reports = lazy(() => import('modules/Reports/ReportsRouter'));
const Support = lazy(() => import('modules/Support/Support'));

export const AppRouter: FC = observer((): JSX.Element => {
  const { errorBoundaryRef } = useErrorBoundary();

  return (
    <ErrorBoundary ref={errorBoundaryRef} catchUnhandled>
      <Suspense fallback={<SpinWrapped />}>
        <Switch>
          <Route path={routes.TRIP} component={Trip} />
          <Route path={routes.STAFF} component={Staff} />
          <Route path={routes.SCHEDULE} component={Schedule} />
          <Route path={routes.REPORTS} component={Reports} />
          <Route path={routes.SUPPORT} component={Support} />

          <Route path="*" component={() => <Redirect to={routes.PAGE_404} />} />
        </Switch>
      </Suspense>
    </ErrorBoundary>
  );
});

export default AppRouter;
