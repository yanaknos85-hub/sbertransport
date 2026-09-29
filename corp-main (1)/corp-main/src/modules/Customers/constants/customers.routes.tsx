import React, { lazy } from 'react';
import mfLoader from 'mf/MFLoader';

import * as routes from 'constants/constants.routes';

const Organizations = lazy(() => mfLoader(import('platform/modules/Customers/Organizations')));
const Contractors = lazy(() => import('../components/Contractors/ContractorsRouter'));
const Contracts = lazy(() => import('../components/Contracts/ContractsRouter'));
const Tariffs = lazy(() => import('../components/Tariffs/TariffsRouter'));
const Mutuality = lazy(() => mfLoader(import('platform/modules/CustomersMutuality')));

export const tabRoutes = [
  {
    title: 'Клиенты',
    route: routes.CUSTOMERS_ORGANIZATIONS,
    component: Organizations,
  },
  {
    title: 'Исполнители',
    route: routes.CUSTOMERS_CONTRACTORS,
    component: Contractors,
  },
  {
    title: 'Договоры',
    route: `${routes.CUSTOMERS}/contracts`,
    component: Contracts,
  },
  {
    title: 'Тарифы',
    route: `${routes.CUSTOMERS}/tariffs`,
    component: Tariffs,
  },
  {
    title: 'Взаиморасчеты',
    route: routes.CUSTOMERS_MUTUALITY,
    component: Mutuality,
  },
  {
    title: 'Аналитика',
    route: routes.CUSTOMERS_ANALYTICS,
    component: () => <div>Аналитика</div>,
  },
];
