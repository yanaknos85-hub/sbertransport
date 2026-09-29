/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC, Suspense } from 'react';
import { Switch, Route, Redirect } from 'react-router-dom';
import { observer } from 'mobx-react';

import ErrorBoundary from 'components/ErrorBoundary';
import useErrorBoundary from 'hooks/useErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';
import * as routes from 'constants/routes.constants';
import ProdRouter from '../prod/AppRouter';

export const AppRouter: FC = observer((): JSX.Element => {
  const { errorBoundaryRef } = useErrorBoundary();

  return (
    <ErrorBoundary ref={errorBoundaryRef} catchUnhandled>
      <Suspense fallback={<SpinWrapped />}>
        <Switch>
          <Route exact path="/" component={() => <Redirect to={routes.VEHICLES} />} />
          <Route exact path="/client" component={() => <Redirect to={routes.VEHICLES} />} />
          <ProdRouter />
        </Switch>
      </Suspense>
    </ErrorBoundary>
  );
});

export default AppRouter;
