import React, { ReactNode } from 'react';

import * as routes from 'constants/constants.routes';

const CargoOrdersRegistryRouter = React.lazy(() => import('modules/CargoRegistry/CargoRegistryRouter'));
const CargoRoutesRegistryRouter = React.lazy(() => import('modules/CargoRoutesRegistry/CargoRegistryRouter'));
const CargoCompensationsRegistryRouter = React.lazy(() => import('modules/CargoCompensationsRegistry/CargoRegistryRouter'));
const CargoCompensationsReportsRegistryRouter = React.lazy(() => import('modules/CargoCompensationsReportsRegistry/CargoRegistryRouter'));

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
    route: routes.REGISTRY_CARGO,
    tabs: [
      {
        title: 'Заявки',
        route: routes.REGISTRY_CARGO_ORDERS,
        component: <CargoOrdersRegistryRouter />,
      },
      {
        title: 'Маршруты',
        route: routes.REGISTRY_CARGO_ROUTES,
        component: <CargoRoutesRegistryRouter />,
      },
      {
        title: 'Компенсации',
        route: routes.REGISTRY_CARGO_COMPENSATIONS,
        component: <CargoCompensationsRegistryRouter />,
      },
      {
        title: 'Выгрузки по компенсации',
        route: routes.REGISTRY_CARGO_COMPENSATIONS_REPORTS,
        component: <CargoCompensationsReportsRegistryRouter />,
      },
    ]
  },
];
