import React, { FC, lazy, Suspense } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import mfLoader from 'mf/MFLoader';

import * as routes from 'constants/constants.routes';

import { CustomRoute } from 'shared/components/Breadcrumbs';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';
import useErrorBoundary from 'shared/hooks/useErrorBoundary';

import { BonusesPageRouter } from 'modules/Bonuses/BonusesPage.router';
import DashboardCards from 'modules/DashboardCards/DashboardCards';
import { LimitsPageRouter } from 'modules/LimitsPage/LimitsPage.router';

const ApprovalRouter = lazy(() => import('modules/ApprovalPage/Approval.router'));
const FavoriteAddressPage = lazy(() => import('modules/FavoriteAddressPage/FavoriteAddressPage'));
const SupportPage = lazy(() => import('modules/SupportPage/SupportPage'));
const ProfilePage = lazy(() => import('modules/ProfilePage/ProfilePage'));

const Page404 = React.lazy(() => import('modules/Page404/Page404'));

const MF_Passengers = React.lazy(() => mfLoader(import('passengers/App')));
const MF_Cargo = React.lazy(() => mfLoader(import('cargo/App')));
const MF_Fleet = React.lazy(() => mfLoader(import('fleet/App')));

const AppRouter: FC = () => {
  const { errorBoundaryRef } = useErrorBoundary();

  return (
    <ErrorBoundary ref={errorBoundaryRef}>
      <Suspense fallback={<SpinWrapped />}>
        <Switch>
          <CustomRoute path={routes.HOME} component={DashboardCards} />
          <CustomRoute path={routes.PAGE_404} component={Page404} />

          <Route path={routes.MF_PASSENGERS} component={MF_Passengers} />
          <Route path={routes.MF_CARGO} component={MF_Cargo} />
          <Route path={routes.MF_FLEET} component={MF_Fleet} />

          <Route path={routes.APPROVEMENT} component={ApprovalRouter} />
          <Route path={routes.LIMITS} component={LimitsPageRouter} />
          <Route path={routes.FAVORITE} component={FavoriteAddressPage} />
          <Route path={routes.SUPPORT} component={SupportPage} />
          <Route path={routes.PROFILE} component={ProfilePage} />
          <Route path={routes.BONUSES} component={BonusesPageRouter} />

          <Route
            exact
            path={routes.MAIN}
            component={() => <Redirect to={routes.HOME} />}
          />
          <Route path="*" component={() => <Redirect to={routes.PAGE_404} />} />
        </Switch>
      </Suspense>
    </ErrorBoundary>
  );
};

export default AppRouter;
