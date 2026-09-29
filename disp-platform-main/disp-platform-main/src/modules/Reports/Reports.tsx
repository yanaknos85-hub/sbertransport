import React, {
  ComponentProps, FC, Key, Suspense
} from 'react';

import { useSelfAutopark } from 'api/contractors/contractors.api';
import { Roles, rolesCanAccessAnalytics } from 'constants/app.constants';
import ErrorBoundary from 'components/ErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';
import { Tab } from 'components/Tab';
import { TabPane } from 'components/Tab/TabPane';
import { useRole } from 'hooks/useRole';
import { useTab } from 'hooks/useTab';

import { ReportsTabs } from './Reports.constants';

const Analytics = React.lazy(() => import('./Analytics/Analytics'));
const TripsTab = React.lazy(() => import('./TripsTab/TripsTab'));

interface ITab extends ComponentProps<typeof TabPane> {
  component?: FC;
  key: Key;
}

// Табы скрыты до реализации по просьбе ВП
const useTabs = (): ITab[] => {
  const { isInternal: accessAnalytics } = useSelfAutopark().data;
  const roles = useRole();
  const isAvailableAnalytics = accessAnalytics && roles.some(role => rolesCanAccessAnalytics.includes(role as Roles));

  return ([
    ...(isAvailableAnalytics
      ? [
        {
          tab: 'Аналитика',
          key: ReportsTabs.Analytics,
          component: Analytics,
        },
      ]
      : []),
    {
      tab: 'Перевозки',
      key: ReportsTabs.Trips,
      component: TripsTab,
    },
  // {
  //   tab: 'Автосервис',
  //   key: ReportsTabs.CarService,
  // },
  // {
  //   tab: 'Финансы',
  //   key: ReportsTabs.Finance,
  // },
  ]);
};

const Reports: FC = () => {
  const [tab, setTab] = useTab<ReportsTabs>();

  const tabs = useTabs();

  return (
    <Tab
      activeKey={tab}
      onChange={setTab}
      size="large"
      destroyInactiveTabPane
    >
      {tabs.map(({ component: Component, ...tab }) => (
        <TabPane disabled={!Component} {...tab}>
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>
              {Component && <Component />}
            </Suspense>
          </ErrorBoundary>
        </TabPane>
      ))}
    </Tab>
  );
};

export default Reports;
