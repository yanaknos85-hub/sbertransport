import React, { ReactNode } from 'react';

import * as routes from 'constants/constants.routes';
import { PublicBusinessJournal } from 'modules/PublicBusinessReports/components/BusinessJournal/PublicBusinessJournal';
import { PersonalBusinessReports } from 'modules/PersonalBusinessReports/PersonalBusinessReports';
import { TaxiBusinessReports } from 'modules/TaxiBusinessReports/TaxiBusinessReports';
import { GroupTransferBusinessReports } from 'modules/GroupTransferBusinessReports/GroupTransferBusinessReports';
import CarSharingBusinessReportsRouter from 'modules/CarSharingBusinessReports/CarSharingBusinessReportsRouter';

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
    title: 'Пассажирские перевозки',
    route: routes.BUSINESS_REPORTS_PASSENGERS,
    tabs: [
      {
        title: 'Общественный транспорт',
        route: routes.BUSINESS_REPORTS_PASSENGERS_PUBLIC,
        component: <PublicBusinessJournal />,
      },
      {
        title: 'Личный транспорт',
        route: routes.BUSINESS_REPORTS_PASSENGERS_PERSONAL,
        component: <PersonalBusinessReports />,
      },
      {
        title: 'Такси',
        route: routes.BUSINESS_REPORTS_PASSENGERS_TAXI,
        component: <TaxiBusinessReports />,
      },
      {
        title: 'Каршеринг',
        route: routes.BUSINESS_REPORTS_PASSENGERS_CARSHARING,
        component: <CarSharingBusinessReportsRouter />,
      },
      {
        title: 'Трансфер',
        route: routes.BUSINESS_REPORTS_PASSENGERS_GROUP_TRANSFER,
        component: <GroupTransferBusinessReports />,
      },
    ],
  },
];
