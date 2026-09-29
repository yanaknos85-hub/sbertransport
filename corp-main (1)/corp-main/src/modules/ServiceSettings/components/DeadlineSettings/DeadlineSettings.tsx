/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC, Suspense } from 'react';
import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { TabPane } from 'shared/components/Tab/TabPane';
import { Tab } from 'shared/components/Tab';
import mfLoader from 'mf/MFLoader';
const DeadlineSettingsPassengers = React.lazy(() => mfLoader(import('passengers/modules/ServiceSettings/DeadlineSettings')));
const DeadlineSettingsCargo = React.lazy(() => mfLoader(import('cargo/modules/ServiceSettings/DeadlineSettings')));
const DeadlineSettingsFleet = React.lazy(() => mfLoader(import('fleet/modules/ServiceSettings/DeadlineSettings')));
import styles from './DeadlineSettings.module.scss';

export const DeadlineSettings: FC = () => {
  return (
    <Tab className={styles.tabContainer}>
      <TabPane tab="Пассажирские перевозки" key="Пассажирские перевозки">
        <ErrorBoundary>
          <Suspense fallback={<SpinWrapped />}>
            <DeadlineSettingsPassengers />
          </Suspense>
        </ErrorBoundary>
      </TabPane>
      <TabPane tab="Грузовые перевозки" key="Грузовые перевозки">
        <ErrorBoundary>
          <Suspense fallback={<SpinWrapped />}>
            <DeadlineSettingsCargo />
          </Suspense>
        </ErrorBoundary>
      </TabPane>
      <TabPane tab="Автосервис" key="Автосервис" disabled>
        <ErrorBoundary>
          <Suspense fallback={<SpinWrapped />}>
            <DeadlineSettingsFleet />
          </Suspense>
        </ErrorBoundary>
      </TabPane>
    </Tab>
  );
};

export default DeadlineSettings;
