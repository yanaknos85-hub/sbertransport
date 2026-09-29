/* eslint-disable @typescript-eslint/no-unused-vars */
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
          Roles.ADMIN_CORP_CLIENT,
          Roles.ENGINEER_OTO,
          Roles.ENGINEER_CORP_CLIENT,
          Roles.DISPATCHER_SUPPORT_SERVICE,
        ]}
      >
        <MenuItem to={routes.BUSINESS_REPORTS} icon={dev}>
          Бизнес-отчеты
        </MenuItem>
      </AccessControl>
      <AccessControl
        userPermissions={userRoles}
        allowedPermissions={[Roles.ENGINEER_CORP_CLIENT]}
      >
        <MenuItem to={routes.FRAUD_MONITORING} icon={dev}>
          Мониторинг нарушений
        </MenuItem>
      </AccessControl>
      <AccessControl
        userPermissions={userRoles}
        allowedPermissions={[
          Roles.ADMIN_DATA_MASTER,
          Roles.ADMIN_CORP_CLIENT,
        ]}
      >
        <MenuItem to={routes.EXECUTOR_GROUPS} icon={dev}>
          Группы исполнителей
        </MenuItem>
      </AccessControl>
      <AccessControl
        userPermissions={userRoles}
        allowedPermissions={[
          Roles.ADMIN_DATA_MASTER,
          Roles.ADMIN_CORP_CLIENT,
        ]}
      >
        <MenuItem to={routes.PLANNER_SRM} icon={dev}>
          Планировщик
        </MenuItem>
      </AccessControl>
    </Router>
  );
};

export {
  MenuItems as Menu
};
