import * as React from 'react';
import { useHistory, useLocation } from 'react-router-dom';
import { Tab } from 'shared/components/Tab';
import { TabPane } from 'shared/components/Tab/TabPane';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import { useRole } from 'utils/useRole';

import mfLoader from 'mf/MFLoader';

import * as routes from 'constants/constants.routes';
import { Roles } from 'constants/constants.app';
import { useTranslation } from 'i18n';

import Registry from 'modules/Registry/Registry';
import BusinessReports from 'modules/BusinessReports/BusinessReports';

const Analytics = React.lazy(() => mfLoader(import('platform/modules/Analytics')));
const Analytics_v2 = React.lazy(() => mfLoader(import('platform/modules/Analytics_v2')));
const FraudMonitoring = React.lazy(() => mfLoader(import('passengers/modules/FraudMonitoring')));

import styles from './styles.module.scss';

const Reports: React.FC<{ analytics?: JSX.Element }> = () => {
  const history = useHistory();
  const location = useLocation();
  const roles = useRole();
  const { t } = useTranslation();

  const isRoot = location.pathname === routes.REPORTS;

  const defaultActiveKey = location.pathname.includes(routes.ANALYTICS)
    ? location.pathname.includes(routes.ANALYTICS_V2) ? routes.ANALYTICS_V2 : routes.ANALYTICS
    : location.pathname.includes(routes.FRAUD_MONITORING)
      ? routes.FRAUD_MONITORING
      : location.pathname.includes(routes.REGISTRY)
        ? routes.REGISTRY_PASSENGERS_PUBLIC
        : routes.BUSINESS_REPORTS_PASSENGERS_PUBLIC;

  React.useEffect(() => {
    if (isRoot) {
      history.push(routes.ANALYTICS);
    }
  }, [isRoot, history, location]);

  if (isRoot) {
    return null;
  }

  return (
    <div className={styles.container}>
      <Tab
        themeSize="xl"
        defaultActiveKey={defaultActiveKey}
        onChange={route => history.push(route)}
      >
        <TabPane
          themeSize="xl"
          tab={t.reports.analytics}
          key={location.pathname.includes(routes.ANALYTICS_V2) ? routes.ANALYTICS_V2 : routes.ANALYTICS}
        >
          <ErrorBoundary>
            <React.Suspense fallback={<SpinWrapped />}>
              {location.pathname.includes(routes.ANALYTICS_V2)
                ? <Analytics_v2 />
                : <Analytics />}
            </React.Suspense>
          </ErrorBoundary>
        </TabPane>
        <TabPane
          themeSize="xl"
          tab={t.reports.registry}
          key={routes.REGISTRY_PASSENGERS_PUBLIC}
        >
          <ErrorBoundary>
            <React.Suspense fallback={<SpinWrapped />}>
              <Registry />
            </React.Suspense>
          </ErrorBoundary>
        </TabPane>
        {roles.includes(Roles.ADMIN_DATA_MASTER) && (
          <TabPane
            themeSize="xl"
            tab={t.reports.businessReports}
            key={routes.BUSINESS_REPORTS_PASSENGERS_PUBLIC}
          >
            <ErrorBoundary>
              <React.Suspense fallback={<SpinWrapped />}>
                <BusinessReports />
              </React.Suspense>
            </ErrorBoundary>
          </TabPane>
        )}
        {roles.includes(Roles.ENGINEER_CORP_CLIENT) && (
          <TabPane
            themeSize="xl"
            tab={t.reports.fraudMonitoring}
            key={routes.FRAUD_MONITORING}
          >
            <ErrorBoundary>
              <React.Suspense fallback={<SpinWrapped />}>
                <FraudMonitoring />
              </React.Suspense>
            </ErrorBoundary>
          </TabPane>
        )}
      </Tab>
    </div>
  );
};

export default Reports;
