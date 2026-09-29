import React, { ReactNode } from 'react';

import * as routes from 'constants/constants.routes';

const PublicRegistryRouter = React.lazy(() => import('modules/PublicRegistry/components/PublicRegistryRouter'));
const PersonalRegistryRouter = React.lazy(() => import('modules/PersonalRegistry/PersonalRegistryRouter'));
const TaxiRegistryRouter = React.lazy(() => import('modules/TaxiRegistry/TaxiRegistryRouter'));
const CarSharingRegistryRouter = React.lazy(() => import('modules/CarSharingRegistry/CarSharingRegistryRouter'));
const GroupTransferRegistryRouter = React.lazy(() => import('modules/GroupTransferRegistry/GroupTransferRegistryRouter'));
const TaxiYandexRegistryRouter = React.lazy(() => import('modules/TaxiYandexRegistry/TaxiYandexRegistryRouter'));

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
    route: routes.REGISTRY_PASSENGERS,
    tabs: [
      {
        title: 'Общественный транспорт',
        route: routes.REGISTRY_PASSENGERS_PUBLIC,
        component: <PublicRegistryRouter />,
      },
      {
        title: 'Личный транспорт',
        route: routes.REGISTRY_PASSENGERS_PERSONAL,
        component: <PersonalRegistryRouter />,
      },
      {
        title: 'Такси',
        route: routes.REGISTRY_PASSENGERS_TAXI,
        component: <TaxiRegistryRouter />,
      },
      {
        title: 'Каршеринг',
        route: routes.REGISTRY_PASSENGERS_CARSHARING,
        component: <CarSharingRegistryRouter />,
      },
      {
        title: 'Трансфер',
        route: routes.REGISTRY_PASSENGERS_GROUP_TRANSFER,
        component: <GroupTransferRegistryRouter />,
      },
      {
        title: 'Яндекс Такси',
        route: routes.REGISTRY_PASSENGERS_YANDEX_TAXI,
        component: <TaxiYandexRegistryRouter />,
      },
    ],
  },
];
