import React, { Suspense } from 'react';
import { Switch, Route } from 'react-router-dom';

import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import CargoAutoHandbookComponent from 'modules/CargoAuto/CargoAuto';
import CargoAutoDetailed from 'modules/CargoAuto/Components/CargoAutoDetailed';
import * as routes from 'constants/constants.routes';

const ROOT_ROUTE = `${routes.REFERENCE_BOOKS}/cargo`;

export const CargoAutoSettings = withErrorBoundary(() => (
  <Suspense fallback={<SpinWrapped />}>
    <Switch>
      <Route
        path={`${ROOT_ROUTE}/CargoAutoSettings`}
        component={CargoAutoHandbookComponent}
        exact
      />
      <Route path={`${ROOT_ROUTE}/CargoAutoSettings/:id`} component={CargoAutoDetailed} />
    </Switch>
  </Suspense>
));
