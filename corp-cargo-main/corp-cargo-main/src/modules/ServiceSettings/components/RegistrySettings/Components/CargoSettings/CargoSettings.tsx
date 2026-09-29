import { useHistory } from '@sber-sbertransport/mf-core';
import React, { ReactNode, Suspense, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { TabPane } from 'shared/components/Tab/TabPane';
import { Tab } from 'shared/components/Tab';
import CargoTypeHandbookComponent from 'modules/CargoType/CargoType';
import ExecutorGroupContent from 'modules/ExecutorGroup/ExecutorGroupContent';
import { CargoPackageSettings } from 'modules/CargoPackage/CargoPackageSettings';
import { CargoAutoSettings } from './CargoAutoSettings/CargoAutoSettings';
import CargoDeliveryTimeSettings
  from './CargoDeliveryTimeHandbook/components/CargoDeliveryTimeSettings/CargoDeliveryTimeSettings';
import VSPSettings from "./VSPSettings/VSPSettings";
import { REFERENCE_BOOKS } from 'constants/constants.routes';
import { useRole } from "utils/useRole";
import { Roles } from "constants/constants.app";

import styles from './CargoSettings.module.scss';
import { CargoSettingsEnum, CargoSettingsTitles } from "./types";

const ROOT_ROUTE = `${REFERENCE_BOOKS}/cargo`;

const getItems = (role: string[]): { tabLabel: string; tab: string; content: ReactNode; disabled?: boolean }[] => {
  let items = [
    {
      tabLabel: CargoSettingsTitles[CargoSettingsEnum.CARGO_PACKAGE],
      tab: CargoSettingsEnum.CARGO_PACKAGE,
      content: <CargoPackageSettings className={styles.CargoPackageWrapper}/>,
    },
    {
      tabLabel: CargoSettingsTitles[CargoSettingsEnum.CARGO_TYPES],
      tab: CargoSettingsEnum.CARGO_TYPES,
      content: <CargoTypeHandbookComponent/>,
    },
    {
      tabLabel: CargoSettingsTitles[CargoSettingsEnum.CARGO_DELIVERY_TIME],
      tab: CargoSettingsEnum.CARGO_DELIVERY_TIME,
      content: <CargoDeliveryTimeSettings/>,
    },
    {
      tabLabel: CargoSettingsTitles[CargoSettingsEnum.CARGO_AUTO_SETTINGS],
      tab: CargoSettingsEnum.CARGO_AUTO_SETTINGS,
      content: <CargoAutoSettings/>,
    },
    {
      tabLabel: CargoSettingsTitles[CargoSettingsEnum.CARGO_EXECUTORS_GROUPS_SETTINGS],
      tab: CargoSettingsEnum.CARGO_EXECUTORS_GROUPS_SETTINGS,
      content: <ExecutorGroupContent/>,
    },
    {
      tabLabel: CargoSettingsTitles[CargoSettingsEnum.CARGO_VSP_SETTINGS],
      tab: CargoSettingsEnum.CARGO_VSP_SETTINGS,
      content: <VSPSettings/>,
      role: "ROLE_ADMIN_DATA_MASTER"
    },
  ]

  if (!role.includes(Roles.ADMIN_DATA_MASTER)) {
    items = items.filter(tabItem => tabItem.tab !== 'cargoVSPSettings');
  }

  return items;
};

export const CargoSettings = withErrorBoundary(() => {
  const { push, replace } = useHistory();
  const roles = useRole();
  const items = getItems(roles);

  const { pathname: currentPath } = useLocation();

  useEffect(() => {
    if (currentPath === ROOT_ROUTE) {
      replace(`${currentPath}/cargoPackage`);
    }
  }, [replace, currentPath]);

  return (
    <Suspense fallback={<SpinWrapped />}>
      <Tab
        className={styles.Tabs}
        onChange={route => push(`${ROOT_ROUTE}/${route}`)}
        destroyInactiveTabPane
      >
        {items.map(({
          tabLabel, tab, content,
        }) => (
          <TabPane
            tab={tabLabel}
            key={tab}
            theme="card"
          >
            {content}
          </TabPane>
        ))}
      </Tab>
    </Suspense>
  );
});
