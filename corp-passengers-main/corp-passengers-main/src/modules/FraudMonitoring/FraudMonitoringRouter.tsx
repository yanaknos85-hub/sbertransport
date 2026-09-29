import React, { Suspense } from 'react';
import { Route, Switch } from 'react-router-dom';

import * as routes from 'constants/constants.routes';

import { ToolbarProvider } from 'components/Toolbar';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';

import FraudMonitoring from './FraudMonitoring';
import { FraudMonitoringDetails } from './components/FraudMonitoringDetails/FraudMonitoringDetails';

export const FraudMonitoringRouter = withErrorBoundary(() => (
  <Switch>
    <ToolbarProvider>
      <Route path={routes.FRAUD_MONITORING} exact>
        <Suspense fallback={<SpinWrapped />}>
          <FraudMonitoring />
        </Suspense>
      </Route>
      <Route path={routes.FRAUD_MONITORING_DETAILS} exact>
        <Suspense fallback={<SpinWrapped />}>
          <FraudMonitoringDetails />
        </Suspense>
      </Route>
    </ToolbarProvider>
  </Switch>
));

export default FraudMonitoringRouter;
