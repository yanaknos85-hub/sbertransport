import React, { FC } from 'react';

import { useHistory } from '@sber-sbertransport/mf-core';

import { useParams } from 'react-router-dom';

import { useShiftConflicts } from 'api/shift-conflicts/shift-conflicts.api';

import { SCHEDULE } from 'constants/routes.constants';

import { useRoleMap } from 'hooks/useRoleMap';

import { useTranslation } from 'i18n';

import ErrorBoundary from 'components/ErrorBoundary';
import Flex from 'components/Flex/Flex';
import Panel from 'components/Panel/Panel';
import { Tab } from 'components/Tab';
import { TabPane } from 'components/Tab/TabPane';

import { Workload } from './components/Workload/Workload';
import { SchedulerTabs } from './constants/schedule.constants';
import { ModalsProvider } from './context/modal.context';
import { SelectedShiftProvider } from './context/selectedShift.context';
import { WorkloadGeneralProvider } from './context/workload.context';
import { CreateModal } from './modals/CreateModal/CreateModal';
import Conflicts from './tabs/Conflicts/Conflicts';
import Shifts from './tabs/Shifts';

import styles from './Schedule.module.scss';

const Schedule: FC = () => {
  const { t } = useTranslation();
  const { tab } = useParams<{ tab: SchedulerTabs }>();
  const { isAdmin } = useRoleMap();

  const history = useHistory();

  const handleTabChange = (tab: string) => {
    history.push(`${SCHEDULE}/${tab}`);
  };

  // Получаем данные, чтобы отобразить количество в табах
  const { data: conflictsData } = useShiftConflicts({
    page: 0,
    size: 1,
  }, { suspense: false, enabled: isAdmin });

  return (
    <WorkloadGeneralProvider>
      <SelectedShiftProvider>
        <ModalsProvider>
          <Panel
            smallVerticalPadding
            title={t.Shifts.title}
            actions={tab === SchedulerTabs.Conflict ? null : <Workload />}
          >
            {isAdmin ? (
              <Tab
                activeKey={tab}
                onChange={handleTabChange}
                destroyInactiveTabPane
                type="card"
                card
              >
                <TabPane key={SchedulerTabs.Shift} tab={t.Shifts.shiftTitle}>
                  <ErrorBoundary>
                    <Shifts />
                  </ErrorBoundary>
                </TabPane>
                <TabPane
                  key={SchedulerTabs.Conflict}
                  tab={(
                    <Flex alignItems="center" gap={8}>
                      <span>{t.Shifts.conflictTitle}</span>
                      {!!conflictsData?.totalElements && (
                        <div className={styles.counter}>{conflictsData?.totalElements}</div>
                      )}
                    </Flex>
                  )}
                >
                  <ErrorBoundary>
                    <Conflicts />
                  </ErrorBoundary>
                </TabPane>
              </Tab>
            ) : (
              <Shifts />
            )}
          </Panel>

          <CreateModal />
        </ModalsProvider>
      </SelectedShiftProvider>
    </WorkloadGeneralProvider>
  );
};

export default Schedule;
