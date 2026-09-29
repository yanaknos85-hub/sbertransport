/* eslint-disable @stylistic/jsx-max-props-per-line */
import './override.scss';
import React, { Suspense } from 'react';
import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import classNames from 'classnames';
import { useTranslation } from 'i18n';
import { TabPane } from 'shared/components/Tab/TabPane';
import { Tab } from 'shared/components/Tab';
import mfLoader from 'mf/MFLoader';

const NotificationsSettingsPassengers = React.lazy(() => mfLoader(import('passengers/modules/ServiceSettings/NotificationsSettings')));
const NotificationsSettingsCargo = React.lazy(() => mfLoader(import('cargo/modules/ServiceSettings/NotificationsSettings')));
const NotificationsSettingsFleet = React.lazy(() => mfLoader(import('fleet/modules/ServiceSettings/NotificationsSettings')));
const NotificationsSettingsPlatform = React.lazy(() => mfLoader(import('platform/modules/ServiceSettings/NotificationsSettings')));

import styles from './styles.module.scss';

const NotificationsSettings = () => {
  const { t } = useTranslation();
  const { DeadlineSettings } = t.ServiceParamsPage;

  return (
    <div className={classNames(styles.container)}>
      <Tab>
        <TabPane tab={DeadlineSettings.passenger} key="Пассажирские перевозки">
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>
              <NotificationsSettingsPassengers />
            </Suspense>
          </ErrorBoundary>
        </TabPane>
        <TabPane tab={DeadlineSettings.cargo} key="Грузовые перевозки">
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>
              <NotificationsSettingsCargo />
            </Suspense>
          </ErrorBoundary>
        </TabPane>
        <TabPane tab={DeadlineSettings.fleet} key="Автосервис" disabled>
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>
              <NotificationsSettingsFleet />
            </Suspense>
          </ErrorBoundary>
        </TabPane>
        <TabPane tab={DeadlineSettings.shared} key="Общие">
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>
              <NotificationsSettingsPlatform />
            </Suspense>
          </ErrorBoundary>
        </TabPane>
      </Tab>
    </div>
  );
};

export default NotificationsSettings;
