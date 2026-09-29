import React, { FC, ReactNode } from 'react';
import {
  Redirect, Route, Switch, useRouteMatch
} from 'react-router-dom';
import { observer } from 'mobx-react';

import * as routes from 'constants/constants.routes';

import {
  CargosApprovalDetailed as CargosMultipleApprovalDetailed,
  RegularCargosApprovalDetailed as RegularCargosMultipleApprovalDetailed,
  RegularCargosApprovalJournal as RegularCargosMultipleApprovalJournal
} from '../CargosMultiple';
import CargosApprovalTabs from '../CargosMultiple/CargoApproval/CargosApprovalTabs';

const ApprovalRouter: FC = observer(() => {
  const match = useRouteMatch();

  return (
    <Switch>
      <Route path={routes.APPROVEMENT_CARGOS_DETAILED} component={CargosMultipleApprovalDetailed} />
      <Route path={routes.APPROVEMENT_REGULAR_CARGOS_DETAILED} component={RegularCargosMultipleApprovalDetailed} />

      <Route
        exact
        path={routes.APPROVEMENT_CARGOS_JOURNAL}
        component={CargosApprovalTabs}
      />
      <Route
        exact
        path={routes.APPROVEMENT_REGULAR_CARGOS_JOURNAL}
        component={RegularCargosMultipleApprovalJournal}
      />
      <Route
        exact
        path={routes.APPROVEMENT_COMPENSATION_JOURNAL}
        component={CargosApprovalTabs}
      />

      <Route render={(): ReactNode => <Redirect to={`${match.path}/active`} />} />
    </Switch>
  );
});

export default ApprovalRouter;
