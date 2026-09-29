import React, { ReactNode } from 'react';

import * as routes from 'constants/constants.routes';
import { CargoBusinessJournal } from 'modules/CargoBusinessReports/components/BusinessJournal/CargoBusinessJournal';
import TasksTab from 'modules/CargoBusinessReports/components/PublicBusinessReportsTable/Table';
import BusinessReportsProvider from 'modules/CargoBusinessReports/context/BusinessReportsProvider';

export interface OneLevelRoute {
  title: string;
  route: string;
  component: ReactNode;
  tabs?: never;
}

export interface TwoLevelRoutes {
  title: string;
  route: string;
  component?: ReactNode;
  tabs: OneLevelRoute[];
}

export const tabRoutes = [
  {
    title: 'Грузовые перевозки',
    route: routes.BUSINESS_REPORTS_CARGO,
    component: (
      <BusinessReportsProvider>
        <CargoBusinessJournal />
        <TasksTab />
      </BusinessReportsProvider>
    ),
  },
];
