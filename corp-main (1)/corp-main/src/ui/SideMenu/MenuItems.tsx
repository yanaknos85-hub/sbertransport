import {
  faCog,
  faHome,
  faComments,
  faUsers,
  faUserFriends,
  faCoins,
  faCar,
  faChartPie,
  faRoute,
  faUsersCog,
  faFileAlt,
  faWrench,
  faParking
} from '@fortawesome/free-solid-svg-icons';
import React, { useMemo, useState, useEffect } from 'react';
import { Router, RouterProps } from 'react-router-dom';
import { useRole } from 'utils/useRole';
import { useTranslation } from 'i18n';
import AccessControl from 'shared/components/AccessControl';
import { Roles } from 'constants/constants.app';
import { IS_DEV } from 'constants/constants.env';
import * as routes from 'constants/constants.routes';
import { useAppStore, StoreNames } from 'stores';
import MenuItem from './MenuItem';

import mfDataLoader from 'mf/MFDataLoader';

interface MFRoutes { data: Record<string, string> }

// Грузим роуты из ремоутов
const MFBootRoutes = {
  platform: mfDataLoader(() => {
    try {
      return import('platform/routes');
    } catch {
      return Promise.resolve();
    }
  }) as MFRoutes,
  passengers: mfDataLoader(() => {
    try {
      return import('passengers/routes');
    } catch {
      return Promise.resolve();
    }
  }) as MFRoutes,
  cargo: mfDataLoader(() => {
    try {
      return import('cargo/routes');
    } catch {
      return Promise.resolve();
    }
  }) as MFRoutes,
  fleet: mfDataLoader(() => {
    try {
      return import('fleet/routes');
    } catch {
      return Promise.resolve();
    }
  }) as MFRoutes,
};

