/* eslint-disable @stylistic/jsx-max-props-per-line */
import { observer } from 'mobx-react';
import React, { FC, Suspense } from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';

import * as routes from 'constants/routes.constants';

// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { CustomRoute } from 'components/Breadcrumbs/CustomRoutes';
import ErrorBoundary from 'components/ErrorBoundary';
import useErrorBoundary from 'hooks/useErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';

export const AppRouter: FC = observer((): JSX.Element => {
  const { errorBoundaryRef } = useErrorBoundary();

  return (
    <ErrorBoundary ref={errorBoundaryRef} catchUnhandled>
      <Suspense fallback={<SpinWrapped />}>
        <Switch>
          <Route path="*" component={() => <Redirect to={routes.PAGE_404} />} />
        </Switch>
      </Suspense>
    </ErrorBoundary>
  );
});

export default AppRouter;
