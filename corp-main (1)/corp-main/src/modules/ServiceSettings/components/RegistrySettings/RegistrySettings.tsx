import React, { ReactNode, Suspense, useEffect } from 'react';
import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useTranslation } from 'i18n';
import { TabPane } from 'shared/components/Tab/TabPane';
import { Tab } from 'shared/components/Tab';
import { useHistory, useLocation } from 'react-router-dom';
import { ReferenceBooksTabs } from '../../constants.settings';

import mfLoader from 'mf/MFLoader';
import * as routes from 'constants/constants.routes';

const RegistrySettingsPlatform = React.lazy(() => mfLoader(import('platform/modules/ServiceSettings/RegistrySettings')));
const RegistrySettingsPassengers = React.lazy(() => mfLoader(import('passengers/modules/ServiceSettings/RegistrySettings')));
const RegistrySettingsCargo = React.lazy(() => mfLoader(import('cargo/modules/ServiceSettings/RegistrySettings')));
const RegistrySettingsFleet = React.lazy(() => mfLoader(import('fleet/modules/ServiceSettings/RegistrySettings')));

const pageRoute = routes.REFERENCE_BOOKS;
const defaultTab = ReferenceBooksTabs.common;

const getItems = (
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  locales: any
): {
  tabLabel: string;
  tab: string;
  content: ReactNode;
  disabled?: boolean;
}[] => [
  {
    tabLabel: locales.shared,
    tab: ReferenceBooksTabs.common,
    content: <RegistrySettingsPlatform />,
  },
  {
    tabLabel: locales.passenger,
    tab: ReferenceBooksTabs.passengers,
    content: <RegistrySettingsPassengers />,
  },
  {
    tabLabel: locales.cargo,
    tab: ReferenceBooksTabs.cargo,
    content: <RegistrySettingsCargo />,
  },
  {
    tabLabel: locales.fleet,
    tab: ReferenceBooksTabs.fleet,
    content: <RegistrySettingsFleet />,
    disabled: true,
  },
];

export const RegistrySettings = () => {
  const {
    t: {
      ServiceParamsPage: { RegistrySettings },
    },
  } = useTranslation();
  const items = getItems(RegistrySettings);
  const { push, replace } = useHistory();
  const { pathname: currentPath } = useLocation();

  useEffect(() => {
    if (currentPath === pageRoute) {
      replace(`${currentPath}/${defaultTab}`);
    }
  }, [replace, currentPath]);

  return (
    <Suspense fallback={<SpinWrapped />}>
      <Tab
        defaultActiveKey={defaultTab}
        onChange={route => push(`${pageRoute}/${route}`)}
        destroyInactiveTabPane
      >
        {items.map(({
          tabLabel, tab, content, disabled,
        }) => (
          <TabPane
            tab={tabLabel}
            key={tab}
            disabled={disabled}
          >
            <ErrorBoundary>
              <Suspense fallback={<SpinWrapped />}>
                {content}
              </Suspense>
            </ErrorBoundary>
          </TabPane>
        ))}
      </Tab>
    </Suspense>
  );
};

export default RegistrySettings;
