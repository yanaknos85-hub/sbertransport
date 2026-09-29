/* eslint-disable no-unused-expressions */
import { faHome, faFrog as dev, faGlobeAmericas as prod } from '@fortawesome/free-solid-svg-icons';
import React, { useMemo, useState } from 'react';
import { Router, RouterProps } from 'react-router-dom';
import { Roles } from 'constants/constants.app';
import * as routes from 'constants/constants.routes';
import { useAppStore, StoreNames } from 'stores';
import { useRole } from 'utils/useRole';
import AccessControl from 'shared/components/AccessControl';
import { useTranslation } from 'i18n';
import MenuItem from './MenuItem';

export const MenuItems: React.FC = () => {
  const {
    [StoreNames.configStore]: { history },
  } = useAppStore();

  const [getHistory, setHistory] = useState<RouterProps>({ history: {} } as RouterProps);
  const userRoles = useRole();

  const { t: { SideMenu } } = useTranslation();

  useMemo((): void => {
    setHistory({ history });
  }, [history]);

  return (
    <Router history={getHistory.history}>
      <MenuItem
        to={routes.HOME}
        exact
        icon={faHome}
      >
        {SideMenu.main}
      </MenuItem>

      <MenuItem to={routes.PAGE} icon={prod}>
        Page
      </MenuItem>
      <AccessControl
        userPermissions={userRoles}
        allowedPermissions={[
          Roles.ADMIN_CORP_CLIENT,
          Roles.ENGINEER_OTO,
          Roles.ENGINEER_CORP_CLIENT,
          Roles.DISPATCHER_SUPPORT_SERVICE,
        ]}
      >
        <MenuItem to={routes.ORDER_EXECUTION} icon={dev}>
          {SideMenu.ordersMonitor}
        </MenuItem>
      </AccessControl>
      <AccessControl
        userPermissions={userRoles}
        allowedPermissions={[
          Roles.ADMIN_CORP_CLIENT,
          Roles.ENGINEER_OTO,
          Roles.ENGINEER_CORP_CLIENT,
          Roles.DISPATCHER_SUPPORT_SERVICE,
        ]}
      >
        <MenuItem to={routes.MULTI_LOGISTICS} icon={prod}>
          {SideMenu.logistics}
        </MenuItem>
      </AccessControl>
      <MenuItem to={routes.TRIP_SETTINGS} icon={dev}>
        {SideMenu.tripSettings}
      </MenuItem>
      <MenuItem to={routes.SERVICE_SETTINGS} icon={dev}>
        {SideMenu.serviceSettings}
      </MenuItem>
      <MenuItem to={routes.TARIFF_SETTINGS} icon={dev}>
        {SideMenu.tariffs}
      </MenuItem>
      <AccessControl
        userPermissions={userRoles}
        allowedPermissions={[
          Roles.ADMIN_DATA_MASTER,
          Roles.ADMIN_CORP_CLIENT,
          Roles.ENGINEER_OTO,
          Roles.ENGINEER_CORP_CLIENT,
          Roles.DISPATCHER_SUPPORT_SERVICE,
        ]}
      >
        <MenuItem to={routes.REGISTRY} icon={dev}>
          Реестр
        </MenuItem>
      </AccessControl>
      <AccessControl
        userPermissions={userRoles}
        allowedPermissions={[
          Roles.ADMIN_DATA_MASTER,
          Roles.ROLE_DATA_ANALYST,
        ]}
      >
        <MenuItem to={routes.BUSINESS_REPORTS_CARGO} icon={dev}>
          Бизнес-отчеты
        </MenuItem>
      </AccessControl>
    </Router>
  );
};

export {
  MenuItems as Menu
};
