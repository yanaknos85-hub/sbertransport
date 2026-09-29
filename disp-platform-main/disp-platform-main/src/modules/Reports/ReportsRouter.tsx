import React, { FC } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';

import { useSelfAutopark } from 'api/contractors/contractors.api';
import { REPORTS } from 'constants/routes.constants';

import Reports from './Reports';
import { ReportsTabs } from './Reports.constants';
import { TripTypes } from './TripsTab/constants/TripsTab.constants';

export const REPORTS_PASSENGER_TRIPS_ROUTE = `${REPORTS}/${ReportsTabs.Trips}/${TripTypes.Passenger}`;

const ReportsRouter: FC = () => {
  const { isInternal: accessAnalytics } = useSelfAutopark().data;

  const initialRoute = accessAnalytics ? `${REPORTS}/${ReportsTabs.Analytics}` : REPORTS_PASSENGER_TRIPS_ROUTE;

  return (
    <Switch>
      <Route path={`${REPORTS}/:tab/:type`} component={Reports} />
      <Route path={`${REPORTS}/:tab`} component={Reports} />

      <Redirect path={REPORTS} to={initialRoute} />

      <Redirect path={`${REPORTS}/${ReportsTabs.Trips}`} to={REPORTS_PASSENGER_TRIPS_ROUTE} />
    </Switch>
  );
};

export default ReportsRouter;
