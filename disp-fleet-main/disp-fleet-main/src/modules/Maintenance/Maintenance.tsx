import React, {
  FC, Suspense, useEffect, useMemo
} from 'react';

import { useHistory } from '@sber-sbertransport/mf-core';
import { Tabs, TabsPropsOriginal } from '@sber-sbertransport/ui-kit/src';

import { useParams } from 'react-router-dom';

import { useSelfAutopark } from 'api/contractors/contractors.api';

import { Roles } from 'constants/app.constants';
import { MAINTENANCE } from 'constants/routes.constants';

import { useQuery } from 'hooks/useQuery';
import { useRole } from 'hooks/useRole';

import ErrorBoundary from 'components/ErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';

import Table from './components/Table';
import { MaintenanceTabs } from './Maintenance.constants';

import type { MaintenanceFilters } from 'api/maintenance/maintenance.types';

interface TabItem {
  key: string;
  label: string;
  children?: React.ReactNode;
  /** Роли, которым таб виден; если не указано — виден всем */
  roles?: string[];
}

const ALL_TABS: TabItem[] = [
  {
    key: MaintenanceTabs.Repair,
    label: 'Ремонт',
    children: '',
  },
  {
    key: MaintenanceTabs.Maintenance,
    label: 'Техническое обслуживание',
    children: '',
  },
  {
    key: MaintenanceTabs.TireService,
    label: 'Шиномонтаж',
    children: '',
  },
  {
    key: MaintenanceTabs.Washing,
    label: 'Мойка',
    children: '',
  },
  {
    key: MaintenanceTabs.Evacuation,
    label: 'Эвакуатор',
    children: '',
  },
  {
    key: MaintenanceTabs.Registration,
    label: 'Регистрация',
    children: '',
    roles: [Roles.ROLE_DISPATCHER_ROOM_ADMIN, Roles.ROLE_FEDERAL_DISPATCHER_CONTRACTOR],
  },
];

const Maintenance: FC = () => {
  const { tab } = useParams();
  const history = useHistory();
  const roles = useRole();

  const {
    query, setPagination,
  } = useQuery<Omit<MaintenanceFilters, 'type'>>();

  const tabs = useMemo(() => (
    ALL_TABS.filter(
      tab => !tab.roles || tab.roles.some(role => roles.includes(role))
    )
  ), [roles]);

  useEffect(() => {
    if (!tab || !tabs.some(({ key }) => key === tab)) {
      history.replace(MAINTENANCE.replace(':tab', tabs[0].key));
    }
  }, [tab, history, tabs]);

  const onChangeTab = (key: string) => {
    history.push(MAINTENANCE.replace(':tab', key));
  };

  const activeTab = tab && tabs.some(({ key }) => key === tab)
    ? tab
    : tabs[0].key;

  const { isInternal } = useSelfAutopark().data;

  if (!isInternal) return null;

  return (
    <>
      <Tabs
        activeKey={tab}
        items={tabs as TabsPropsOriginal['items']}
        onChange={onChangeTab}
      />
      <ErrorBoundary key={activeTab}>
        <Suspense fallback={<SpinWrapped />}>
          <Table
            query={{
              ...query,
              type: activeTab as MaintenanceTabs,
            }}
            setPagination={setPagination}
          />
        </Suspense>
      </ErrorBoundary>
    </>
  );
};

export default Maintenance;