export const MenuItems: React.FC = () => {
  const { [StoreNames.configStore]: { history, ...configStore } } = useAppStore();

  const [getHistory, setHistory] = useState<RouterProps>({ history: {} } as RouterProps);
  const userRoles = useRole();
  const { t: { SideMenu } } = useTranslation();

  useMemo(() => {
    setHistory({ history });
  }, [history]);

  const MFRoutes = useMemo(() => MFBootRoutes, []) as Record<string, MFRoutes>;

  useEffect(() => {
    if (IS_DEV) {
      // eslint-disable-next-line no-console
      console.log('Routes', { 'App': routes, 'MF routes': MFRoutes });
    }
  }, [MFRoutes]);

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
        isBundleRoles
        userPermissions={userRoles}
        allowedPermissions={[
          Roles.ADMIN_DATA_MASTER,
          Roles.EMPLOYEE_CORP_CLIENT,
          Roles.CLIENT_MANAGER,
        ]}
      >
        <MenuItem to={routes.CUSTOMERS} icon={faUserFriends}>
          {SideMenu.customers}
        </MenuItem>
      </AccessControl>
      {/* <AccessControl
        userPermissions={userRoles}
        allowedPermissions={[
          Roles.ADMIN_DATA_MASTER,
          Roles.ADMIN_CORP_CLIENT,
        ]}
      >
        <MenuItem to={routes.PLANNER_SRM} icon={faRoute}>
          Планировщик
        </MenuItem>
      </AccessControl> */}
      {MFRoutes.platform?.data?.DIRECTORIES && (
        <MenuItem to={MFRoutes.platform.data.DIRECTORIES} icon={faUsers}>
          {SideMenu.orgStructure}
        </MenuItem>
      )}
      <AccessControl
        userPermissions={userRoles}
        allowedPermissions={[
          Roles.ADMIN_CORP_CLIENT,
          Roles.ENGINEER_OTO,
          Roles.ENGINEER_CORP_CLIENT,
          Roles.DISPATCHER_SUPPORT_SERVICE,
        ]}
      >
        <MenuItem to={routes.ORDER_EXECUTION} icon={faUsersCog}>
          {SideMenu.ordersMonitor}
        </MenuItem>
      </AccessControl>
      {MFRoutes.cargo?.data?.MULTI_LOGISTICS && (
      <AccessControl
        userPermissions={userRoles}
        allowedPermissions={[
          Roles.ADMIN_CORP_CLIENT,
          Roles.ENGINEER_OTO,
          Roles.ENGINEER_CORP_CLIENT,
          Roles.DISPATCHER_SUPPORT_SERVICE,
        ]}
      >
        <MenuItem to={MFRoutes.cargo?.data?.MULTI_LOGISTICS} icon={faRoute}>
          {SideMenu.logistics}
        </MenuItem>
      </AccessControl>
      )}
      {MFRoutes.platform?.data?.LIMITS && (
        <AccessControl
          userPermissions={userRoles}
          allowedPermissions={[
            Roles.ADMIN_CORP_CLIENT,
            Roles.ENGINEER_OTO,
            Roles.ENGINEER_CORP_CLIENT,
            Roles.DISPATCHER_SUPPORT_SERVICE,
          ]}
        >
          <MenuItem to={MFRoutes.platform.data.LIMITS} icon={faCoins}>
            {SideMenu.limits}
          </MenuItem>
        </AccessControl>
      )}
      <MenuItem to={routes.TRIP_SETTINGS} icon={faCar}>
        {SideMenu.tripSettings}
      </MenuItem>
      <MenuItem to={routes.SERVICE_SETTINGS} icon={faCog}>
        {SideMenu.settings}
      </MenuItem>
      {MFRoutes.platform?.data?.MUTUALITY && !configStore.env.IS_SDO && (
        <AccessControl
          userPermissions={userRoles}
          allowedPermissions={[
            Roles.ADMIN_CORP_CLIENT,
            Roles.ADMIN_DATA_MASTER,
            Roles.DISPATCHER_SUPPORT_SERVICE,
            Roles.ENGINEER_CORP_CLIENT,
            Roles.ACCESS_ADMIN,
            Roles.MAINTENANCE_ENGINEER,
            Roles.DISPATCHER_CORP_CLIENT,
          ]}
        >
          <MenuItem to={MFRoutes.platform?.data?.MUTUALITY} icon={faChartPie}>
            {SideMenu.settlements}
          </MenuItem>
        </AccessControl>
      )}
      <MenuItem to={routes.TARIFF_SETTINGS} icon={faFileAlt}>
        {SideMenu.tariffs}
      </MenuItem>
      {!configStore.env.IS_SDO && (
        <AccessControl
          userPermissions={userRoles}
          allowedPermissions={[
            Roles.DISPATCHER_ROOM_ADMIN,
            Roles.MAIN_DISPATCHER_CONTRACTOR,
            Roles.DISPATCHER_CONTRACTOR,
          ]}
        >
          <MenuItem to={routes.AUTOPARKS} icon={faCar}>
            {SideMenu.autopark}
          </MenuItem>
        </AccessControl>
      )}
      {MFRoutes.fleet?.data?.FLEET_MANAGEMENT && (
        <>
          <AccessControl
            userPermissions={userRoles}
            allowedPermissions={[
              Roles.ADMIN_CORP_CLIENT,
              Roles.ADMIN_DATA_MASTER,
              Roles.TELEMECHANIC,
              Roles.TELEMECHANIC_ORGANIZATION,
              Roles.ENGINEER_CORP_CLIENT,
              Roles.DISPATCHER_SUPPORT_SERVICE,
              Roles.MEDIC,
            ]}
          >
            <MenuItem to={MFRoutes.fleet?.data?.FLEET_MANAGEMENT} icon={faWrench}>
              {SideMenu.fleetManagement}
            </MenuItem>
          </AccessControl>
          <AccessControl
            userPermissions={userRoles}
            allowedPermissions={[
              Roles.PARKING_ADMIN,
              Roles.PARKING_ADMIN_ORGANIZATION,
            ]}
          >
            <MenuItem to={MFRoutes.fleet?.data?.PARKING_MANAGEMENT} icon={faParking}>
              {SideMenu.parking}
            </MenuItem>
          </AccessControl>
        </>
      )}
      {MFRoutes.platform?.data?.AI && (
        <AccessControl
          userPermissions={userRoles}
          allowedPermissions={[Roles.AI_AGENT_MASTER]}
        >
          <MenuItem to={MFRoutes.platform?.data?.AI} icon={faCog}>
            {SideMenu.ai}
          </MenuItem>
        </AccessControl>
      )}
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
        <MenuItem to={routes.REPORTS} icon={faChartPie}>
          {SideMenu.reports}
        </MenuItem>
      </AccessControl>
      {MFRoutes.platform?.data?.SUPPORT && (
        <MenuItem to={MFRoutes.platform.data.SUPPORT} icon={faComments}>
          {SideMenu.support}
        </MenuItem>
      )}
      {MFRoutes.platform?.data?.MAINTENANCE && (
        <AccessControl
          userPermissions={userRoles}
          allowedPermissions={[Roles.MAINTENANCE_ENGINEER]}
        >
          <MenuItem to={MFRoutes.platform.data.MAINTENANCE} icon={faChartPie}>
            {SideMenu.maintenance}
          </MenuItem>
        </AccessControl>
      )}

    </Router>
  );
};

export {
  MenuItems as Menu
};
